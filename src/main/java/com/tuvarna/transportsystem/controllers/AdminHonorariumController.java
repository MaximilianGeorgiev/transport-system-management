package com.tuvarna.transportsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tuvarna.transportsystem.services.AdminService;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import com.tuvarna.transportsystem.utils.ViewResponse;

public class AdminHonorariumController implements Initializable {

	@FXML
	private TextField userNameTextField;

	@FXML
	private TextField userHonorariumTextField;

	@FXML
	private TextField userRatingTextField;

	@FXML
	private Label informationLabel;

	private static final Logger logger = LogManager.getLogger(AdminHonorariumController.class.getName());
	private AdminService adminService;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		adminService = new AdminService();

		PropertyConfigurator.configure("log4j.properties"); // configure log4j
		logger.info("Log4J successfully configured.");
	}

	public void goBack(javafx.event.ActionEvent event) throws IOException {
		Parent userPanel = FXMLLoader.load(getClass().getResource("/views/AdminPanel.fxml"));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		logger.info("Returned to admin panel.");
	}

	public void backToLogIn(javafx.event.ActionEvent event) throws IOException {
		Parent userPanel = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		DatabaseUtils.currentUser = null;
		logger.info("User logged off.");
	}

	public void editOnClick(javafx.event.ActionEvent event) throws IOException {
		ViewResponse response = adminService.editHonorarium(userNameTextField.getText(),
				userHonorariumTextField.getText(), userRatingTextField.getText());
		
		// in case somehow the message was never initialized
		if (response.getMessage().equals(null) || response.getMessage().equals("") || response.getMessage().length() == 0) {
			return;
		}

		informationLabel.setText(response.getMessage()); // display error / constraint check failure / success
	}
}
