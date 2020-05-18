package com.gzachos.ir.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import com.gzachos.ir.Globals;
import com.gzachos.ir.SearchEngine;
import com.gzachos.ir.gui.IfaceRangeQuery;
import com.gzachos.ir.gui.MainApp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AdvancedSearchController implements Initializable {
	private SearchEngine searchEngine;
	
	@FXML private TextField andTextField, orTextField, notTextField, phraseTextField;
	@FXML private CheckBox titleCheckBox, contentCheckBox, multimediaCheckBox,
	                       quotesCheckBox, referencesCheckBox;
	@FXML private ChoiceBox<String> updateChoiceBox, creationChoiceBox;
	@FXML private Button cacnelAdvSearchButton, advSearchButton;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		searchEngine = SearchEngine.getInstance();
		setUpChoiceBoxes();
	}
	
	private void setUpChoiceBoxes() {
		String options[] = {"Anytime", "Past 24h", "Past week", "Past month",
				"Past three months", "Past six months", "Past year"};
		updateChoiceBox.getItems().addAll(options);
		creationChoiceBox.getItems().addAll(options);
		updateChoiceBox.getSelectionModel().select("Anytime");
		creationChoiceBox.getSelectionModel().select("Anytime");
	}
	
	private IfaceRangeQuery getRangeQuery(ChoiceBox<String> cbox) {
		int selectedIndex = cbox.getSelectionModel().getSelectedIndex();
		long toDate = (new Date()).getTime() / 1000; // Time since epoch GMT
		long fromDate;
		long secondsInOneDay = 86400;
		switch (selectedIndex) {
			case 1:
				fromDate = toDate - secondsInOneDay;
				break;
			case 2:
				fromDate = toDate - secondsInOneDay * 7;
				break;
			case 3:
				fromDate = toDate - secondsInOneDay * 30;
				break;
			case 4:
				fromDate = toDate - secondsInOneDay * 90;
				break;
			case 5:
				fromDate = toDate - secondsInOneDay * 180;
				break;
			case 6:
				fromDate = toDate - secondsInOneDay * 365;
				break;
			default:
				return null;
		}
		String fieldName = (cbox == updateChoiceBox) ?
				      Globals.UPDATE_TIME_FIELD_NAME :
				  Globals.PUBLICATION_TIME_FIELD_NAME;
		return new IfaceRangeQuery(fieldName, fromDate, toDate);
	}
	
	private ArrayList<IfaceRangeQuery> getRangeQueries() {
		ArrayList<IfaceRangeQuery> queries = new ArrayList<IfaceRangeQuery>();
		IfaceRangeQuery query = getRangeQuery(creationChoiceBox);
		if (query != null)
			queries.add(query);
		query = getRangeQuery(updateChoiceBox);
		if (query != null)
			queries.add(query);
		return queries;
	}
	
	@FXML
	private void conductAdvancedSearch() {
		String queryStr = generateQueryStr();
		if (queryStr == null) {
			warnUser("You can't leave all text fields empty!", "Missing User Input");
			return;
		}
		String fields[] = getFieldsToSearch();
		ArrayList<IfaceRangeQuery> rangeQueries = getRangeQueries();
		String res = searchEngine.searchForAdvanced(queryStr, rangeQueries, 5, fields,
				Globals.DEFAULT_QUERY_BOOSTS);
		if (res != null)
			warnUser(res, "Search Error");
		else
			invokeResultPresenter();
	}
	
	public void invokeResultPresenter() {
		try{
			FXMLLoader fxmlLoader = new FXMLLoader(
					getClass().getResource("../gui/ResultPresenter.fxml")
			);
			Parent root = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setMinWidth(1280);
			stage.setMinHeight(720); // TODO verify value
			stage.setOnCloseRequest(e -> searchEngine.clearCurrentQuery());
			stage.setTitle(MainApp.getAppNameAndVersion());
			stage.getIcons().add(
					new Image(MainAppController.class.getResourceAsStream("../res/cse-logo.png"))
			);
			stage.setScene(new Scene(root));
			stage.show();
			root.requestFocus();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void cancelAdvancedSearch() {
		Stage stage = (Stage) andTextField.getScene().getWindow();
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}
	
	private String generateQueryStr() {
		String queryStr = "";
		// TODO handle multiple whitespace in phrase(s)
		String tmpStr = andTextField.getText().strip();
		if (tmpStr.length() > 0)
			queryStr = "+" + escape(tmpStr).replaceAll(" +", " +");
		tmpStr = phraseTextField.getText().strip();
		if (tmpStr.length() > 0)
			queryStr += " +\"" + tmpStr + "\"";
		tmpStr = orTextField.getText().strip();
		if (tmpStr.length() > 0)
			queryStr += " " + escape(tmpStr).replaceAll(" +", " OR ");
		tmpStr = notTextField.getText().strip();
		if (tmpStr.length() > 0)
			queryStr += " -" + escape(tmpStr).replaceAll(" +", " -");
		if (queryStr.strip().length() == 0)
			return null;
		return queryStr;
	}
	
	private String escape(String str) {
		String newStr = "";
		String tokens[] = str.split(" +");
		for (String token : tokens) {
			newStr += "\"" + token + "\" ";
		}
		return newStr.strip();
	}
	
	private String[] getFieldsToSearch() {
		ArrayList<String> fields = new ArrayList<String>();
		
		if (titleCheckBox.isSelected())
			fields.add(Globals.TITLE_FIELD_NAME);
		if (contentCheckBox.isSelected())
			fields.add(Globals.CONTENT_FIELD_NAME);
		if (multimediaCheckBox.isSelected())
			fields.add(Globals.MULTIMEDIA_FIELD_NAME);
		if (quotesCheckBox.isSelected())
			fields.add(Globals.QUOTES_FIELD_NAME);
		if (referencesCheckBox.isSelected())
			fields.add(Globals.REFERENCES_FIELD_NAME);
		String[] fs = fields.toArray(new String[0]);
		return fs;
	}
	
	private void warnUser(String warningText, String headerText) {
		Alert warn = new Alert(AlertType.WARNING, warningText);
		Stage warnstage = (Stage) warn.getDialogPane().getScene().getWindow();
		warnstage.getIcons().add(
				new Image(getClass().getResourceAsStream("../res/cse-logo.png"))
		);
		warn.setHeaderText(headerText);
		warn.showAndWait();
	}

}
