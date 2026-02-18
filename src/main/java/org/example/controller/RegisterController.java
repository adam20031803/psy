package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField ageField;
    @FXML
    private TextField telField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField pwdField;
    @FXML
    private PasswordField confirmPwdField;
    @FXML
    private ComboBox<String> roleBox;
    @FXML
    private Label messageLabel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // ✅ Regex / Patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ\\s'\\-]{1,29}$"); // 2..30
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_TN_PATTERN = Pattern.compile("^\\d{8}$"); // Tunisie
    private static final Pattern STRONG_PWD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("USER", "ADMIN");
        roleBox.setValue("USER");

        // ✅ Limiteurs + filtres (optionnel mais très utile)
        setMaxLen(nomField, 30);
        setMaxLen(prenomField, 30);
        setMaxLen(emailField, 80);
        setMaxLen(telField, 15);

        // âge : chiffres seulement
        ageField.setTextFormatter(digitsOnlyMaxLen(3));

        // tel : chiffres + plus (au début) seulement
        telField.setTextFormatter(phoneFormatter());

        // mdp : pas d'espaces
        pwdField.setTextFormatter(noSpacesMaxLen(60));
        confirmPwdField.setTextFormatter(noSpacesMaxLen(60));
    }

    @FXML
    private void onCreateAccount() {

        // Reset style message (à chaque tentative)
        setMsg("", false);

        String nom = safe(nomField.getText());
        String prenom = safe(prenomField.getText());
        String ageStr = safe(ageField.getText());
        String tel = safe(telField.getText());
        String email = safe(emailField.getText()).toLowerCase();
        String pwd = safe(pwdField.getText());
        String confirm = safe(confirmPwdField.getText());
        String role = roleBox.getValue();

        // 1) champs vides
        if (nom.isEmpty() || prenom.isEmpty() || ageStr.isEmpty() ||
                tel.isEmpty() || email.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
            setMsg("Veuillez remplir tous les champs.", false);
            return;
        }

        // 2) nom/prénom
        if (!NAME_PATTERN.matcher(nom).matches()) {
            setMsg("Nom invalide (2-30 lettres, espaces, - ou ').", false);
            nomField.requestFocus();
            return;
        }
        if (!NAME_PATTERN.matcher(prenom).matches()) {
            setMsg("Prénom invalide (2-30 lettres, espaces, - ou ').", false);
            prenomField.requestFocus();
            return;
        }

        // 3) âge
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            setMsg("Âge invalide.", false);
            ageField.requestFocus();
            return;
        }
        if (age < 10 || age > 120) {
            setMsg("Âge doit être entre 10 et 120.", false);
            ageField.requestFocus();
            return;
        }

        // 4) téléphone
        // 👉 ici je valide TN 8 chiffres. Si tu veux accepter +216xxxxxxxx aussi,
        // dis-moi.
        if (!PHONE_TN_PATTERN.matcher(tel).matches()) {
            setMsg("Téléphone invalide (8 chiffres).", false);
            telField.requestFocus();
            return;
        }

        // 5) email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            setMsg("Email invalide.", false);
            emailField.requestFocus();
            return;
        }

        // 6) mot de passe
        if (!STRONG_PWD_PATTERN.matcher(pwd).matches()) {
            setMsg("Mot de passe faible: min 8, 1 maj, 1 min, 1 chiffre.", false);
            pwdField.requestFocus();
            return;
        }

        // 7) confirmation
        if (!pwd.equals(confirm)) {
            setMsg("Les mots de passe ne correspondent pas.", false);
            confirmPwdField.requestFocus();
            return;
        }

        // 8) rôle
        if (role == null || (!role.equals("USER") && !role.equals("ADMIN"))) {
            setMsg("Rôle invalide.", false);
            roleBox.requestFocus();
            return;
        }

        // ✅ si tout est OK : insert DB
        try {
            String hash = sha256(pwd);

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

            setMsg("Compte créé avec succès ✅", true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("duplicate") || msg.contains("unique")) {
                setMsg("Email déjà utilisé.", false);
            } else {
                setMsg("Erreur DB (voir console).", false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setMsg("Erreur inattendue.", false);
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
            setMsg("Erreur navigation.", false);
        }
    }

    // -------- Helpers UI --------

    private void setMsg(String text, boolean ok) {
        if (ok) {
            messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
        messageLabel.setText(text);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void setMaxLen(TextField tf, int max) {
        tf.setTextFormatter(new TextFormatter<String>(
                (TextFormatter.Change c) -> c.getControlNewText().length() <= max ? c : null));
    }

    private TextFormatter<String> digitsOnlyMaxLen(int maxLen) {
        UnaryOperator<TextFormatter.Change> filter = c -> {
            String t = c.getControlNewText();
            return t.matches("\\d{0," + maxLen + "}") ? c : null;
        };
        return new TextFormatter<>(filter);
    }

    private TextFormatter<String> phoneFormatter() {
        UnaryOperator<TextFormatter.Change> filter = c -> {
            String t = c.getControlNewText();
            // autorise: "" ou "+" au début puis chiffres, max 15
            if (t.isEmpty())
                return c;
            if (t.length() > 15)
                return null;
            if (t.startsWith("+"))
                return t.substring(1).matches("\\d*") ? c : null;
            return t.matches("\\d*") ? c : null;
        };
        return new TextFormatter<>(filter);
    }

    private TextFormatter<String> noSpacesMaxLen(int maxLen) {
        UnaryOperator<TextFormatter.Change> filter = c -> {
            String t = c.getControlNewText();
            if (t.length() > maxLen)
                return null;
            return t.contains(" ") ? null : c;
        };
        return new TextFormatter<>(filter);
    }
}
