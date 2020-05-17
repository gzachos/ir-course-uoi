package com.gzachos.ir.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class IfaceDoc {
	Hyperlink url;
	Label title;
	Text summary;
	
	public IfaceDoc(String urlStr, String titleStr, String summaryStr) {
		url = new Hyperlink(urlStr);
		title = new Label(titleStr);
		summary = new Text(summaryStr);
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
		title.setPadding(new Insets(10, 0, 0, 0));
		url.setStyle("-fx-font-size: 13");
		url.setPadding(new Insets(0, 0, 0, 0));
		summary.setStyle("-fx-font-size: 12");
	}
	
	public Hyperlink getUrl() {
		return url;
	}

	public Label getTitle() {
		return title;
	}

	public Text getSummary() {
		return summary;
	}
	
}
