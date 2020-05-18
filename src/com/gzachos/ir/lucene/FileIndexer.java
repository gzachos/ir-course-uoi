package com.gzachos.ir.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.gzachos.ir.Config;
import com.gzachos.ir.Globals;


public class FileIndexer {
	private int n = 0;
	private String indexPath;
	
	public FileIndexer(String indexPath) {
		this.indexPath = indexPath;
	}
	
	public void createIndex(boolean overwriteIndex) {
		try {
			Path indexDirPath = Paths.get(indexPath);

			if (!Files.isDirectory(indexDirPath)) {
				System.err.println(Config.INDEX_PATH + ": Not a directory");
				System.exit(-1);
			}
			
			if (!Files.isWritable(indexDirPath)) {
				System.err.println(Config.INDEX_PATH + ": Permission writing denied");
				System.exit(-1);
			}
			
			System.out.println("Indexing files...");
			Date startDate = new Date();
			Directory indexDir = FSDirectory.open(indexDirPath);
			
			if (DirectoryReader.indexExists(indexDir)) {
				System.out.println("Index already exists: " + indexDirPath.toString());
				if (!overwriteIndex)
					return;
				System.out.println("About to overwrite existing index...");
			}
			
			// Create a new config, using StandardAnalyzer as the analyzer.
			IndexWriterConfig iwc = new IndexWriterConfig();
			// iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			iwc.setOpenMode(OpenMode.CREATE); // to overwrite index
			iwc.setRAMBufferSizeMB(512.0);
			IndexWriter indexWriter = new IndexWriter(indexDir, iwc);
			indexDocs(indexWriter);
			int numDocs = indexWriter.getDocStats().numDocs;
			indexWriter.close();
			indexDir.close();
			Date endDate = new Date();
			double indexingDuration = (endDate.getTime() - startDate.getTime()) / 1000.0;
			System.out.println("\nIndexing of " + numDocs + " documents finished in " 
					+ indexingDuration + " seconds");
		} catch (Exception e) {
			System.err.println("Error opening FSDirectory");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void indexDocs(final IndexWriter indexWriter) {
		Path corpusDirPath = Paths.get(Config.CORPUS_PATH);
		
		if (Files.notExists(corpusDirPath)) {
			System.err.println(Config.CORPUS_PATH + ": No such file or directory");
			System.exit(-1);
		}
		
		if (!Files.isDirectory(corpusDirPath)) {
			System.err.println(Config.CORPUS_PATH + ": Not a directory");
			System.exit(-1);
		}
		
		if (!Files.isReadable(corpusDirPath)) {
			System.err.println(Config.CORPUS_PATH + " : Permission reading denied");
			System.exit(-1);
		}
		
		try {
			Files.list(corpusDirPath).forEach(fp -> indexDoc(indexWriter, fp));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void indexDoc(IndexWriter indexWriter, Path filePath) {
		System.out.println(++n + " : Indexing \"" + filePath.getFileName() + "\"");
		try {
			Document doc = parseVirtualXml(filePath);
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Document parseVirtualXml(Path filePath) throws IOException {
		Document doc = new Document();
		BufferedReader br = Files.newBufferedReader(filePath);
		boolean readURL = false, readTitle = false, readHeading = false,
				readContent = false, readUpdateTime = false, readPublicationTime = false;
		String line, contentStr = "", mediaStr = "", quoteStr = "",
				currHeading = "", summaryStr = "", url = "", title = "",
				referencesStr = "";
		long updateTime = 0, publicationTime = 0;
		
		while ((line = br.readLine()) != null) {
			if (line.equals("<url>"))
				readURL = true;
			else if (line.equals("</url>"))
				readURL = false;
			else if (line.equals("<title>"))
				readTitle = true;
			else if (line.equals("</title>"))
				readTitle = false;
			else if (line.equals("<heading>"))
				readHeading = true;
			else if (line.equals("</heading>"))
				readHeading = false;
			else if (line.equals("<content>"))
				readContent = true;
			else if (line.equals("</content>"))
				readContent = false;
			else if (line.equals("<published>"))
				readPublicationTime = true;
			else if (line.equals("</published>"))
				readPublicationTime = false;
			else if (line.equals("<updated>"))
				readUpdateTime = true;
			else if (line.equals("</updated>"))
				readUpdateTime = false;
			
			else if (readURL) {
				url = line;
			} else if (readTitle) {
				title = line;
			} else if (readUpdateTime) {
				updateTime = Long.valueOf(line);
			} else if (readPublicationTime) {
				publicationTime = Long.valueOf(line);
			} else if (readHeading) {
				currHeading = line;
				if (currHeading.equals("__multimedia__")) {
					mediaStr = "";
				} else if (currHeading.equals("__quotes__")) {
					quoteStr = "";
				} else if (currHeading.equals("__infobox__")) {
					// Do nothing
				} else if (currHeading.equals("__summary__")) {
					summaryStr = "";
				} else if (currHeading.equals("References")) {
					referencesStr = "";
				} else if (!currHeading.equals(title)){
					contentStr += line + "\n";
				}
			} else if (readContent) {
				if (currHeading.equals("__multimedia__"))
					mediaStr += line + "\n";
				else if (currHeading.equals("__quotes__"))
					quoteStr += line + "\n";
				else if (currHeading.equals("__summary__"))
					summaryStr += line + "\n";  // Summary should only one line.
				else if (currHeading.contentEquals("References"))
					referencesStr += line + "\n";
				else  // Handle simple sections and infobox.
					contentStr += line + "\n";
			}
		}
		br.close();
		
		doc.add(new StoredField(Globals.URL_FIELD_NAME, url));
		doc.add(new TextField(Globals.TITLE_FIELD_NAME, title, Field.Store.YES));
		doc.add(new TextField(Globals.CONTENT_FIELD_NAME, contentStr, Field.Store.NO));
		doc.add(new StoredField(Globals.SUMMARY_FIELD_NAME, summaryStr));
		
		if (mediaStr.length() > 0)
			doc.add(new TextField(Globals.MULTIMEDIA_FIELD_NAME, mediaStr, Field.Store.NO));
		if (quoteStr.length() > 0)
			doc.add(new TextField(Globals.QUOTES_FIELD_NAME, quoteStr, Field.Store.NO));
		if (referencesStr.length() > 0)
			doc.add(new TextField(Globals.REFERENCES_FIELD_NAME, referencesStr, Field.Store.NO));
		if (publicationTime > 0)
			doc.add(new LongPoint(Globals.PUBLICATION_TIME_FIELD_NAME, publicationTime));
		if (updateTime > 0)
			doc.add(new LongPoint(Globals.UPDATE_TIME_FIELD_NAME, updateTime));
		
		return doc;
	}
	
}
