package org.example.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.example.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserController {

    // ===== TABLE =====
    @FXML private TableView<User> userTable;

    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, Integer> colAge;
    @FXML private TableColumn<User, String> colTel;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPwd;
    @FXML private TableColumn<User, String> colRole;

    // ===== FORM (EDIT) =====
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField ageField;
    @FXML private TextField telField;
    @FXML private TextField emailField;
    @FXML private PasswordField pwdField;
    @FXML private ComboBox<String> roleBox;

    @FXML private Label totalUsersLabel;
    @FXML private Label messageLabel;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    @FXML
    public void initialize() {

        // ✅ bind columns
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        colAge.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getAge()).asObject());
        colTel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTel()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPwd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPwd()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));

        // ✅ role choices
        roleBox.getItems().setAll("USER", "ADMIN");
        roleBox.setValue("USER");

        // ✅ when select row -> fill form
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                nomField.setText(newV.getNom());
                prenomField.setText(newV.getPrenom());
                ageField.setText(String.valueOf(newV.getAge()));
                telField.setText(newV.getTel());
                emailField.setText(newV.getEmail());
                pwdField.setText(newV.getPwd());
                roleBox.setValue(newV.getRole());
                setMsg("Sélection: user #" + newV.getId(), true);
            }
        });

        Platform.runLater(() -> {
            loadUsers();
            countUsers();
        });
    }

    // ===== READ =====
    private void loadUsers() {
        ObservableList<User> list = FXCollections.observableArrayList();
        String sql = "SELECT id, nom, prenom, age, tel, email, pwd, role FROM user ORDER BY id DESC";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getInt("age"),
                        rs.getString("tel"),
                        rs.getString("email"),
                        rs.getString("pwd"),
                        rs.getString("role")
                ));
            }

            userTable.setItems(list);
            if (list.isEmpty()) setMsg("Aucun utilisateur.", false);
            else setMsg("", true);

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erreur chargement: " + e.getMessage(), false);
        }
    }

    private void countUsers() {
        String sql = "SELECT COUNT(*) FROM user";
        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) totalUsersLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== BUTTONS =====
    @FXML
    private void onRefresh(ActionEvent e) {
        loadUsers();
        countUsers();
        setMsg("Données rafraîchies ✅", true);
    }

    @FXML
    private void onUpdate(ActionEvent e) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMsg("Sélectionne un utilisateur dans la table.", false);
            return;
        }

        String nom = safe(nomField.getText());
        String prenom = safe(prenomField.getText());
        String ageStr = safe(ageField.getText());
        String tel = safe(telField.getText());
        String email = safe(emailField.getText());
        String pwd = safe(pwdField.getText());
        String role = roleBox.getValue();

        // ✅ simple validation
        if (nom.isEmpty() || prenom.isEmpty() || ageStr.isEmpty() || tel.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            setMsg("Veuillez remplir tous les champs.", false);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (Exception ex) {
            setMsg("Âge invalide.", false);
            return;
        }

        if (age < 0 || age > 120) { setMsg("Âge doit être entre 0 et 120.", false); return; }
        if (!email.contains("@") || !email.contains(".")) { setMsg("Email invalide.", false); return; }
        if (!tel.matches("\\d{8,15}")) { setMsg("Tel invalide (8-15 chiffres).", false); return; }
        if (role == null || !(role.equals("USER") || role.equals("ADMIN"))) { setMsg("Rôle invalide.", false); return; }

        String sql = "UPDATE user SET nom=?, prenom=?, age=?, tel=?, email=?, pwd=?, role=? WHERE id=?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setInt(3, age);
            ps.setString(4, tel);
            ps.setString(5, email);
            ps.setString(6, pwd);
            ps.setString(7, role);
            ps.setInt(8, selected.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                setMsg("Utilisateur mis à jour ✅", true);
                loadUsers();
                countUsers();
                selectRowById(selected.getId());
            } else {
                setMsg("Update échoué (id introuvable).", false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setMsg("Erreur update: " + ex.getMessage(), false);
        }
    }

    @FXML
    private void onDelete(ActionEvent e) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMsg("Sélectionne un utilisateur.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer user #" + selected.getId() + " ?",
                ButtonType.OK, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String sql = "DELETE FROM user WHERE id=?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                setMsg("Utilisateur supprimé ✅", true);
                loadUsers();
                countUsers();
                clearForm();
            } else {
                setMsg("Suppression échouée.", false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setMsg("Erreur delete: " + ex.getMessage(), false);
        }
    }

    // ✅ for your FXML "Retour"
    @FXML
    private void onBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erreur navigation.", false);
        }
    }

    // ===== helpers =====
    private void selectRowById(int id) {
        for (User u : userTable.getItems()) {
            if (u.getId() == id) {
                userTable.getSelectionModel().select(u);
                userTable.scrollTo(u);
                break;
            }
        }
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        ageField.clear();
        telField.clear();
        emailField.clear();
        pwdField.clear();
        roleBox.setValue("USER");
        userTable.getSelectionModel().clearSelection();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void setMsg(String text, boolean ok) {
        if (messageLabel == null) return;
        messageLabel.setStyle(ok
                ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        messageLabel.setText(text == null ? "" : text);
    }
}
