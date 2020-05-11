package com.gzachos.ir.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.gzachos.ir.SearchEngine;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainAppController implements Initializable {
	private static MainAppController mainAppController = null;
	private SearchEngine searchEngine;
	
	@FXML private VBox mainAppVBox;
	@FXML private TextField mainSearchArea;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchEngine = SearchEngine.getInstance();
		
		mainSearchArea.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCharacter().equals("\r")) {
					String queryStr = mainSearchArea.getText();
					searchEngine.searchFor(queryStr);
				}
			}
		});
	}
	
	public static MainAppController getInstance() {
		if (mainAppController == null)
			mainAppController = new MainAppController();
		return mainAppController;
	}
	
	@FXML
	private void showAboutApp() {
		Alert info = new Alert(AlertType.INFORMATION, "WikiSearch"
				+ "\nVersion: 0.1.0\n" + "License: GPLv2\n\n"
				+ "Developed by George Z. Zachos and\n"
				+ "Andrew Konstantinidis for the Information\n"
				+ "Retrieval course @cse.uoi.gr\n"
				+ "Instructor: Evaggelia Pitoura"
		);
		Stage stage = (Stage) info.getDialogPane().getScene().getWindow();
		stage.getIcons().add(
				new Image(MainAppController.class.getResourceAsStream("../res/cse-logo.png"))
		);
		info.setHeaderText("About WikiSearch");
		info.showAndWait();
	}
	
	@FXML
	private void advancedSearch() {
		System.out.println("Advanced Search");
	}
	
	@FXML
	private void exitNormally() {
		System.out.println("Quit");
		Stage stage = (Stage) mainAppVBox.getScene().getWindow();
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

}
