package com.gzachos.ir;

import java.util.Scanner;
import java.util.Stack;

import org.apache.lucene.search.Query;

import com.gzachos.ir.lucene.DocumentSearcher;
import com.gzachos.ir.lucene.FileIndexer;
import com.gzachos.ir.lucene.SearchResult;

public class SearchEngine {
	private static SearchEngine instance = null;
	
	boolean overwriteIndex;
	private FileIndexer fileIndexer;
	private DocumentSearcher docSearcher;
	private Stack<Query> queries;
	
	private SearchEngine() {
		 fileIndexer = new FileIndexer(Config.INDEX_PATH);
		 docSearcher = new DocumentSearcher(Config.INDEX_PATH);
		 queries = new Stack<Query>();
		 overwriteIndex = false;
		 createIndex();
	}
	
	public static SearchEngine getInstance() {
		if (instance == null)
			instance = new SearchEngine();
		return instance;
	}
	
	public void searchFor(String queryStr) {
		Query query = docSearcher.executeQuery(queryStr);
		queries.push(query);
		SearchResult result0 = docSearcher.executeQuery(query, 5, null);
		docSearcher.printReturnedDocs(result0);
		SearchResult result1 = docSearcher.executeQuery(query, 5, result0);
		docSearcher.printReturnedDocs(result1);
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
}
