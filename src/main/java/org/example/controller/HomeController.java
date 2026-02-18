package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node; // Correct import
import org.example.util.Session;

import java.io.IOException;

public class HomeController {

    @FXML
    public void onLogout(ActionEvent event) {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onGoToCreateReclamation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/reclamation.fxml", "Réclamations");
    }

    @FXML
    public void onGoToFitness(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/fitness/fitness_dashboard.fxml", "Fitness Hub");
    }

    @FXML
    public void onGoToMotivation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/motivation/MainView.fxml",
                "Motivation");
    }
}
