package com.tuvarna.transportsystem.main;
import com.tuvarna.transportsystem.utils.DatabaseUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends javafx.application.Application {
	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("views/Login.fxml"));
		stage.setTitle("Transport Company");

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);

		// primaryStage.setScene(new Scene(root, 380, 500));
		stage.show();
	}

	public static void main(String[] args) {
		DatabaseUtils.init();
		launch(args);
	}
}
