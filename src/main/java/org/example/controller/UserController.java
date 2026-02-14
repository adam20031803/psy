package org.example.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.stage.Stage
        ;

import org.example.model.User;

import java.sql.*;

public class UserController {

    @FXML private TableView<User> userTable;

    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, Integer> colAge;
    @FXML private TableColumn<User, String> colTel;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPwd;
    @FXML private Label totalUsersLabel;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    @FXML

    public void initialize() {

        userTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colAge.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAge()).asObject());
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTel()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colPwd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPwd()));

        userTable.setEditable(true);
        colNom.setCellFactory(TextFieldTableCell.forTableColumn());
        colPrenom.setCellFactory(TextFieldTableCell.forTableColumn());
        colTel.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmail.setCellFactory(TextFieldTableCell.forTableColumn());
        colPwd.setCellFactory(TextFieldTableCell.forTableColumn());
        colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        colNom.setOnEditCommit(e ->
                updateField(e.getRowValue().getId(), "nom", e.getNewValue()));

        colPrenom.setOnEditCommit(e ->
                updateField(e.getRowValue().getId(), "prenom", e.getNewValue()));

        colAge.setOnEditCommit(e ->
                updateField(e.getRowValue().getId(), "age", e.getNewValue()));

        colTel.setOnEditCommit(e ->
                updateField(e.getRowValue().getId(), "tel", e.getNewValue()));

        colEmail.setOnEditCommit(e ->
                updateField(e.getRowValue().getId(), "email", e.getNewValue()));

        colPwd.setOnEditCommit(e ->
                updateField(e.getRowValue().getId(), "pwd", e.getNewValue()));

        loadUsers();
        countUsers();   // 🔥 AJOUTE JUSTE CETTE LIGNE
    }

    private void loadUsers() {

        ObservableList<User> list = FXCollections.observableArrayList();

        String sql = "SELECT * FROM user";

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateField(int id, String column, Object value) {

        String sql = "UPDATE user SET " + column + " = ? WHERE id = ?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setObject(1, value);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Modification enregistrée !");
            } else {
                System.out.println("Aucune ligne modifiée !");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteUser() {

        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String sql = "DELETE FROM user WHERE id=?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            ps.executeUpdate();

            loadUsers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml")); // صحّح المسار
            Parent root = loader.load();

            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
    private void countUsers() {

        String sql = "SELECT COUNT(*) FROM user";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt(1);
                totalUsersLabel.setText(String.valueOf(total));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
