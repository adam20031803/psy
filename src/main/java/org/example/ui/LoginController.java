package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.controller.SceneUtil;
import org.example.service.UserService;
import org.example.util.Session;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for login.fxml. Handles login, register and forgot password
 * navigation.
 */
public class LoginController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button loginButton;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (loginButton != null) {
            loginButton.setOnAction(e -> onLogin());
        }
    }

    public void onLogin() {
        try {
            if (messageLabel != null)
                messageLabel.setText("");
            if (emailField == null || passwordField == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Champs non chargés. Redémarrez l'application.");
                return;
            }
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pwd = passwordField.getText() == null ? "" : passwordField.getText();
            if (email.isEmpty() || pwd.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez entrer l'email et le mot de passe.");
                return;
            }
            var user = userService.login(email, pwd);
            if (user != null) {
                Session.set(user.getId(), user.getRole() != null ? user.getRole() : "USER");
                Stage stage = (Stage) emailField.getScene().getWindow();
                try {
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        SceneUtil.switchTo(stage, "/org/example/ui/gestion.fxml", "Administration");
                    } else {
                        SceneUtil.switchTo(stage, "/org/example/ui/Home.fxml", "Accueil");
                    }
                } catch (Exception ex) {
                    StringBuilder sb = new StringBuilder("Impossible d'ouvrir l'application:\n");
                    Throwable t = ex;
                    while (t != null) {
                        sb.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
                        t = t.getCause();
                    }
                    showAlert(Alert.AlertType.ERROR, "Erreur", sb.toString());
                    ex.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Connexion refusée",
                        "Email ou mot de passe incorrect. Vérifiez que le compte test@example.com existe (exécutez setup_psy_database.sql dans phpMyAdmin).");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Connexion base de données: " + e.getMessage()
                    + "\n\nVérifiez que MySQL (XAMPP) est démarré et que la base 'psy' existe.");
        }
    }

    @FXML
    public void onRegister() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/register.fxml", "Créer un compte");
    }

    @FXML
    public void onForgotPassword() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/ForgotPassword.fxml", "Mot de passe oublié");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
