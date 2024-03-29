package com.gzachos.ir.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.lucene.document.Document;

import com.gzachos.ir.Globals;
import com.gzachos.ir.SearchEngine;
import com.gzachos.ir.gui.IfaceDoc;
import com.gzachos.ir.gui.IfaceSearchResult;
import com.gzachos.ir.gui.MainApp;
import com.gzachos.ir.gui.QuerySpellSuggestion;
import com.gzachos.ir.gui.Utils;

import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResultPresenterController implements Initializable {
	private SearchEngine searchEngine;
	private ArrayList<ArrayList<IfaceDoc>> totalIfaceDocs;
	private int savedPageIndex;
	private boolean spellChecked = false;

	@FXML
	private Pagination pagination;
	private HBox spellSuggestionsHBox = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initResultPresenter();
	}

	private void initResultPresenter() {
		searchEngine = SearchEngine.getInstance();
		savedPageIndex = 0;
		pagination.setMaxPageIndicatorCount(5);

		if (getSearchResults(false))
			populatePages();

		pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldPageNum,
					Number newPageNum) {
				if (newPageNum.intValue() == totalIfaceDocs.size() - 1) {
					if (getSearchResults(true)) {
						savedPageIndex = pagination.getCurrentPageIndex();
						populatePages();
					}
				}
			}
		});
	}

	private void resetResultPresenter() {
		spellSuggestionsHBox = null;
	}

	private void createSpellSuggestionsBox(ArrayList<QuerySpellSuggestion> suggestions) {
		if (suggestions == null || suggestions.size() == 0 || spellChecked)
			return;
		spellSuggestionsHBox = new HBox(8);
		spellSuggestionsHBox.setPadding(new Insets(0, 5, 0, 5));
		ArrayList<Node> nodes = new ArrayList<Node>();
		Label didYouMeanText = new Label("Did you mean: ");
		didYouMeanText.setPadding(new Insets(5, 0, 5, 5));
		nodes.add(didYouMeanText);
		for (QuerySpellSuggestion suggestion : suggestions) {
			Button termButton = new Button(suggestion.getNewTerm());
			nodes.add(termButton);
			termButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent ae) {
					String newTerm = termButton.getText();
					QuerySpellSuggestion suggestedQuery = Utils.getSuggestionByTerm(suggestions,
							newTerm);
					String queryStr = suggestedQuery.getQueryStr();
					spellChecked = true;
					searchEngine.searchFor(queryStr, 5, spellChecked);
					resetResultPresenter();
					initResultPresenter();
				}
			});
		}
		spellSuggestionsHBox.getChildren().addAll(nodes);
	}

	private boolean getSearchResults(boolean isPartialSearch) {
		IfaceSearchResult searchResults;
		ArrayList<Document> docs;
		ArrayList<String> highlights;
		if (isPartialSearch) {
			if ((searchResults = searchEngine.searchAfter(5)) == null)
				return false; // No need to populate pages
		} else {
			do {
				searchResults = searchEngine.getPendingSearchResults();
			} while (searchResults == null);
			createSpellSuggestionsBox(searchEngine.getCurrentQuerySuggestions());
		}
		docs = searchResults.getDocs();
		highlights = searchResults.getHighlights();
		ArrayList<ArrayList<IfaceDoc>> ifaceDocs = new ArrayList<ArrayList<IfaceDoc>>();

		boolean haveHighlights = (highlights != null && highlights.size() == docs.size());

		int pageNum = 0;
		for (int i = 0; i < docs.size(); i++) {
			if (i % Globals.HITS_PER_PAGE == 0) {
				ifaceDocs.add(new ArrayList<IfaceDoc>());
				pageNum++;
			}
			Document doc = docs.get(i);
			ifaceDocs.get(pageNum - 1).add(new IfaceDoc(doc.get(Globals.URL_FIELD_NAME),
					doc.get(Globals.TITLE_FIELD_NAME), doc.get(Globals.SUMMARY_FIELD_NAME),
					(haveHighlights) ? highlights.get(i) : null));
		}
		int numPages = pageNum;
		if (isPartialSearch) {
			if (numPages == 0)
				return false; // No need to populate pages
			totalIfaceDocs.addAll(ifaceDocs);
		} else {
			totalIfaceDocs = ifaceDocs;
		}
		return true;
	}

	public void populatePages() {
		pagination.setPageFactory(pageIndex -> {
			ScrollPane scrollPane = new ScrollPane();
			VBox vbox = new VBox();
			vbox.setPadding(new Insets(10, 25, 25, 25));
			scrollPane.setContent(vbox);
			Label statsLabel = new Label();
			if (pageIndex == 0) {
				String statsString = searchEngine.getCurrentQueryStats();
				statsLabel.setText(statsString);
				statsLabel.setPadding(new Insets(10, 10, 15, 10));
				statsLabel.setStyle("-fx-font-size: 13");
				vbox.getChildren().add(statsLabel);
				if (spellSuggestionsHBox != null)
					vbox.getChildren().add(spellSuggestionsHBox);
			}
			if (totalIfaceDocs.size() == 0) {
				statsLabel.setStyle("-fx-font-weight: bold");
				return vbox;
			}
			if (pageIndex < totalIfaceDocs.size()) {
				for (IfaceDoc idoc : totalIfaceDocs.get(pageIndex)) {
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
			} else {
				return null;
			}
			return scrollPane;
		});
		int totalNumPages = totalIfaceDocs.size();
		pagination.setPageCount((totalNumPages == 0) ? 1 : totalNumPages);
		pagination.setCurrentPageIndex(savedPageIndex);
	}

	private void viewArticle(IfaceDoc ifaceDoc) {
		HostServices hostServices = MainApp.getInstance().getHostServices();
		hostServices.showDocument(ifaceDoc.getUrl().getText());
	}

}
