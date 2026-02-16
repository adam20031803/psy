package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.example.service.PasswordResetService;
import org.example.controller.SceneUtil;

public class VerifyCodeController {

    @FXML private TextField codeField;

    private final PasswordResetService resetService = new PasswordResetService();

    @FXML
    private void onVerifyCode() {
        try {
            String email = ResetSession.email;
            String code = codeField.getText().trim();

            boolean ok = resetService.verifyCode(email, code);
            if (!ok) {
                new Alert(Alert.AlertType.ERROR, "Code incorrect / expiré / trop de tentatives").showAndWait();
                return;
            }

            new Alert(Alert.AlertType.INFORMATION, "Code صحيح ✅").showAndWait();

            Stage stage = (Stage) codeField.getScene().getWindow();
            SceneUtil.switchTo(stage, "/org/example/ui/NewPassword.fxml", "Nouveau mot de passe");

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void backToForgot() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        SceneUtil.switchTo(stage, "/org/example/ui/ForgotPassword.fxml", "Mot de passe oublié");
    }
}
