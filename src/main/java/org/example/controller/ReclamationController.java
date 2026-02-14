package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.example.model.Reclamation;
import org.example.util.Session;

public class ReclamationController {

    @FXML private TextField sujetField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> prioriteBox;
    @FXML private Label messageLabel;

    // ✅ BON NOM DE BASE
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    @FXML
    public void initialize() {
        prioriteBox.getItems().addAll("BASSE", "NORMALE", "URGENTE");
        prioriteBox.setValue("NORMALE");
    }

    @FXML
    private void onSubmitReclamation() {

        String sujet = sujetField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priorite = prioriteBox.getValue();

        if (sujet.isEmpty() || description.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        int userId = Session.getUserId();

        if (userId <= 0) {
            messageLabel.setText("Utilisateur non connecté.");
            return;
        }

        Reclamation rec = new Reclamation(userId, sujet, description, priorite);

        try {
            String sql =
                    "INSERT INTO reclamation (user_id, sujet, description, priorite) VALUES (?, ?, ?, ?)";

            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setInt(1, rec.getUserId());
                ps.setString(2, rec.getSujet());
                ps.setString(3, rec.getDescription());
                ps.setString(4, rec.getPriorite());

                ps.executeUpdate();
            }

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Réclamation envoyée avec succès ✔");

            sujetField.clear();
            descriptionArea.clear();
            prioriteBox.setValue("NORMALE");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Erreur lors de l'envoi.");
        }
    }
    @FXML
    private void onBack(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/org/example/ui/home.fxml")
            );

            javafx.stage.Stage stage = (javafx.stage.Stage)
                    ((javafx.scene.Node) event.getSource())
                            .getScene()
                            .getWindow();

            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
