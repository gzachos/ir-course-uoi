package com.gzachos.ir.gui;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gzachos.ir.SearchEngine;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
	private static MainApp instance = new MainApp();
	private Stage stage;
	private String programName = "WikiSearch 0.1.0 BETA";
	private HostServices hostServices;
	private SearchEngine searchEngine;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			searchEngine = SearchEngine.getInstance();
			this.stage = primaryStage;
			Parent root = FXMLLoader.load(getClass().getResource("MainApp.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setTitle(programName);
			//primaryStage.setResizable(false);
			primaryStage.setMinWidth(1280);
			primaryStage.setMinHeight(720);
			primaryStage.setOnCloseRequest(e -> {
				e.consume();
				exitNormally(stage);
			});
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(
					new Image(MainApp.class.getResourceAsStream("../res/cse-logo.png"))
			);
			hostServices = getHostServices();
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exitNormally(Stage stage) {
		AtomicBoolean exitConfirmed = new AtomicBoolean(false);
		Alert alert = new Alert(AlertType.CONFIRMATION,
				"Are you sure you want to exit WikiSearch?");
		Stage alerstage = (Stage) alert.getDialogPane().getScene().getWindow();
		alerstage.getIcons().add(
				new Image(MainApp.class.getResourceAsStream("../res/cse-logo.png"))
		);
		alert.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK)
				exitConfirmed.set(true);
			else if (response == ButtonType.CANCEL)
				;
		});
		
		if (exitConfirmed.get() == true) {
			searchEngine.closeDocumentSearcher();
			stage.close();
		}
	}
	
	public HostServices getServices() {
		return hostServices;
	}
	
	public static MainApp getInstance() {
		return instance;
	}
}
