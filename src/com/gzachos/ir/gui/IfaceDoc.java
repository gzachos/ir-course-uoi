package com.gzachos.ir.gui;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class IfaceDoc {
	private Hyperlink url;
	private Label title;
	private TextFlow summaryFlow;

	public IfaceDoc(String urlStr, String titleStr, String summaryStr, String highlightStr) {
		url = new Hyperlink(urlStr);
		title = new Label(titleStr);
		summaryFlow = new TextFlow();
		Text summaryText = new Text(summaryStr);
		summaryText.setStyle("-fx-font-size: 12");
		ArrayList<Text> texts = Utils.parseHighlightText(highlightStr);
		texts.add(0, summaryText);
		summaryFlow.getChildren().addAll(texts);
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
		title.setPadding(new Insets(10, 0, 0, 0));
		url.setStyle("-fx-font-size: 13");
		url.setPadding(new Insets(0, 0, 0, 0));
	}

	public Hyperlink getUrl() {
		return url;
	}

	public Label getTitle() {
		return title;
	}

	public TextFlow getSummary() {
		return summaryFlow;
	}

}
