package com.gzachos.ir.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.gzachos.ir.SearchEngine;
import com.gzachos.ir.gui.MainApp;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainAppController implements Initializable {
	private SearchEngine searchEngine;

	@FXML
	private VBox mainAppVBox;
	@FXML
	private TextField mainSearchArea;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchEngine = SearchEngine.getInstance();

		String corpusSizeStr = String.valueOf(searchEngine.getCorpusSize());
		mainSearchArea.setPromptText("Search among " + corpusSizeStr + " Wikipedia articles");

		mainSearchArea.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCharacter().equals("\r")) {
					String queryStr = mainSearchArea.getText();
					if (queryStr.strip().length() == 0) {
						warnUser("Search for at least one term!", "Missing User Input");
						return;
					}
					String res = searchEngine.searchFor(queryStr, 5, false);
					if (res != null)
						warnUser(res, "Search Error");
					else
						invokeResultPresenter();
				}
			}
		});
	}

	public void invokeResultPresenter() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../gui/ResultPresenter.fxml"));
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setMinWidth(1280);
			stage.setMinHeight(720); // TODO verify value
			stage.setOnCloseRequest(e -> searchEngine.clearCurrentQuery());
			stage.setTitle(MainApp.getAppNameAndVersion());
			stage.getIcons().add(
					new Image(MainAppController.class.getResourceAsStream("../res/cse-logo.png")));
			stage.setScene(new Scene(root));
			stage.show();
			root.requestFocus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void showAboutApp() {
		Alert info = new Alert(AlertType.INFORMATION,
				MainApp.getAppName() + "\nVersion: " + MainApp.getAppVersion() + "\n"
						+ "License: GPLv2\n\n" + "Developed by George Z. Zachos and\n"
						+ "Andrew Konstantinidis for the Information\n"
						+ "Retrieval course @cse.uoi.gr\n" + "Instructor: Evaggelia Pitoura");
		Stage stage = (Stage) info.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(MainAppController.class.getResourceAsStream("../res/cse-logo.png")));
		info.setHeaderText("About " + MainApp.getAppName());
		info.showAndWait();
	}

	@FXML
	private void invokeAdvancedSearch() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../gui/AdvancedSearch.fxml"));
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.setOnCloseRequest(e -> searchEngine.clearCurrentQuery());
			stage.setTitle(MainApp.getAppNameAndVersion());
			stage.getIcons().add(
					new Image(MainAppController.class.getResourceAsStream("../res/cse-logo.png")));
			stage.setScene(new Scene(root));
			stage.show();
			root.requestFocus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void exitNormally() {
		Stage stage = (Stage) mainAppVBox.getScene().getWindow();
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	private void warnUser(String warningText, String headerText) {
		Alert warn = new Alert(AlertType.WARNING, warningText);
		Stage warnstage = (Stage) warn.getDialogPane().getScene().getWindow();
		warnstage.getIcons().add(new Image(getClass().getResourceAsStream("../res/cse-logo.png")));
		warn.setHeaderText(headerText);
		warn.showAndWait();
	}

}
