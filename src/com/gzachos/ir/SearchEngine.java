package com.gzachos.ir;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import com.gzachos.ir.gui.IfaceRangeQuery;
import com.gzachos.ir.gui.IfaceSearchResult;
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
	private IfaceSearchResult pendingSearchResults;
	
	private SearchEngine() {
		 fileIndexer = new FileIndexer(Config.CORPUS_PATH, Config.INDEX_PATH);
		 createIndex(); // Index should be created before DocumentSearcher!
		 docSearcher = new DocumentSearcher(Config.INDEX_PATH);
		 queryInfos = new Stack<QueryInfo>();
		 pendingSearchResults = null;
		 overwriteIndex = false;
	}
	
	public static SearchEngine getInstance() {
		if (instance == null)
			instance = new SearchEngine();
		return instance;
	}
	
	public String searchFor(String queryStr, int numPages) {
		int sortOption = Globals.DEFAULT_SORT_OPTION;
		Query query = docSearcher.parseQuery(queryStr);
		if (query == null) 
			return Globals.QUERY_PARSE_ERROR;
		SearchResult searchResult = docSearcher.executeQuery(query, numPages, sortOption, null);
		if (searchResult == null)
			return Globals.QUERY_EXEC_ERROR;
		currentQueryInfo = new QueryInfo(queryStr, query, sortOption, searchResult);
		pendingSearchResults = new IfaceSearchResult();
		pendingSearchResults.setDocs(docSearcher.getDocuments(searchResult));
		pendingSearchResults.setHighlights(searchResult.getHighlights());
		return null;
	}
	
	public String searchForAdvanced(String queryStr, ArrayList<IfaceRangeQuery> rangeQueries,
			int numPages, String fields[], Map<String, Float> boosts, int sortOption) {
		ArrayList<Query> queries = new ArrayList<Query>();
		Query query = docSearcher.parseAdvancedQuery(queryStr, fields, boosts);
		if (query == null) 
			return Globals.QUERY_PARSE_ERROR;
		queries.add(query);
		for (IfaceRangeQuery rq : rangeQueries) {
			queries.add(docSearcher.buildRangeQuery(
					rq.getField(),
					rq.getLowerBound(),
					rq.getUpperBound())
			);
		}
		Query finalQuery = docSearcher.combineMultipleQueries(queries);
		SearchResult searchResult = docSearcher.executeQuery(finalQuery, numPages, sortOption, null);
		if (searchResult == null)
			return Globals.QUERY_EXEC_ERROR;
		currentQueryInfo = new QueryInfo(queryStr, query, sortOption, searchResult);
		pendingSearchResults = new IfaceSearchResult();
		pendingSearchResults.setDocs(docSearcher.getDocuments(searchResult));
		pendingSearchResults.setHighlights(searchResult.getHighlights());
		return null;
	}
	
	public IfaceSearchResult getPendingSearchResults() {
		if (currentQueryInfo == null || pendingSearchResults == null)
			return null;
		IfaceSearchResult tmpSearchRes = pendingSearchResults;
		pendingSearchResults = null;
		return tmpSearchRes;
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
	
	public IfaceSearchResult searchAfter(int numPages) {
		if (currentQueryInfo == null)
			return null;
		System.out.println("searchAfter - " + currentQueryInfo.getSortOption());
		SearchResult searchResult = docSearcher.executeQuery(
				currentQueryInfo.getQuery(),
				numPages,
				currentQueryInfo.getSortOption(),
				currentQueryInfo.getSearchResult()
		);
		// In order to keep lastReturnedDoc up-to-date.
		currentQueryInfo.appendToSearchResult(searchResult);
		IfaceSearchResult res = new IfaceSearchResult();
		res.setDocs(docSearcher.getDocuments(searchResult));
		res.setHighlights(searchResult.getHighlights());
		return res;
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
	
	public int getCorpusSize() {
		return docSearcher.getCorpusSize();
	}
}
