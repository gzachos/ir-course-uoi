package com.gzachos.ir.gui;

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
		title.setStyle("-fx-font-weight: bold");
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
