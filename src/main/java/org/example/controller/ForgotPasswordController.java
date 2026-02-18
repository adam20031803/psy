package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.example.service.PasswordResetService;
import org.example.controller.SceneUtil;

public class ForgotPasswordController {

    @FXML private TextField emailField;

    private final PasswordResetService resetService = new PasswordResetService();

    @FXML
    private void onSendCode() {
        try {
            String email = emailField.getText().trim();
            resetService.createAndSendCode(email);

            ResetSession.email = email;

            new Alert(Alert.AlertType.INFORMATION, "Code envoyé ✅").showAndWait();

            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneUtil.switchTo(stage, "/org/example/ui/VerifyCode.fxml", "Vérifier Code");

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void backToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/login.fxml", "Login");
    }
}
