package com.gzachos.ir.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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

import com.gzachos.ir.Globals;

public class DocumentSearcher {
	IndexReader indexReader;
	IndexSearcher indexSearcher;
	Analyzer standardAnalyzer;
	MultiFieldQueryParser defaultQueryParser, currentQueryParser = null;
	
	public DocumentSearcher(String indexDir) {
		try {
			Path indexDirPath = Paths.get(indexDir);
			indexReader = DirectoryReader.open(FSDirectory.open(indexDirPath));
			indexSearcher = new IndexSearcher(indexReader);
			standardAnalyzer = new StandardAnalyzer();
			defaultQueryParser = new MultiFieldQueryParser(Globals.DOCUMENT_FIELDS, standardAnalyzer, Globals.QUERY_BOOSTS);
			defaultQueryParser.setDefaultOperator(Operator.AND);
			currentQueryParser = defaultQueryParser;
		//	currentQueryParser = new MultiFieldQueryParser(Globals.DOCUMENT_FIELDS, standardAnalyzer);
		//	currentQueryParser.setDefaultOperator(Operator.AND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			indexReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getCorpusSize() {
		return indexReader.numDocs();
	}
	
	public Query parseQuery(String queryStr) {
		if (queryStr == null)
			return null;
		queryStr = queryStr.trim();
		if (queryStr.length() == 0)
			return null;
		
		try {
			Query query = currentQueryParser.parse(queryStr);
			System.out.println("Searching for: \"" + query.toString() + "\"");
			return query;
		} catch (ParseException pe) {
			// System.err.println("Cannot parse query!");
			return null;
		}
	}
	
	public Query parseAdvancedQuery(String queryStr, String fields[], Map<String, Float> boosts) {
		currentQueryParser = new MultiFieldQueryParser(fields, standardAnalyzer, boosts);
		Query query = parseQuery(queryStr);
		currentQueryParser = defaultQueryParser;
		return query;
	}
	
	public SearchResult executeQuery(Query query, int numPages, SearchResult prevResults) {
		try {
			return doPagingSearch(query, numPages, prevResults);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private SearchResult doPagingSearch(Query query, int numPages, SearchResult prevResults) throws IOException {
		int docsToFetch = numPages * Globals.HITS_PER_PAGE;
		TopDocs searchResults;
		int numPrevReturnedDocs = 0;
		double searchTime = 0;
		ArrayList<ScoreDoc> prevReturnedDocs = null;
	
		if (prevResults != null && prevResults.getNumHits() > 0) {
			prevReturnedDocs = prevResults.getHits();
			numPrevReturnedDocs = prevReturnedDocs.size();
			ScoreDoc lastReturnedDoc = prevReturnedDocs.get(numPrevReturnedDocs-1);
			searchResults = indexSearcher.searchAfter(lastReturnedDoc, query, docsToFetch + Globals.HITS_PER_PAGE);
		} else {
			long startTime = System.currentTimeMillis();
			searchResults = indexSearcher.search(query, docsToFetch + Globals.HITS_PER_PAGE);
			searchTime = (System.currentTimeMillis() - startTime) / 1000.0;
		}
		boolean isPartialSearch = (prevResults != null); // TODO verify condition
		
		ArrayList<ScoreDoc> returnedDocs = new ArrayList<ScoreDoc>();
		TotalHits totalHits = searchResults.totalHits; 
		int numTotalHits = Math.toIntExact(totalHits.value);
		ScoreDoc[] hitsArray = searchResults.scoreDocs;
		ArrayList<ScoreDoc> hits = new ArrayList<ScoreDoc>(Arrays.asList(hitsArray));
		int numHits = hits.size();
		if (numHits == 0)
			return new SearchResult(returnedDocs, searchTime, numTotalHits, totalHits.relation, isPartialSearch);

		ScoreDoc lastFetchedDoc = hits.get(numHits-1);
		boolean haveEnoughTotalHits = (docsToFetch < numTotalHits);
		int numReturnedDocs = 0;
		for (int i = 0; i < numHits; i++) {
			ScoreDoc scoreDoc = hits.get(i);
			Document doc = indexSearcher.doc(scoreDoc.doc);

			if (!alreadyReturnedDoc(doc, indexSearcher, prevReturnedDocs, returnedDocs)) {
				returnedDocs.add(scoreDoc);
				if ((numReturnedDocs = returnedDocs.size()) == docsToFetch)
					break;
			}

			if (i == (numHits-1) && haveEnoughTotalHits) {
				int newDocsToFetch = (docsToFetch - numReturnedDocs) + Globals.HITS_PER_PAGE;
//				System.out.println("About to fetch: " + newDocsToFetch + " more docs");
				TopDocs newSearchResults = indexSearcher.searchAfter(lastFetchedDoc, query, newDocsToFetch);
				ScoreDoc[] newHitsArray = newSearchResults.scoreDocs;
				ArrayList<ScoreDoc> newHits = new ArrayList<ScoreDoc>(Arrays.asList(newHitsArray));
				hits.addAll(newHits);
//				System.out.println("Fetched " + newHits.size() + " more docs");
				numHits += newHits.size();
				lastFetchedDoc = hits.get(hits.size()-1); // In case of 0 hits, lastFetchedDoc value will be retained.
			}
		}
		return new SearchResult(returnedDocs, searchTime, numTotalHits, totalHits.relation, isPartialSearch);
	}
	
	private boolean alreadyReturnedDoc(Document doc, IndexSearcher indexSearcher, ArrayList<ScoreDoc> prevReturnedDocs,
			ArrayList<ScoreDoc> returnedDocs) throws IOException {
		Document prevDoc;
		String docTitle = doc.get("title");
		
		if (prevReturnedDocs != null) {
			for (int j = 0; j < prevReturnedDocs.size(); j++) {
				prevDoc = indexSearcher.doc(prevReturnedDocs.get(j).doc);
				if (prevDoc.get("title").equals(docTitle))
					return true;
			}
		}
		
		for (int j = 0; j < returnedDocs.size(); j++) {
			prevDoc = indexSearcher.doc(returnedDocs.get(j).doc);
			if (prevDoc.get("title").equals(docTitle))
				return true;
		}
		return false;
	}
	
	public void printReturnedDocs(SearchResult searchResult) {
		int page = 0;

		if (searchResult == null)
			return;
		
		if (!searchResult.isPartialSearchResult())
			System.out.println(searchResult.getStatsStr());
		
		ArrayList<ScoreDoc> scoreDocs = searchResult.getHits();
		for (int i = 0; i < scoreDocs.size(); i++) {
			Document doc;
			try {
				doc = indexSearcher.doc(scoreDocs.get(i).doc);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			String url = doc.get(Globals.URL_FIELD_NAME);
			String title = doc.get(Globals.TITLE_FIELD_NAME);
			String summary = doc.get(Globals.SUMMARY_FIELD_NAME);
			
			if (url != null) {
				if (i % Globals.HITS_PER_PAGE == 0)
					System.out.println("\n################## Page " + (++page) + "\n");
				System.out.println((i+1) + " - '" + title + "' - " + url);
				System.out.println(summary);
			}
		}
	}
	
	public ArrayList<Document> getDocuments(SearchResult searchResult) {
		if (searchResult == null)
			return null;
		
		ArrayList<Document> docs = new ArrayList<Document>();
		ArrayList<ScoreDoc> scoreDocs = searchResult.getHits();
		for (int i = 0; i < scoreDocs.size(); i++) {
			Document doc;
			try {
				doc = indexSearcher.doc(scoreDocs.get(i).doc);
				docs.add(doc);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return docs;
	}
}
