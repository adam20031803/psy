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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.example.model.Reclamation;

public class ReclamationListController {

    // ====== TABLE ======
    @FXML private TableView<Reclamation> table;
    @FXML private TableColumn<Reclamation, Number> colId;
    @FXML private TableColumn<Reclamation, Number> colUserId;
    @FXML private TableColumn<Reclamation, String> colSujet;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, String> colPriorite;

    // ====== FORM (EDIT) ======
    @FXML private TextField sujetField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> prioriteBox;
    @FXML private ComboBox<String> statutBox;

    @FXML private Label messageLabel;

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    @FXML
    public void initialize() {

        // Columns
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colUserId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getUserId()));
        colSujet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSujet()));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colPriorite.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriorite()));
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()));

        // ComboBoxes
        prioriteBox.getItems().setAll("BASSE", "NORMALE", "URGENTE");
        prioriteBox.setValue("NORMALE");

        statutBox.getItems().setAll("EN_ATTENTE", "TRAITEE", "REFUSEE");
        statutBox.setValue("EN_ATTENTE");

        // When user selects a row -> fill form
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                sujetField.setText(newV.getSujet());
                descriptionArea.setText(newV.getDescription());
                prioriteBox.setValue(newV.getPriorite());
                statutBox.setValue(newV.getStatut());
                setMsg("Sélection: réclamation #" + newV.getId(), true);
            }
        });

        Platform.runLater(this::loadReclamations);
    }

    // ====== READ ======
    private void loadReclamations() {
        ObservableList<Reclamation> list = FXCollections.observableArrayList();

        String sql = "SELECT id, user_id, sujet, description, priorite, statut FROM reclamation ORDER BY id DESC";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
            table.refresh();

            if (list.isEmpty()) setMsg("Aucune réclamation.", false);
            else setMsg("Total: " + list.size() + " réclamations.", true);

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erreur chargement: " + e.getMessage(), false);
        }
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        Reclamation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMsg("Sélectionne une réclamation dans la table.", false);
            return;
        }

        String sujet = safe(sujetField.getText());
        String description = safe(descriptionArea.getText());
        String priorite = prioriteBox.getValue();
        String statut = statutBox.getValue();

        if (sujet.length() < 3) { setMsg("Sujet trop court.", false); return; }
        if (description.length() < 5) { setMsg("Description trop courte.", false); return; }
        if (priorite == null) { setMsg("Choisis une priorité.", false); return; }
        if (statut == null) { setMsg("Choisis un statut.", false); return; }

        System.out.println("UPDATE id=" + selected.getId()
                + " sujet=" + sujet + " priorite=" + priorite + " statut=" + statut);

        String sql = "UPDATE reclamation SET sujet=?, description=?, priorite=?, statut=? WHERE id=?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, sujet);
            ps.setString(2, description);
            ps.setString(3, priorite);
            ps.setString(4, statut);
            ps.setInt(5, selected.getId());

            int rows = ps.executeUpdate();
            System.out.println("rows affected=" + rows);

            if (rows > 0) {
                setMsg("Réclamation mise à jour ✅", true);

                // ✅ الأفضل: reload من DB (يضمن التحديث في TableView حتى بدون setters)
                loadReclamations();

                // optional: reselect نفس الid بعد reload
                selectRowById(selected.getId());
            } else {
                setMsg("Update échoué: id introuvable / 0 rows.", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erreur update: " + e.getMessage(), false);
        }
    }

    // ====== DELETE ======
    @FXML
    private void onDelete(ActionEvent event) {
        Reclamation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMsg("Sélectionne une réclamation à supprimer.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la réclamation #" + selected.getId() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String sql = "DELETE FROM reclamation WHERE id=?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                table.getItems().remove(selected);
                clearForm();
                setMsg("Réclamation supprimée ✅", true);
            } else {
                setMsg("Suppression échouée (id introuvable).", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setMsg("Erreur delete: " + e.getMessage(), false);
        }
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        loadReclamations();
    }

    private void clearForm() {
        sujetField.clear();
        descriptionArea.clear();
        prioriteBox.setValue("NORMALE");
        statutBox.setValue("EN_ATTENTE");
        table.getSelectionModel().clearSelection();
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

    @FXML
    private void onBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectRowById(int id) {
        for (Reclamation r : table.getItems()) {
            if (r.getId() == id) {
                table.getSelectionModel().select(r);
                table.scrollTo(r);
                break;
            }
        }
    }
}
