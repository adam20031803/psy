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
import org.example.model.fitness.Workout;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Workout CRUD screen: TableView, form, Add/Edit/Delete/Clear.
 * Follows same logic style as existing motivation controllers.
 */
public class WorkoutController implements Initializable {

    @FXML
    private TableView<Workout> workoutTable;
    @FXML
    private Label workoutCountLabel;
    @FXML
    private Label strengthCountLabel;
    @FXML
    private Label cardioCountLabel;
    @FXML
    private Label flexibilityCountLabel;
    @FXML
    private TableColumn<Workout, Integer> idColumn;
    @FXML
    private TableColumn<Workout, String> titleColumn;
    @FXML
    private TableColumn<Workout, String> categoryColumn;
    @FXML
    private TableColumn<Workout, Integer> durationColumn;
    @FXML
    private TableColumn<Workout, String> difficultyColumn;
    @FXML
    private TableColumn<Workout, String> createdAtColumn;

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> categoryField;
    @FXML
    private TextField durationField;
    @FXML
    private ComboBox<String> difficultyField;

    @FXML
    private Button addBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button dashboardBtn;

    private final ObservableList<Workout> workoutList = FXCollections.observableArrayList();
    private final WorkoutDAO workoutDAO = new WorkoutDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!org.example.util.Session.isCoach()) {
            javafx.application.Platform.runLater(() -> {
                showAlert("Access Denied", "Only coaches can manage workouts.", Alert.AlertType.ERROR);
                goToDashboard();
            });
            return;
        }

        setupTableColumns();
        setupFormChoices();
        setupButtons();
        loadData();
    }

    private void setupTableColumns() {
        // 1. Set up Value Factories (Data Binding) using Lambdas to avoid reflection
        // issues
        idColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        titleColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTitle()));
        categoryColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategory()));
        durationColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getDurationMinutes()));
        difficultyColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDifficultyLevel()));

        createdAtColumn.setCellValueFactory(cell -> {
            LocalDateTime dt = cell.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                    dt != null ? dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
        });

        // 2. Set up Cell Factories (Visual Rendering) to ensure white text
        setWhiteTextCellFactory(idColumn);
        setWhiteTextCellFactory(titleColumn);
        setWhiteTextCellFactory(categoryColumn);
        setWhiteTextCellFactory(durationColumn);
        setWhiteTextCellFactory(difficultyColumn);
        setWhiteTextCellFactory(createdAtColumn);

        // 3. Bind list and selection listener
        workoutTable.setItems(workoutList);
        workoutTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                loadWorkoutIntoForm(newVal);
        });
    }

    /**
     * Helper to force visible text on table cells using setGraphic (bypasses CSS
     * overrides on setText).
     */
    private <T> void setWhiteTextCellFactory(TableColumn<Workout, T> col) {
        col.setCellFactory(column -> new TableCell<Workout, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null); // Clear default text rendering
                    Label label = new Label(item.toString());
                    label.setTextFill(javafx.scene.paint.Color.WHITE);
                    label.setStyle("-fx-font-size: 13px;");
                    setGraphic(label);
                }
            }
        });
    }

    private void setupFormChoices() {
        categoryField.getItems().addAll("strength", "cardio", "mobility", "flexibility", "balance", "other");
        difficultyField.getItems().addAll("easy", "medium", "hard");
        categoryField.setValue("strength");
        difficultyField.setValue("medium");
    }

    private void setupButtons() {
        addBtn.setOnAction(e -> addWorkout());
        updateBtn.setOnAction(e -> updateWorkout());
        deleteBtn.setOnAction(e -> deleteWorkout());
        clearBtn.setOnAction(e -> clearForm());
        dashboardBtn.setOnAction(e -> goToDashboard());
    }

    private void loadData() {
        List<Workout> list = workoutDAO.readAll();
        workoutList.setAll(list);

        // Update total count
        if (workoutCountLabel != null) {
            workoutCountLabel.setText(String.valueOf(list.size()));
        }

        // Update category counts
        int strength = 0;
        int cardio = 0;
        int flex = 0;

        for (Workout w : list) {
            String c = w.getCategory() == null ? "" : w.getCategory().toLowerCase();
            if (c.equals("strength")) {
                strength++;
            } else if (c.equals("cardio")) {
                cardio++;
            } else if (c.equals("flexibility") || c.equals("mobility") || c.equals("balance")) {
                flex++;
            }
        }

        if (strengthCountLabel != null)
            strengthCountLabel.setText(String.valueOf(strength));
        if (cardioCountLabel != null)
            cardioCountLabel.setText(String.valueOf(cardio));
        if (flexibilityCountLabel != null)
            flexibilityCountLabel.setText(String.valueOf(flex));
    }

    private void loadWorkoutIntoForm(Workout w) {
        titleField.setText(w.getTitle());
        descriptionField.setText(w.getDescription());
        categoryField.setValue(w.getCategory());
        durationField.setText(String.valueOf(w.getDurationMinutes()));
        difficultyField.setValue(w.getDifficultyLevel());
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        categoryField.setValue("strength");
        durationField.clear();
        difficultyField.setValue("medium");
        workoutTable.getSelectionModel().clearSelection();
    }

    private Workout formToWorkout(Workout existing) {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String desc = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String cat = categoryField.getValue() == null ? "strength" : categoryField.getValue();
        String diff = difficultyField.getValue() == null ? "medium" : difficultyField.getValue();
        int dur = 0;
        try {
            dur = Integer.parseInt(durationField.getText() == null ? "0" : durationField.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
        Workout w = existing != null ? existing : new Workout();
        w.setTitle(title);
        w.setDescription(desc);
        w.setCategory(cat);
        w.setDurationMinutes(dur);
        w.setDifficultyLevel(diff);
        if (existing == null)
            w.setCreatedAt(LocalDateTime.now());
        return w;
    }

    private void addWorkout() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showAlert("Validation", "Title is required.", Alert.AlertType.WARNING);
            return;
        }
        Workout w = formToWorkout(null);
        if (w == null || w.getDurationMinutes() < 1) {
            showAlert("Validation", "Duration must be a positive number.", Alert.AlertType.WARNING);
            return;
        }
        try {
            workoutDAO.create(w);
            showAlert("Success", "Workout added.", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to add: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateWorkout() {
        Workout selected = workoutTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Select a workout to update.", Alert.AlertType.WARNING);
            return;
        }
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showAlert("Validation", "Title is required.", Alert.AlertType.WARNING);
            return;
        }
        Workout w = formToWorkout(selected);
        if (w == null || w.getDurationMinutes() < 1) {
            showAlert("Validation", "Duration must be a positive number.", Alert.AlertType.WARNING);
            return;
        }
        try {
            workoutDAO.update(w);
            showAlert("Success", "Workout updated.", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to update: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteWorkout() {
        Workout selected = workoutTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Select a workout to delete.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete workout \"" + selected.getTitle() + "\"?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;
        try {
            workoutDAO.delete(selected.getId());
            showAlert("Success", "Workout deleted.", Alert.AlertType.INFORMATION);
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

    /** Back to main app (gestion) so user stays in tabbed flow. */
    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/fitness/fitness_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Fitness Hub");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
