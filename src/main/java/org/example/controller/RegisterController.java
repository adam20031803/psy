package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField ageField;
    @FXML private TextField telField;
    @FXML private TextField emailField;
    @FXML private PasswordField pwdField;
    @FXML private PasswordField confirmPwdField;
    @FXML private ComboBox<String> roleBox;
    @FXML private Label messageLabel;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // 🔥 Initialisation de la ComboBox ROLE
    @FXML
    public void initialize() {
        roleBox.getItems().addAll("USER", "ADMIN");
        roleBox.setValue("USER"); // par défaut
    }

    @FXML
    private void onCreateAccount() {

        String nom     = safe(nomField.getText());
        String prenom  = safe(prenomField.getText());
        String ageStr  = safe(ageField.getText());
        String tel     = safe(telField.getText());
        String email   = safe(emailField.getText());
        String pwd     = safe(pwdField.getText());
        String confirm = safe(confirmPwdField.getText());
        String role    = roleBox.getValue(); // 🔥 rôle depuis ComboBox

        if (nom.isEmpty() || prenom.isEmpty() || ageStr.isEmpty() ||
                tel.isEmpty() || email.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            messageLabel.setText("Âge invalide.");
            return;
        }

        if (!pwd.equals(confirm)) {
            messageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            String hash = sha256(pwd);

            // 🔥 SQL AVEC ROLE
            String sql = """
                INSERT INTO `user` (nom, prenom, age, tel, email, pwd, role)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setString(1, nom);
                ps.setString(2, prenom);
                ps.setInt(3, age);
                ps.setString(4, tel);
                ps.setString(5, email);
                ps.setString(6, hash);
                ps.setString(7, role);

                ps.executeUpdate();
            }

            messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            messageLabel.setText("Compte créé avec succès ✅");

        } catch (SQLException ex) {
            ex.printStackTrace();
            String msg = ex.getMessage().toLowerCase();
            if (msg.contains("duplicate") || msg.contains("unique")) {
                messageLabel.setText("Email déjà utilisé.");
            } else {
                messageLabel.setText("Erreur DB (voir console).");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Erreur inattendue.");
        }
    }

    @FXML
    private void onBackToLogin() {
        switchScene("/org/example/ui/login.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            URL url = RegisterController.class.getResource(fxmlPath);
            Objects.requireNonNull(url, "FXML introuvable: " + fxmlPath);

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur navigation.");
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
