package com.gzachos.ir;

import java.util.Scanner;

import com.gzachos.ir.lucene.IndexFiles;
import com.gzachos.ir.lucene.SearchFiles;

public class MainTest {

	public static void main(String[] args) {
		boolean overwriteIndex = false;
		
		if (overwriteIndex) {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			System.out.print("About to overwrite existing index. Are you sure (y/N)? ");
			String userInput = scanner.nextLine();
			if (!(userInput.equalsIgnoreCase("y") || userInput.equalsIgnoreCase("yes")))
				overwriteIndex = false;
			scanner = null; // Don't close scanner as System.in will be closes too.
		}
		IndexFiles.createIndex(overwriteIndex);
		SearchFiles.searchFiles();
		System.out.println("\nTerminated...");
	}

}
