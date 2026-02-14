package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

import org.example.util.Session;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    @FXML
    private void onLogin() {

        String email = emailField.getText().trim();
        String pass  = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Remplir email et mot de passe");
            return;
        }

        try {
            String pwdHash = sha256(pass);

            // 🔥 ON RÉCUPÈRE ID + ROLE
            String sql = "SELECT id, role FROM user WHERE email = ? AND pwd = ?";

            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, pwdHash);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    int userId = rs.getInt("id");
                    String role = rs.getString("role");

                    // 🔥 ENREGISTRER SESSION
                    Session.set(userId, role);

                    // 🔁 REDIRECTION SELON ROLE
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        goTo("/org/example/ui/gestion.fxml");   // backend
                    } else {
                        goTo("/org/example/ui/home.fxml");      // frontend
                    }

                } else {
                    messageLabel.setText("Email ou mot de passe incorrect");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur login");
        }
    }

    @FXML
    private void onRegister() {
        try {
            goTo("/org/example/ui/register.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur navigation");
        }
    }

    @FXML
    private void onForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/ForgotPassword.fxml")); // عدّل المسار حسب مشروعك
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mot de passe oublié");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur: ForgotPassword.fxml introuvable");
        }
    }

    private void goTo(String fxml) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
