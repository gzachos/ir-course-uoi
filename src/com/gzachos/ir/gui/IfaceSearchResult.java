package com.gzachos.ir.gui;

import java.util.ArrayList;

import org.apache.lucene.document.Document;

public class IfaceSearchResult {
	private ArrayList<Document> docs;
	private ArrayList<String> highlights;

	public IfaceSearchResult() {
	}

	public ArrayList<Document> getDocs() {
		return docs;
	}

	public void setDocs(ArrayList<Document> docs) {
		this.docs = docs;
	}

	public ArrayList<String> getHighlights() {
		return highlights;
	}

	public void setHighlights(ArrayList<String> highlights) {
		this.highlights = highlights;
	}

}
