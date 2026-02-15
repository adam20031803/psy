package org.example.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneUtil {

    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            var url = SceneUtil.class.getResource(fxmlPath);
            if (url == null) throw new RuntimeException("FXML introuvable: " + fxmlPath);

            Parent root = FXMLLoader.load(url);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
