package org.example.controller.fitness;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.fitness.WorkoutDAO;
import org.example.dao.fitness.WorkoutProgressDAO;
import org.example.model.fitness.Workout;
import org.example.model.fitness.WorkoutProgress;
import org.example.util.Session;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Client Workout History: view completed workouts, edit date/notes, or delete
 * entries.
 */
public class WorkoutHistoryController implements Initializable {

    @FXML
    private TableView<WorkoutProgress> historyTable;
    @FXML
    private TableColumn<WorkoutProgress, Integer> histIdCol;
    @FXML
    private TableColumn<WorkoutProgress, String> histWorkoutCol;
    @FXML
    private TableColumn<WorkoutProgress, String> histDateCol;
    @FXML
    private TableColumn<WorkoutProgress, String> histRatingCol;
    @FXML
    private TableColumn<WorkoutProgress, Integer> histStreakCol;
    @FXML
    private TableColumn<WorkoutProgress, String> histNotesCol;

    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> ratingCombo;
    @FXML
    private TextArea notesField;
    @FXML
    private Label totalLabel;

    @FXML
    private Button updateBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button backBtn;

    private final WorkoutProgressDAO progressDAO = new WorkoutProgressDAO();
    private final WorkoutDAO workoutDAO = new WorkoutDAO();
    private final ObservableList<WorkoutProgress> historyList = FXCollections.observableArrayList();
    private List<Workout> allWorkouts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allWorkouts = workoutDAO.readAll();
        setupTableColumns();
        setupButtons();
        loadData();
    }

    private void setupTableColumns() {
        histIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        histWorkoutCol.setCellValueFactory(c -> {
            int wid = c.getValue().getWorkoutId();
            String title = allWorkouts.stream()
                    .filter(w -> w.getId() == wid)
                    .map(Workout::getTitle)
                    .findFirst().orElse("Workout #" + wid);
            return new javafx.beans.property.SimpleStringProperty(title);
        });
        histDateCol.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getCompletedDate();
            return new javafx.beans.property.SimpleStringProperty(
                    d != null ? d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });

        histRatingCol.setCellValueFactory(c -> {
            int r = c.getValue().getRating();
            String label = "➖";
            if (r == 1)
                label = "👎 Dislike";
            if (r == 2)
                label = "👍 Like";
            return new javafx.beans.property.SimpleStringProperty(label);
        });

        histStreakCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getStreakCount()));
        histNotesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));

        setWhiteTextCellFactory(histIdCol);
        setWhiteTextCellFactory(histWorkoutCol);
        setWhiteTextCellFactory(histDateCol);
        setWhiteTextCellFactory(histRatingCol);
        setWhiteTextCellFactory(histStreakCol);
        setWhiteTextCellFactory(histNotesCol);

        historyTable.setItems(historyList);
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null)
                loadIntoForm(sel);
        });
    }

    private <T> void setWhiteTextCellFactory(TableColumn<WorkoutProgress, T> col) {
        col.setCellFactory(column -> new TableCell<WorkoutProgress, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Label label = new Label(item.toString());
                    label.setTextFill(javafx.scene.paint.Color.WHITE);
                    label.setStyle("-fx-font-size: 13px;");
                    setGraphic(label);
                }
            }
        });
    }

    private void setupButtons() {
        updateBtn.setOnAction(e -> updateEntry());
        deleteBtn.setOnAction(e -> deleteEntry());
        clearBtn.setOnAction(e -> clearForm());
        backBtn.setOnAction(e -> goBack());

        if (ratingCombo != null) {
            ratingCombo.getItems().addAll("➖ No Rating", "👎 Dislike", "👍 Like");
        }
    }

    private void loadData() {
        int userId = Session.getUserId();
        List<WorkoutProgress> list = progressDAO.findByUserId(userId);
        historyList.setAll(list);
        if (totalLabel != null)
            totalLabel.setText(String.valueOf(list.size()));
    }

    private void loadIntoForm(WorkoutProgress wp) {
        datePicker.setValue(wp.getCompletedDate());
        notesField.setText(wp.getNotes() != null ? wp.getNotes() : "");

        int r = wp.getRating();
        if (r == 1)
            ratingCombo.setValue("👎 Dislike");
        else if (r == 2)
            ratingCombo.setValue("👍 Like");
        else
            ratingCombo.setValue("➖ No Rating");
    }

    private void clearForm() {
        datePicker.setValue(null);
        notesField.clear();
        ratingCombo.setValue(null);
        historyTable.getSelectionModel().clearSelection();
    }

    private void updateEntry() {
        WorkoutProgress selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Select an entry to update.", Alert.AlertType.WARNING);
            return;
        }
        if (datePicker.getValue() == null) {
            showAlert("Validation", "Please select a date.", Alert.AlertType.WARNING);
            return;
        }
        selected.setCompletedDate(datePicker.getValue());
        selected.setNotes(notesField.getText() != null ? notesField.getText().trim() : "");

        int rating = 0;
        String val = ratingCombo.getValue();
        if (val != null) {
            if (val.contains("Dislike"))
                rating = 1;
            else if (val.contains("Like"))
                rating = 2;
        }
        selected.setRating(rating);

        try {
            progressDAO.update(selected);
            showAlert("Success", "Entry updated!", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to update: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteEntry() {
        WorkoutProgress selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Select an entry to delete.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete this progress entry?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;
        try {
            progressDAO.delete(selected.getId());
            showAlert("Success", "Entry deleted!", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/fitness/fitness_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Fitness Hub");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot go back: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
