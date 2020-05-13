package com.gzachos.ir.lucene;

import org.apache.lucene.search.Query;

public class QueryInfo {
	private String queryStr;
	private Query query;
	private SearchResult searchResult;
	
	public QueryInfo(String queryStr, Query query, SearchResult searchResult) {
		this.setQueryStr(queryStr);
		this.setQuery(query);
		this.setSearchResult(searchResult);
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(SearchResult searchResult) {
		this.searchResult = searchResult;
	}

	public String getQueryStr() {
		return queryStr;
	}

	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}
	
	public void appendToSearchResult(SearchResult searchResult) {
		this.searchResult.getHits().addAll(searchResult.getHits());
	}
	
}
