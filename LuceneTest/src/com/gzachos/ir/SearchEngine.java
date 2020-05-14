package com.gzachos.ir;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import com.gzachos.ir.lucene.DocumentSearcher;
import com.gzachos.ir.lucene.FileIndexer;
import com.gzachos.ir.lucene.QueryInfo;
import com.gzachos.ir.lucene.SearchResult;

public class SearchEngine {
	private static SearchEngine instance = null;
	
	boolean overwriteIndex;
	private FileIndexer fileIndexer;
	private DocumentSearcher docSearcher;
	private Stack<QueryInfo> queryInfos;
	private QueryInfo currentQueryInfo;
	private ArrayList<Document> pendingDocHits;
	
	private SearchEngine() {
		 fileIndexer = new FileIndexer(Config.INDEX_PATH);
		 docSearcher = new DocumentSearcher(Config.INDEX_PATH);
		 queryInfos = new Stack<QueryInfo>();
		 pendingDocHits = null;
		 overwriteIndex = false;
		 createIndex();
	}
	
	public static SearchEngine getInstance() {
		if (instance == null)
			instance = new SearchEngine();
		return instance;
	}
	
	public void searchFor(String queryStr, int numPages) {
		Query query = docSearcher.parseQuery(queryStr);
		SearchResult searchResult = docSearcher.executeQuery(query, numPages, null);
		currentQueryInfo = new QueryInfo(queryStr, query, searchResult);
		pendingDocHits = docSearcher.getDocuments(searchResult);
	}
	
	public ArrayList<Document> getPendingDocHits() {
		if (currentQueryInfo == null || pendingDocHits == null)
			return null;
		ArrayList<Document> tmpDocs = pendingDocHits;
		pendingDocHits = null;
		return tmpDocs;
	}
	
	public String getCurrentQueryStats() {
		if (currentQueryInfo == null)
			return "";
		return currentQueryInfo.getSearchResult().getStatsStr();
	}
	
	public void clearCurrentQuery() {
		queryInfos.push(currentQueryInfo);
		currentQueryInfo = null;
	}
	
	public ArrayList<Document> searchAfter(int numPages) {
		if (currentQueryInfo == null)
			return null;
		SearchResult searchResult = docSearcher.executeQuery(
				currentQueryInfo.getQuery(),
				numPages,
				currentQueryInfo.getSearchResult()
		);
		// In order to keep lastReturnedDoc up-to-date.
		currentQueryInfo.appendToSearchResult(searchResult);
		return docSearcher.getDocuments(searchResult);
	}
	
	public void createIndex() {
		if (overwriteIndex) {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			System.out.print("About to overwrite existing index. Are you sure (y/N)? ");
			String userInput = scanner.nextLine();
			if (!(userInput.equalsIgnoreCase("y") || userInput.equalsIgnoreCase("yes")))
				overwriteIndex = false;
			scanner = null; // Don't close scanner as System.in will be closes too.
		}
		fileIndexer.createIndex(overwriteIndex);
	}
	
	public void closeDocumentSearcher() {
		docSearcher.close();
	}
}
