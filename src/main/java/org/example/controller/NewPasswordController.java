package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import org.example.service.PasswordResetService;
import org.example.controller.SceneUtil;

public class NewPasswordController {

    @FXML private PasswordField newPwdField;
    @FXML private PasswordField confirmPwdField;

    private final PasswordResetService resetService = new PasswordResetService();

    @FXML
    private void onChangePassword() {
        try {
            String p1 = newPwdField.getText();
            String p2 = confirmPwdField.getText();

            if (!p1.equals(p2)) {
                new Alert(Alert.AlertType.WARNING, "Les mots de passe ne sont pas identiques.").showAndWait();
                return;
            }

            resetService.updatePasswordAndClearReset(ResetSession.email, p1);

            new Alert(Alert.AlertType.INFORMATION, "Mot de passe modifié ✅").showAndWait();

            Stage stage = (Stage) newPwdField.getScene().getWindow();
            SceneUtil.switchTo(stage, "/org/example/ui/login.fxml", "Login");

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
}
