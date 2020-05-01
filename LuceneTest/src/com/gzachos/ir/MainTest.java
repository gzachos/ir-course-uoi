package com.gzachos.ir;

import com.gzachos.ir.lucene.IndexFiles;

public class MainTest {

	public static void main(String[] args) {
		boolean overwriteIndex = false;
		
		IndexFiles.createIndex(overwriteIndex);
	}

}
