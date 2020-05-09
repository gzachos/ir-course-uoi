package com.gzachos.ir.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
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
			queryParser.setDefaultOperator(Operator.AND);
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
				doPagingSearch(indexSearcher, query, 5);
			}
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void doPagingSearch(IndexSearcher indexSearcher, Query query, int numPages) throws IOException {
		int docsToFetch = numPages * Globals.HITS_PER_PAGE;
		long startTime = System.currentTimeMillis();
		TopDocs searchResults = indexSearcher.search(query, docsToFetch + Globals.HITS_PER_PAGE);
		double searchTime = (System.currentTimeMillis() - startTime) / 1000.0;
		ScoreDoc[] hitsArray = searchResults.scoreDocs;
		ArrayList<ScoreDoc> hits = new ArrayList<ScoreDoc>(Arrays.asList(hitsArray));
		ArrayList<Document> returnedDocs = new ArrayList<Document>();
		TotalHits totalHits = searchResults.totalHits; 
		int numTotalHits = Math.toIntExact(totalHits.value);
		if (numTotalHits == 0) {
			System.out.println("Found 0 results");
			return;
		}
		String relationStr = (totalHits.relation == TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO) ? "More than" : "About";
		System.out.println("\n" + relationStr + " " + numTotalHits + " results (" + searchTime + " seconds)\n");
		ScoreDoc lastFetchedDoc = hits.get(hits.size()-1);

		int end = docsToFetch;
		boolean notEnoughTotalHits = false;
		if (docsToFetch >= numTotalHits) {
			end = numTotalHits;
			notEnoughTotalHits = true;
		}
		int skippedDocs = 0, page = 0;
		int numReturnedDocs = 0;

		for (int i = 0; i < end; i++) {
			boolean skip = false;
			Document doc = indexSearcher.doc(hits.get(i).doc);
			for (int j = 0; j < returnedDocs.size(); j++) {
				Document prevDoc = returnedDocs.get(j);
				if (prevDoc.get("title").equals(doc.get("title"))) {
					skip = true;
					skippedDocs++;
					break;
				}
			}
			
			if (!skip) {
				returnedDocs.add(doc);
				numReturnedDocs = returnedDocs.size();
				
				String url = doc.get("url");
				String title = doc.get("title");
				String summary = doc.get("summary");
				
				if (url != null) {
					System.out.println(numReturnedDocs + " - '" + title + "' - " + url);
				//	System.out.println(summary);
				}
				
				if (numReturnedDocs == numTotalHits || numReturnedDocs == docsToFetch)
					break;
				
				if (numReturnedDocs % Globals.HITS_PER_PAGE == 0)
					System.out.println("\n################## Page " + (++page + 1) + "\n");
			}

			if (i == (end-1) && !notEnoughTotalHits) {
				int newDocsToFetch = Math.max(Globals.HITS_PER_PAGE, docsToFetch - numReturnedDocs);
				// System.out.println("About to fetch: " + newDocsToFetch + " more docs");
				TopDocs newSearchResults = indexSearcher.searchAfter(lastFetchedDoc, query, newDocsToFetch);
				ScoreDoc[] newHitsArray = newSearchResults.scoreDocs;
				ArrayList<ScoreDoc> newHits = new ArrayList<ScoreDoc>(Arrays.asList(newHitsArray));
				hits.addAll(newHits);
				int numNewHits = newHits.size();
				// System.out.println("Fetched " + numNewHits + " more docs");
				end += numNewHits;
				lastFetchedDoc = hits.get(hits.size()-1);
			}
		}
		System.out.println("\n\nreturned: " + returnedDocs.size());
		System.out.println("skipped:  " + skippedDocs);
	}
	
}
