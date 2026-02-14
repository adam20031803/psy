package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.example.model.Reclamation;
import org.example.util.Session;

public class ReclamationController {

    @FXML private TextField sujetField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> prioriteBox;
    @FXML private Label messageLabel;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // ✅ Patterns
    // Sujet: commence par une lettre/chiffre, autorise lettres/chiffres/espace/'-.,?!
    private static final Pattern SUJET_PATTERN =
            Pattern.compile("^[A-Za-zÀ-ÿ0-9][A-Za-zÀ-ÿ0-9\\s'\\-.,!?]{3,79}$"); // 4..80

    @FXML
    public void initialize() {
        prioriteBox.getItems().addAll("BASSE", "NORMALE", "URGENTE");
        prioriteBox.setValue("NORMALE");

        // ✅ Limites de longueur
        sujetField.setTextFormatter(maxLenTF(80));
        descriptionArea.setTextFormatter(maxLenTA(1000));
    }

    @FXML
    private void onSubmitReclamation() {

        setMsg("", false);

        String sujet = safe(sujetField.getText());
        String description = safe(descriptionArea.getText());
        String priorite = prioriteBox.getValue();

        // 1) user connecté
        int userId = Session.getUserId();
        if (userId <= 0) {
            setMsg("Utilisateur non connecté.", false);
            return;
        }

        // 2) priorité
        if (priorite == null || !(priorite.equals("BASSE") || priorite.equals("NORMALE") || priorite.equals("URGENTE"))) {
            setMsg("Veuillez choisir une priorité.", false);
            prioriteBox.requestFocus();
            return;
        }

        // 3) sujet
        if (sujet.isEmpty()) {
            setMsg("Le sujet est obligatoire.", false);
            sujetField.requestFocus();
            return;
        }
        if (sujet.length() < 4) {
            setMsg("Sujet trop court (min 4 caractères).", false);
            sujetField.requestFocus();
            return;
        }
        if (!SUJET_PATTERN.matcher(sujet).matches()) {
            setMsg("Sujet invalide (évitez les symboles seulement).", false);
            sujetField.requestFocus();
            return;
        }

        // 4) description
        if (description.isEmpty()) {
            setMsg("La description est obligatoire.", false);
            descriptionArea.requestFocus();
            return;
        }
        if (description.length() < 10) {
            setMsg("Description trop courte (min 10 caractères).", false);
            descriptionArea.requestFocus();
            return;
        }
        if (description.length() > 1000) {
            setMsg("Description trop longue (max 1000 caractères).", false);
            descriptionArea.requestFocus();
            return;
        }

        // ✅ insert DB
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

            setMsg("Réclamation envoyée avec succès ✔", true);

            sujetField.clear();
            descriptionArea.clear();
            prioriteBox.setValue("NORMALE");

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erreur lors de l'envoi.", false);
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

    // -------- Helpers --------

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void setMsg(String text, boolean ok) {
        if (ok) {
            messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
        messageLabel.setText(text);
    }

    private javafx.scene.control.TextFormatter<String> maxLenTF(int max) {
        UnaryOperator<javafx.scene.control.TextFormatter.Change> filter =
                c -> c.getControlNewText().length() <= max ? c : null;
        return new javafx.scene.control.TextFormatter<>(filter);
    }

    private javafx.scene.control.TextFormatter<String> maxLenTA(int max) {
        UnaryOperator<javafx.scene.control.TextFormatter.Change> filter =
                c -> c.getControlNewText().length() <= max ? c : null;
        return new javafx.scene.control.TextFormatter<>(filter);
    }
}
