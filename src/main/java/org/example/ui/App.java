package org.example.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/ui/login.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Application");
        stage.show();
        stage.setMaximized(true);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
