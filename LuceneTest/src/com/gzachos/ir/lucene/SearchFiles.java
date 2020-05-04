package com.gzachos.ir.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import com.gzachos.ir.Config;
import com.gzachos.ir.Globals;

public class SearchFiles {
	
	public static void searchFiles() {
		try {
			Path indexDirPath = Paths.get(Config.INDEX_PATH);
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexDirPath));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer standardAnalyzer = new StandardAnalyzer();
			
			InputStreamReader inStreamReader =  new InputStreamReader(System.in, StandardCharsets.UTF_8);
			BufferedReader inputReader = new BufferedReader(inStreamReader);
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Globals.DOCUMENT_FIELDS, standardAnalyzer);
			System.out.println("Press \"Enter\" to exit search...");
			while (true) {
				System.out.print("> ");
				System.out.flush();
				String line = inputReader.readLine();
				if (line == null || line.length() == -1)
					break;
				line = line.trim();
				if (line.length() == 0)
					break;
				Query query = queryParser.parse(line);
				System.out.println("Searching for: \"" + query.toString() + "\"");
				doPagingSearch(indexSearcher, query, 10);
			}
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void doPagingSearch(IndexSearcher indexSearcher, Query query, int hitsPerPage) throws IOException {
		TopDocs searchResults = indexSearcher.search(query, hitsPerPage);
		ScoreDoc[] hits = searchResults.scoreDocs;
		int numTotalHits = Math.toIntExact(searchResults.totalHits.value);
		System.out.println(numTotalHits + " total matching documents");
		
		for (int i = 0; i < hits.length; i++) {
			Document doc = indexSearcher.doc(hits[i].doc);
			String url = doc.get("url");
			String title = doc.get("title");
			String summary = doc.get("summary");
			if (url != null) {
				System.out.println((i+1) + " - " + title + " - " + url);
				System.out.println(summary + "\n");
			}
		}
	}
	
}
