package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.util.Session;

import java.io.IOException;

public class GestionController {

    @FXML
    public void logout(ActionEvent event) {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
            System.out.println("Déconnexion réussie - Redirection vers Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
