package com.gzachos.ir.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.lucene.document.Document;

import com.gzachos.ir.Globals;
import com.gzachos.ir.SearchEngine;
import com.gzachos.ir.gui.IfaceDoc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Pagination;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ResultPresenterController implements Initializable {

	private SearchEngine searchEngine;
	
	@FXML private Pagination pagination;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchEngine = SearchEngine.getInstance();
		populatePages();
	}
	
	public void populatePages() {
		ArrayList<Document> docs;
		do {
			docs = searchEngine.getPendingDocHits();
		} while (docs == null);
		int numPages =  (int) Math.nextUp(docs.size() / Globals.HITS_PER_PAGE);
		pagination.setPageCount(numPages);
		pagination.setCurrentPageIndex(0);
		pagination.setMaxPageIndicatorCount(10);
		ArrayList<ArrayList<IfaceDoc>> ifaceDocs = new ArrayList<ArrayList<IfaceDoc>>();

		int pageNum = 0;
		for (int i = 0; i < docs.size(); i++) {
			if (i % Globals.HITS_PER_PAGE == 0) {
				ifaceDocs.add(new ArrayList<IfaceDoc>());
				pageNum++;
			}
			Document doc = docs.get(i);
			ifaceDocs.get(pageNum-1).add(new IfaceDoc(doc.get("url"), doc.get("title"), doc.get("summary")));
		}
		pagination.setPageCount((pageNum > 0) ? pageNum : 1);
		pagination.setPageFactory(pageIndex -> {
			VBox vbox = new VBox();
			if (pageIndex < ifaceDocs.size()) {
				for (IfaceDoc idoc : ifaceDocs.get(pageIndex)) {
					vbox.getChildren().addAll(idoc.getTitle());
					vbox.getChildren().addAll(idoc.getUrl());
					vbox.getChildren().addAll(idoc.getSummary());
					vbox.getChildren().addAll(new Separator(Orientation.HORIZONTAL));
				}
			} else if (pageIndex == 0) {
					vbox.getChildren().add(new Text("No results found!"));
			}
			return vbox;
		});
	}
	
	

}
