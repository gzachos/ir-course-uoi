package com.gzachos.ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;

import com.gzachos.ir.gui.IfaceRangeQuery;
import com.gzachos.ir.gui.IfaceSearchResult;
import com.gzachos.ir.gui.QuerySpellSuggestion;
import com.gzachos.ir.gui.Utils;
import com.gzachos.ir.lucene.DocumentSearcher;
import com.gzachos.ir.lucene.FileIndexer;
import com.gzachos.ir.lucene.QueryInfo;
import com.gzachos.ir.lucene.SearchResult;
import com.gzachos.ir.lucene.SpellingChecker;

public class SearchEngine {
	private static SearchEngine instance = null;

	boolean overwriteIndex;
	private FileIndexer fileIndexer;
	private SpellingChecker spellingChecker;
	private DocumentSearcher docSearcher;
	private Stack<QueryInfo> queryInfos;
	private QueryInfo currentQueryInfo;
	private IfaceSearchResult pendingSearchResults;

	private SearchEngine() {
		fileIndexer = new FileIndexer(Config.CORPUS_PATH, Config.INDEX_PATH);
		createIndex(); // Index should be created before DocumentSearcher!
		spellingChecker = new SpellingChecker(Config.INDEX_PATH, Config.SPELL_INDEX_PATH);
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

	public String searchFor(String queryStr, int numPages, boolean spellChecked) {
		int sortOption = Globals.DEFAULT_SORT_OPTION;
		Query query = docSearcher.parseQuery(queryStr, spellChecked);
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

	public String searchForAdvanced(String queryStr, ArrayList<IfaceRangeQuery> rangeQueries, int numPages,
			String fields[], Map<String, Float> boosts, int sortOption) {
		ArrayList<Query> queries = new ArrayList<Query>();
		Query query = docSearcher.parseAdvancedQuery(queryStr, fields, boosts);
		if (query == null)
			return Globals.QUERY_PARSE_ERROR;
		queries.add(query);
		for (IfaceRangeQuery rq : rangeQueries) {
			queries.add(docSearcher.buildRangeQuery(rq.getField(), rq.getLowerBound(), rq.getUpperBound()));
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
		SearchResult searchResult = docSearcher.executeQuery(currentQueryInfo.getQuery(), numPages,
				currentQueryInfo.getSortOption(), currentQueryInfo.getSearchResult());
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

	public ArrayList<String> getSuggestions(String term, String field) {
		return spellingChecker.getSuggestions(term, field);
	}

	public ArrayList<QuerySpellSuggestion> getQuerySuggestions(Query query) {
		ArrayList<QuerySpellSuggestion> querySuggestions = new ArrayList<QuerySpellSuggestion>();
		ArrayList<String> suggestions;
		QuerySpellSuggestion newSuggestion;
		HashSet<Term> termsSet = new HashSet<Term>();
		QueryVisitor queryVisitor = QueryVisitor.termCollector(termsSet);
		query.visit(queryVisitor);
		String queryStr = query.toString();
		for (Term term : termsSet) {
			String newQueryStr = queryStr;
			String fieldTerm = term.toString();
			String tokens[] = fieldTerm.split(":");
			String field = tokens[0];
			String termStr = tokens[1];
			suggestions = spellingChecker.getSuggestions(termStr, field);
			if (suggestions.size() >= 1) {
				String newTerm = suggestions.get(0);
				for (String tmpField : Globals.DOCUMENT_FIELDS)
					newQueryStr = newQueryStr.replaceAll(tmpField + ":" + termStr,
							tmpField + ":" + newTerm);
				newSuggestion = new QuerySpellSuggestion(newQueryStr, newTerm);
				if (!Utils.alreadySuggested(querySuggestions, newSuggestion))
					querySuggestions.add(new QuerySpellSuggestion(newQueryStr, newTerm));
			}
		}
		return querySuggestions;
	}

	public ArrayList<QuerySpellSuggestion> getCurrentQuerySuggestions() {
		if (currentQueryInfo == null)
			return null;
		return getQuerySuggestions(currentQueryInfo.getQuery());
	}

}
