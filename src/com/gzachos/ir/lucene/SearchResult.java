package com.gzachos.ir.lucene;

import java.util.ArrayList;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TotalHits.Relation;

public class SearchResult {
	private ArrayList<ScoreDoc> scoreDocs;
	private ArrayList<String> highlights;
	private double searchTimeSec;
	private int numTotalHits;
	private Relation relation;
	private boolean isPartialResult;

	public SearchResult(ArrayList<ScoreDoc> scoreDocs, ArrayList<String> highlights, double searchTimeSec,
			int numTotalHits, Relation relation, boolean isPartialResult) {
		this.scoreDocs = scoreDocs;
		this.highlights = highlights;
		this.searchTimeSec = searchTimeSec;
		this.numTotalHits = numTotalHits;
		this.relation = relation;
		this.isPartialResult = isPartialResult;
	}

	public ArrayList<ScoreDoc> getHits() {
		return scoreDocs;
	}

	public ArrayList<String> getHighlights() {
		return highlights;
	}

	public int getNumHits() {
		return scoreDocs.size();
	}

	public boolean isPartialSearchResult() {
		return isPartialResult;
	}

	public String getStatsStr() {
		if (getNumHits() == 0)
			return "No results found!";
		String relationStr = (relation == Relation.GREATER_THAN_OR_EQUAL_TO) ? "More than" : "About";
		return relationStr + " " + numTotalHits + " results (" + searchTimeSec + " seconds)\n";
	}

}
