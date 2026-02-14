package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

// 🔥 IMPORTS MANQUANTS (CAUSE DU cannot find symbol)
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.example.model.Reclamation;

public class ReclamationListController {

    @FXML private TableView<Reclamation> table;

    // 🔹 Colonnes numériques
    @FXML private TableColumn<Reclamation, Number> colId;
    @FXML private TableColumn<Reclamation, Number> colUserId;

    // 🔹 Colonnes texte
    @FXML private TableColumn<Reclamation, String> colSujet;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, String> colPriorite;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    @FXML
    public void initialize() {

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getId())
        );

        colUserId.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getUserId())
        );

        colSujet.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSujet())
        );

        colDescription.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getDescription())
        );

        colStatut.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatut())
        );

        colPriorite.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getPriorite())
        );

        loadReclamations();
    }

    private void loadReclamations() {

        ObservableList<Reclamation> list = FXCollections.observableArrayList();

        String sql = "SELECT * FROM reclamation WHERE user_id = ?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Reclamation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("sujet"),
                        rs.getString("description"),
                        rs.getString("priorite"),
                        rs.getString("statut")
                ));

            }

            table.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/example/ui/home.fxml")
            );

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
