package com.gzachos.ir.gui;

public class QuerySpellSuggestion {
	private String query;
	private String newTerm;
	
	public QuerySpellSuggestion(String query, String newTerm) {
		this.query = query;
		this.newTerm = newTerm;
	}

	public String getQueryStr() {
		return query;
	}

	public String getNewTerm() {
		return newTerm;
	}
	
	public boolean equals(QuerySpellSuggestion other) {
		return this.query.equals(other.getQueryStr()) &&
				this.newTerm.equals(other.getNewTerm());
	}
}
