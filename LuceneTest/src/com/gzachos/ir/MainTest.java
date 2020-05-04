package com.gzachos.ir;

import com.gzachos.ir.lucene.IndexFiles;
import com.gzachos.ir.lucene.SearchFiles;

public class MainTest {

	public static void main(String[] args) {
		boolean overwriteIndex = false;
		IndexFiles.createIndex(overwriteIndex);
		SearchFiles.searchFiles();
		System.out.println("\nTerminated...");
	}

}
