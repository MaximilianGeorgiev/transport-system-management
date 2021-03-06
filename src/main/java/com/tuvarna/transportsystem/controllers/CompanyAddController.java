package com.tuvarna.transportsystem.controllers;

import com.tuvarna.transportsystem.entities.*;
import com.tuvarna.transportsystem.services.*;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.ValidationUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.ResourceBundle;

public class CompanyAddController implements Initializable {

	ObservableList<String> list = FXCollections.observableArrayList();
	@FXML
	private ChoiceBox<String> routeChoiceBox;
	@FXML
	private ChoiceBox<String> timeChoiceBox;

	@FXML
	private ComboBox<String> attachmentComboBox;
	@FXML
	private ToggleGroup radioBusType;
	@FXML
	private RadioButton radioBigBus;
	@FXML
	private RadioButton radioVan;
	@FXML
	private Label informationLabel;
	@FXML
	private DatePicker departureDatePicker;
	@FXML
	private TextField seatsCapacityTextField;
	@FXML
	private DatePicker arrivalDatePicker;
	@FXML
	private ChoiceBox<String> restrictionChoiceBox;
	@FXML
	private TextField ticketsQuantityTextField;
	@FXML
	private ToggleGroup radioTypeTrip;
	@FXML
	private RadioButton radioTypeExpress;
	@FXML
	private RadioButton radioTypeNormal;
	@FXML
	private TextField durationTextField;
	@FXML
	private TextField priceTextField;
	@FXML
	private TextField ticketsAvailabilityTextField;
	@FXML
	private ChoiceBox<String> departureChoiceBox;
	@FXML
	private ChoiceBox<String> arrivalChoiceBox;

	Route globalRoute;

	private static final Logger logger = LogManager.getLogger(CompanyAddController.class.getName());
	private CompanyService companyService;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		companyService = new CompanyService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
		logger.info("Log4J successfully configured.");

		// loadRoutes();
		loadTime();
		loadRestrictionQuantity();
		// loadAttachmentLocations();

		departureChoiceBox.setItems(getLocation());
		arrivalChoiceBox.setItems(getLocation());
		logger.info("Loaded times, purchase restrictions, locations.");
	}

	private ObservableList<String> getLocation() {
		ObservableList<String> locationList = FXCollections.observableArrayList();
		LocationService locationService = new LocationService();
		/*
		 * Loading locations from data base.
		 */
		List<Location> eList = locationService.getAll();
		for (Location ent : eList) {
			locationList.add(ent.getLocationName());
		}
		return locationList;
	}

	private void loadTime() {
		list.removeAll(list);
		String time_01 = "00:00";
		String time_02 = "00:30";
		String time_03 = "02:00";
		String time_04 = "04:30";
		String time_05 = "06:00";
		String time_06 = "07:30";
		String time_07 = "08:00";
		String time_08 = "09:30";
		String time_09 = "10:45";
		String time_10 = "11:30";
		String time_11 = "12:15";
		String time_12 = "13:30";
		String time_13 = "14:00";
		String time_14 = "15:30";
		String time_15 = "17:00";
		String time_16 = "17:30";
		String time_17 = "18:00";
		String time_18 = "18:15";
		String time_19 = "19:00";
		String time_20 = "20:30";
		String time_21 = "21:15";
		String time_22 = "22:05";
		String time_23 = "22:30";
		String time_24 = "23:30";
		String time_25 = "23:45";

		list.addAll(time_01, time_02, time_03, time_04, time_05, time_06, time_07, time_08, time_09, time_10, time_11,
				time_12, time_13, time_14, time_15, time_16, time_17, time_18, time_19, time_20, time_21, time_22,
				time_23, time_24, time_25);
		timeChoiceBox.getItems().addAll(list);
	}

	private void loadRestrictionQuantity() {
		list.removeAll(list);
		String number_01 = ("1");
		String number_02 = ("2");
		String number_03 = ("3");
		String number_04 = ("4");
		String number_05 = ("5");
		String number_06 = ("6");
		String number_07 = ("7");
		String number_08 = ("8");
		String number_09 = ("9");
		String number_10 = ("10");

		list.addAll(number_01, number_02, number_03, number_04, number_05, number_06, number_07, number_08, number_09,
				number_10);
		restrictionChoiceBox.getItems().addAll(list);

	}

	public void goToScheduleCompany(javafx.event.ActionEvent event) throws IOException {
		Parent userPanel = FXMLLoader.load(getClass().getResource("/views/CompanySchedulePanel.fxml"));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		logger.info("Schedule view loaded.");
	}

	public void goToNotifications(javafx.event.ActionEvent event) throws IOException {
		Stage stage = new Stage();
		FXMLLoader userPanel = new FXMLLoader(getClass().getResource("/views/CompanyNotificationsPanel.fxml"));
		AnchorPane root = (AnchorPane) userPanel.load();
		Scene adminScene = new Scene(root);
		stage.setScene(adminScene);
		stage.setTitle("Transport Company");
		stage.showAndWait();

		logger.info("Notifications view loaded.");
	}

	public void goToRequest(javafx.event.ActionEvent event) throws IOException {
		Parent userPanel = FXMLLoader.load(getClass().getResource("/views/CompanyRequestPanel.fxml"));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		logger.info("Requests view loaded.");
	}

	public void backToLogIn(javafx.event.ActionEvent event) throws IOException {
		Parent userPanel = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		DatabaseUtils.currentUser = null;
		logger.info("User logged out.");
	}

	public void addTrip() throws IOException, ParseException {
		if (globalRoute == null) {
			informationLabel.setText("Add stations for the trip first!");
			return;
		}

		ViewResponse response = companyService.addTrip(globalRoute, ticketsAvailabilityTextField.getText(),
				seatsCapacityTextField.getText(), durationTextField.getText(), priceTextField.getText(),
				departureDatePicker.getEditor(), arrivalDatePicker.getEditor(),
				restrictionChoiceBox.getSelectionModel().getSelectedItem(),
				timeChoiceBox.getSelectionModel().getSelectedItem(),
				departureChoiceBox.getSelectionModel().getSelectedItem(),
				arrivalChoiceBox.getSelectionModel().getSelectedItem(), radioTypeTrip.getSelectedToggle(),
				radioBusType.getSelectedToggle());

		if (!response.isValid()) {
			informationLabel.setText(response.getMessage());
			return;
		}

		informationLabel.setText("Trip successfully created.");
	}

	public void addStations(javafx.event.ActionEvent event) throws IOException {		
		ViewResponse response = ValidationUtils.validateCompanyAddAttachmentLocationsEndPoint(
				departureChoiceBox.getSelectionModel().getSelectedItem(),
				arrivalChoiceBox.getSelectionModel().getSelectedItem());

		if (!response.isValid()) {
			informationLabel.setText(response.getMessage()); // display error message
			return;
		}
		
		globalRoute = companyService.createGlobalTrip(departureChoiceBox.getSelectionModel().getSelectedItem(),
				arrivalChoiceBox.getSelectionModel().getSelectedItem());
		
		companyService.addTripHandleAttachmentLocations(arrivalChoiceBox.getSelectionModel().getSelectedItem(),
				departureChoiceBox.getSelectionModel().getSelectedItem(), globalRoute);
	}
}
