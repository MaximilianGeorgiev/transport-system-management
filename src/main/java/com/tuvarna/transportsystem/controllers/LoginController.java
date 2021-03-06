package com.tuvarna.transportsystem.controllers;

import com.tuvarna.transportsystem.services.UserService;
import com.tuvarna.transportsystem.utils.ViewResponse;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

	@FXML
	private Label informationLabel;
	@FXML
	private TextField usernameTextField;
	@FXML
	private PasswordField passwordTextField;

	private static final Logger logger = LogManager.getLogger(LoginController.class.getName());
	private UserService userService;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		userService = new UserService();
		PropertyConfigurator.configure("log4j.properties"); // configure log4j
		logger.info("Log4J successfully configured.");
	}

	public void Change(javafx.event.ActionEvent event) throws IOException {
		Parent userPanel = FXMLLoader.load(getClass().getResource("/views/Register.fxml"));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		logger.info("Register panel loaded.");
	}

	public void loginButtonOnAction(javafx.event.ActionEvent event) throws IOException {
		ViewResponse viewToLoad = userService.login(usernameTextField.getText(), passwordTextField.getText());

		if (userService.userLoginInputIncorrect(viewToLoad.getMessage())) {
			informationLabel.setText("Incorrect login credentials.");
			logger.info("Incorrect login credentials.");
			return;
		}

		// clear error messages
		informationLabel.setText("");

		Parent userPanel = FXMLLoader.load(getClass().getResource(viewToLoad.getMessage()));
		Scene adminScene = new Scene(userPanel);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(adminScene);
		window.show();

		logger.info("Login success.");
	}
}
