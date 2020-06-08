package com.gzachos.ir.lucene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.gzachos.ir.Globals;

public class SpellingChecker {
	private String indexPath, spellIndexPath;
	private HashMap<String, SpellChecker> perFieldSpellchecker;
	private Analyzer analyzer;

	public SpellingChecker(String indexPath, String spellIndexPath) {
		this.indexPath = indexPath;
		this.spellIndexPath = spellIndexPath;
		perFieldSpellchecker = new HashMap<String, SpellChecker>();
		createIndex(false);
	}

	public void createIndex(boolean overwriteIndex) {
		try {
			Path spellIndexDirPath = Paths.get(spellIndexPath);

			if (!Files.isDirectory(spellIndexDirPath)) {
				System.err.println(spellIndexPath + ": Not a directory");
				System.exit(-1);
			}

			if (!Files.isWritable(spellIndexDirPath)) {
				System.err.println(spellIndexPath + ": Permission writing denied");
				System.exit(-1);
			}

			analyzer = CustomAnalyzer.builder().withTokenizer(Globals.TOKENIZER_NAME)
					.addTokenFilter(Globals.TOKENFILTER_NAME).build();
			Path indexDirPath = Paths.get(indexPath);
			Directory indexDirectory = FSDirectory.open(indexDirPath);
			IndexReader indexReader = DirectoryReader.open(indexDirectory);

			SpellChecker spellchecker;
			for (String field : Globals.DOCUMENT_FIELDS) {
				Date startDate = new Date();
				System.out.println("Creating spell index for field '" + field + "'");
				Path fieldSpellIndexPath = Paths.get(spellIndexPath, field);
				Directory fieldSpellIndexDir = FSDirectory.open(fieldSpellIndexPath);

				if (DirectoryReader.indexExists(fieldSpellIndexDir)) {
					System.out.println("Spell index already exists: "
							+ fieldSpellIndexPath.toString());
					if (!overwriteIndex) {
						spellchecker = new SpellChecker(fieldSpellIndexDir);
						perFieldSpellchecker.put(field, spellchecker);
						continue;
					}
					System.out.println("About to overwrite existing spell index...");
				}

				spellchecker = new SpellChecker(fieldSpellIndexDir);
				perFieldSpellchecker.put(field, spellchecker);

				IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				iwc.setOpenMode(OpenMode.APPEND);
				iwc.setRAMBufferSizeMB(512.0);
				LuceneDictionary dictionary = new LuceneDictionary(indexReader, field);
				// spellchecker.setStringDistance(new NGramDistance());
				spellchecker.indexDictionary(dictionary, iwc, true);
				fieldSpellIndexDir.close();
				Date endDate = new Date();
				double indexingDuration = (endDate.getTime() - startDate.getTime()) / 1000.0;
				System.out.println(
						"Finished creation of spell index in " + indexingDuration + " seconds");
			}
			indexReader.close();
		} catch (Exception e) {
			System.err.println("Error opening FSDirectory");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public ArrayList<String> getSuggestions(String term, String field) {
		ArrayList<String> returnedSuggestions = new ArrayList<String>();
		try {
			Path indexDirPath = Paths.get(indexPath);
			Directory indexDirectory = FSDirectory.open(indexDirPath);
			IndexReader indexReader = DirectoryReader.open(indexDirectory);
			term = analyzer.normalize(field, term).utf8ToString();
			String[] suggestions = perFieldSpellchecker.get(field).suggestSimilar(term, 5, indexReader,
					field, SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
			for (String suggestion : suggestions) {
				if (!suggestion.equals(term))
					returnedSuggestions.add(suggestion);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnedSuggestions;
	}
}
