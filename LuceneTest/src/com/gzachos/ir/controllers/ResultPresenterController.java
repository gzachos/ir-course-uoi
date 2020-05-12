package com.gzachos.ir.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.lucene.document.Document;

import com.gzachos.ir.Globals;
import com.gzachos.ir.SearchEngine;
import com.gzachos.ir.gui.IfaceDoc;
import com.gzachos.ir.gui.MainApp;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Pagination;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ResultPresenterController implements Initializable {
	private SearchEngine searchEngine;
	@FXML private Pagination pagination;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchEngine = SearchEngine.getInstance();
		pagination.setCurrentPageIndex(0);
		pagination.setMaxPageIndicatorCount(10);
		populatePages();
	}
	
	public void populatePages() {
		ArrayList<Document> docs;
		do {
			docs = searchEngine.getPendingDocHits();
		} while (docs == null);
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
		int numPages = (pageNum > 0) ? pageNum : 1;
		pagination.setPageCount(numPages);
		pagination.setPageFactory(pageIndex -> {
			VBox vbox = new VBox();
			if (pageIndex < ifaceDocs.size()) {
				for (IfaceDoc idoc : ifaceDocs.get(pageIndex)) {
					vbox.getChildren().addAll(idoc.getTitle());
					Hyperlink url = idoc.getUrl();
					url.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							viewArticle(idoc);
						}
					});
					vbox.getChildren().addAll(url);
					vbox.getChildren().addAll(idoc.getSummary());
					vbox.getChildren().addAll(new Separator(Orientation.HORIZONTAL));
				}
			} else if (pageIndex == 0) {
				vbox.getChildren().add(new Text("No results found!"));
			}
			return vbox;
		});
		
		pagination.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.println(event.toString());
			}
		});
	}
	
	
	private void viewArticle(IfaceDoc ifaceDoc) {
		HostServices hostServices = MainApp.getInstance().getHostServices();
		hostServices.showDocument(ifaceDoc.getUrl().getText());
	}
	
}
