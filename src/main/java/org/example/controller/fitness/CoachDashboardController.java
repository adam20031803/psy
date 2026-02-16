package org.example.controller.fitness;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.dao.UserDAO;
import org.example.dao.fitness.WorkoutDAO;
import org.example.dao.fitness.WorkoutPlanDAO;
import org.example.dao.fitness.WorkoutProgressDAO;
import org.example.model.User;
import org.example.model.fitness.Workout;
import org.example.model.fitness.WorkoutPlan;
import org.example.model.fitness.WorkoutProgress;
import org.example.util.Session;

import java.io.File;
import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Coach Dashboard: assign workouts to clients, sort, search-by-field, PDF table
 * export.
 */
public class CoachDashboardController implements Initializable {

    @FXML
    private TableView<WorkoutPlan> planTable;
    @FXML
    private TableColumn<WorkoutPlan, Integer> planIdCol;
    @FXML
    private TableColumn<WorkoutPlan, String> planClientCol;
    @FXML
    private TableColumn<WorkoutPlan, String> planWorkoutCol;
    @FXML
    private TableColumn<WorkoutPlan, String> planDayCol;
    @FXML
    private TableColumn<WorkoutPlan, String> planNotesCol;

    @FXML
    private ComboBox<String> clientCombo;
    @FXML
    private ComboBox<String> workoutCombo;
    @FXML
    private ComboBox<String> dayCombo;
    @FXML
    private TextArea notesField;

    // Feedback Table
    @FXML
    private TableView<WorkoutProgress> feedbackTable;
    @FXML
    private TableColumn<WorkoutProgress, String> fbDateCol;
    @FXML
    private TableColumn<WorkoutProgress, String> fbClientCol;
    @FXML
    private TableColumn<WorkoutProgress, String> fbWorkoutCol;
    @FXML
    private TableColumn<WorkoutProgress, String> fbRatingCol;
    @FXML
    private TableColumn<WorkoutProgress, String> fbNotesCol;
    @FXML
    private Button refreshFeedbackBtn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> searchByCombo;
    @FXML
    private ComboBox<String> sortByCombo;

    @FXML
    private Button assignBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button backBtn;
    @FXML
    private Button exportPdfBtn;
    @FXML
    private Button manageWorkoutsBtn;

    private final WorkoutPlanDAO planDAO = new WorkoutPlanDAO();
    private final WorkoutDAO workoutDAO = new WorkoutDAO();
    private final UserDAO userDAO = new UserDAO();
    private final WorkoutProgressDAO progressDAO = new WorkoutProgressDAO();

    private final ObservableList<WorkoutPlan> planList = FXCollections.observableArrayList();
    private final ObservableList<WorkoutProgress> feedbackList = FXCollections.observableArrayList();
    private List<WorkoutPlan> allPlans = new ArrayList<>();
    private List<User> clients;
    private List<Workout> workouts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!Session.isCoach()) {
            showAlert("Access Denied", "You do not have permission to view this page.", Alert.AlertType.ERROR);
            goBack(); // Redirect attempt
            return;
        }

        setupTableColumns();
        setupFeedbackTable();
        setupCombos();
        setupButtons();
        setupSearchAndSort();
        loadData();
    }

    /* ==================== TABLE ==================== */

    private void setupTableColumns() {
        planIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        planClientCol
                .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getClientName()));
        planWorkoutCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getWorkoutTitle()));
        planDayCol
                .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDayOfWeek()));
        planNotesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));

        setWhiteTextCellFactory(planIdCol);
        setWhiteTextCellFactory(planClientCol);
        setWhiteTextCellFactory(planWorkoutCol);
        setWhiteTextCellFactory(planDayCol);
        setWhiteTextCellFactory(planNotesCol);

        planTable.setItems(planList);
        planTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null)
                loadPlanIntoForm(sel);
        });
    }

    private <T> void setWhiteTextCellFactory(TableColumn<WorkoutPlan, T> col) {
        col.setCellFactory(column -> new TableCell<WorkoutPlan, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Label label = new Label(item.toString());
                    label.setTextFill(Color.WHITE);
                    label.setStyle("-fx-font-size: 13px;");
                    setGraphic(label);
                }
            }
        });
    }

    /* ==================== COMBOS ==================== */

    private void setupCombos() {
        dayCombo.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        dayCombo.setValue("Monday");

        try {
            clients = userDAO.findAll();
            for (User u : clients) {
                clientCombo.getItems().add(u.getId() + " - " + u.getPrenom() + " " + u.getNom());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        workouts = workoutDAO.readAll();
        for (Workout w : workouts) {
            workoutCombo.getItems().add(w.getId() + " - " + w.getTitle());
        }
    }

    /* ==================== BUTTONS ==================== */

    private void setupButtons() {
        assignBtn.setOnAction(e -> assignPlan());
        updateBtn.setOnAction(e -> updatePlan());
        deleteBtn.setOnAction(e -> deletePlan());
        clearBtn.setOnAction(e -> clearForm());
        backBtn.setOnAction(e -> goBack());
        if (exportPdfBtn != null)
            exportPdfBtn.setOnAction(e -> exportToPDF());
        if (manageWorkoutsBtn != null)
            manageWorkoutsBtn.setOnAction(e -> goToWorkoutCrud());
        if (refreshFeedbackBtn != null)
            refreshFeedbackBtn.setOnAction(e -> loadFeedback());
    }

    /* ==================== SEARCH & SORT ==================== */

    private void setupSearchAndSort() {
        // Search-by dropdown
        if (searchByCombo != null) {
            searchByCombo.getItems().addAll("All", "Client", "Workout", "Day", "Notes");
            searchByCombo.setValue("All");
            searchByCombo.setOnAction(e -> applySearchAndSort());
        }

        // Sort dropdown
        if (sortByCombo != null) {
            sortByCombo.getItems().addAll("Name (A→Z)", "Name (Z→A)", "Newest First", "Oldest First", "Day of Week");
            sortByCombo.setValue("Name (A→Z)");
            sortByCombo.setOnAction(e -> applySearchAndSort());
        }

        // Live search
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearchAndSort());
        }
    }

    private void applySearchAndSort() {
        List<WorkoutPlan> result = new ArrayList<>(allPlans);

        // 1. FILTER
        String keyword = searchField != null ? searchField.getText() : "";
        String searchBy = searchByCombo != null ? searchByCombo.getValue() : "All";
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lower = keyword.toLowerCase().trim();
            result = result.stream().filter(p -> {
                switch (searchBy) {
                    case "Client":
                        return p.getClientName() != null && p.getClientName().toLowerCase().contains(lower);
                    case "Workout":
                        return p.getWorkoutTitle() != null && p.getWorkoutTitle().toLowerCase().contains(lower);
                    case "Day":
                        return p.getDayOfWeek() != null && p.getDayOfWeek().toLowerCase().contains(lower);
                    case "Notes":
                        return p.getNotes() != null && p.getNotes().toLowerCase().contains(lower);
                    default: // All
                        return (p.getClientName() != null && p.getClientName().toLowerCase().contains(lower))
                                || (p.getWorkoutTitle() != null && p.getWorkoutTitle().toLowerCase().contains(lower))
                                || (p.getDayOfWeek() != null && p.getDayOfWeek().toLowerCase().contains(lower))
                                || (p.getNotes() != null && p.getNotes().toLowerCase().contains(lower));
                }
            }).collect(Collectors.toList());
        }

        // 2. SORT
        String sortBy = sortByCombo != null ? sortByCombo.getValue() : "Name (A→Z)";
        switch (sortBy) {
            case "Name (A→Z)":
                result.sort(
                        Comparator.comparing(p -> p.getClientName() != null ? p.getClientName().toLowerCase() : ""));
                break;
            case "Name (Z→A)":
                result.sort(Comparator
                        .comparing((WorkoutPlan p) -> p.getClientName() != null ? p.getClientName().toLowerCase() : "")
                        .reversed());
                break;
            case "Newest First":
                result.sort(Comparator.comparing(WorkoutPlan::getId).reversed());
                break;
            case "Oldest First":
                result.sort(Comparator.comparing(WorkoutPlan::getId));
                break;
            case "Day of Week":
                List<String> dayOrder = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                        "Saturday", "Sunday");
                result.sort(Comparator.comparingInt(p -> {
                    int idx = dayOrder.indexOf(p.getDayOfWeek());
                    return idx >= 0 ? idx : 99;
                }));
                break;
        }

        planList.setAll(result);
    }

    /* ==================== FEEDBACK TABLE ==================== */

    private void setupFeedbackTable() {
        fbDateCol.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getCompletedDate();
            return new javafx.beans.property.SimpleStringProperty(
                    d != null ? d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        });
        fbClientCol.setCellValueFactory(c -> {
            int uid = c.getValue().getUserId();
            return new javafx.beans.property.SimpleStringProperty(getUserName(uid));
        });
        fbWorkoutCol.setCellValueFactory(c -> {
            int wid = c.getValue().getWorkoutId();
            return new javafx.beans.property.SimpleStringProperty(getWorkoutName(wid));
        });
        fbRatingCol.setCellValueFactory(c -> {
            int r = c.getValue().getRating();
            String label = "-";
            if (r == 1)
                label = "👎";
            if (r == 2)
                label = "👍";
            return new javafx.beans.property.SimpleStringProperty(label);
        });
        fbNotesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));

        setWhiteTextCellFactoryFeedback(fbDateCol);
        setWhiteTextCellFactoryFeedback(fbClientCol);
        setWhiteTextCellFactoryFeedback(fbWorkoutCol);
        setWhiteTextCellFactoryFeedback(fbRatingCol);
        setWhiteTextCellFactoryFeedback(fbNotesCol);

        feedbackTable.setItems(feedbackList);
    }

    private <T> void setWhiteTextCellFactoryFeedback(TableColumn<WorkoutProgress, T> col) {
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
                    label.setTextFill(Color.WHITE);
                    label.setStyle("-fx-font-size: 13px;");
                    setGraphic(label);
                }
            }
        });
    }

    // Helper to get Names efficiently (could cache)
    private String getUserName(int id) {
        if (clients == null)
            return String.valueOf(id);
        return clients.stream().filter(u -> u.getId() == id)
                .map(u -> u.getPrenom() + " " + u.getNom())
                .findFirst().orElse(String.valueOf(id));
    }

    private String getWorkoutName(int id) {
        if (workouts == null)
            return String.valueOf(id);
        return workouts.stream().filter(w -> w.getId() == id)
                .map(Workout::getTitle)
                .findFirst().orElse(String.valueOf(id));
    }

    /* ==================== DATA ==================== */

    private void loadData() {
        int coachId = Session.getUserId();
        allPlans = planDAO.findByCoachId(coachId);
        applySearchAndSort();

        loadFeedback();
    }

    private void loadFeedback() {
        // Load all progress (admin view) or coach's clients?
        // Ideally should filter by coach's clients, but readAll is fine for now if
        // small scale
        List<WorkoutProgress> list = progressDAO.readAll();
        feedbackList.setAll(list);
    }

    /* ==================== FORM ==================== */

    private void loadPlanIntoForm(WorkoutPlan p) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == p.getUserId()) {
                clientCombo.getSelectionModel().select(i);
                break;
            }
        }
        for (int i = 0; i < workouts.size(); i++) {
            if (workouts.get(i).getId() == p.getWorkoutId()) {
                workoutCombo.getSelectionModel().select(i);
                break;
            }
        }
        dayCombo.setValue(p.getDayOfWeek());
        notesField.setText(p.getNotes() != null ? p.getNotes() : "");
    }

    private void clearForm() {
        clientCombo.getSelectionModel().clearSelection();
        workoutCombo.getSelectionModel().clearSelection();
        dayCombo.setValue("Monday");
        notesField.clear();
        planTable.getSelectionModel().clearSelection();
    }

    private int getSelectedClientId() {
        int idx = clientCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= clients.size())
            return -1;
        return clients.get(idx).getId();
    }

    private int getSelectedWorkoutId() {
        int idx = workoutCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= workouts.size())
            return -1;
        return workouts.get(idx).getId();
    }

    /* ==================== CRUD ==================== */

    private void assignPlan() {
        int clientId = getSelectedClientId();
        int workoutId = getSelectedWorkoutId();
        String day = dayCombo.getValue();
        if (clientId < 0 || workoutId < 0 || day == null) {
            showAlert("Validation", "Please select a client, workout, and day.", Alert.AlertType.WARNING);
            return;
        }
        String notes = notesField.getText() != null ? notesField.getText().trim() : "";
        WorkoutPlan wp = new WorkoutPlan(clientId, workoutId, day, Session.getUserId(), notes);
        try {
            planDAO.create(wp);
            showAlert("Success", "Workout assigned successfully!", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to assign: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updatePlan() {
        WorkoutPlan selected = planTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Select a plan to update.", Alert.AlertType.WARNING);
            return;
        }
        int clientId = getSelectedClientId();
        int workoutId = getSelectedWorkoutId();
        String day = dayCombo.getValue();
        if (clientId < 0 || workoutId < 0 || day == null) {
            showAlert("Validation", "Please select a client, workout, and day.", Alert.AlertType.WARNING);
            return;
        }
        selected.setUserId(clientId);
        selected.setWorkoutId(workoutId);
        selected.setDayOfWeek(day);
        selected.setNotes(notesField.getText() != null ? notesField.getText().trim() : "");
        try {
            planDAO.update(selected);
            showAlert("Success", "Plan updated!", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to update: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deletePlan() {
        WorkoutPlan selected = planTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Select a plan to delete.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        confirm.setContentText("Remove this workout assignment?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;
        try {
            planDAO.delete(selected.getId());
            showAlert("Success", "Plan removed!", Alert.AlertType.INFORMATION);
            loadData();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /* ==================== PDF EXPORT (TABLE ONLY) ==================== */

    private void exportToPDF() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Plans to PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fc.setInitialFileName("workout_plans_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            File file = fc.showSaveDialog(exportPdfBtn.getScene().getWindow());
            if (file != null)
                generateTablePDF(file);
        } catch (Exception e) {
            showAlert("Error", "Export failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void generateTablePDF(File file) {
        try {
            PDDocument doc = new PDDocument();

            // Use the currently displayed (filtered/sorted) list
            List<WorkoutPlan> data = new ArrayList<>(planList);
            int perPage = 25;
            int totalPages = Math.max(1, (int) Math.ceil(data.size() / (double) perPage));

            for (int pg = 0; pg < totalPages; pg++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float pw = page.getMediaBox().getWidth();
                    float ph = page.getMediaBox().getHeight();

                    // Header bar
                    cs.setNonStrokingColor(new java.awt.Color(255, 142, 83));
                    cs.addRect(0, ph - 50, pw, 50);
                    cs.fill();

                    cs.beginText();
                    cs.setNonStrokingColor(java.awt.Color.WHITE);
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                    cs.newLineAtOffset(30, ph - 35);
                    cs.showText("Workout Plans  -  " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    cs.endText();

                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    cs.newLineAtOffset(pw - 100, ph - 35);
                    cs.showText("Page " + (pg + 1) + " / " + totalPages);
                    cs.endText();

                    // Table column positions
                    float[] colX = { 30, 80, 200, 340, 440 };
                    String[] headers = { "ID", "CLIENT", "WORKOUT", "DAY", "NOTES" };
                    float y = ph - 80;

                    // Table header row
                    cs.setNonStrokingColor(new java.awt.Color(44, 62, 80));
                    cs.addRect(25, y - 5, pw - 50, 22);
                    cs.fill();

                    cs.setNonStrokingColor(java.awt.Color.WHITE);
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                    for (int h = 0; h < headers.length; h++) {
                        cs.beginText();
                        cs.newLineAtOffset(colX[h], y);
                        cs.showText(headers[h]);
                        cs.endText();
                    }
                    y -= 28;

                    // Data rows
                    int start = pg * perPage;
                    int end = Math.min(start + perPage, data.size());
                    boolean alternate = false;

                    for (int r = start; r < end; r++) {
                        WorkoutPlan wp = data.get(r);

                        // Alternating row background
                        if (alternate) {
                            cs.setNonStrokingColor(new java.awt.Color(245, 245, 245));
                            cs.addRect(25, y - 5, pw - 50, 20);
                            cs.fill();
                        }
                        alternate = !alternate;

                        cs.setNonStrokingColor(java.awt.Color.BLACK);
                        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

                        // ID
                        cs.beginText();
                        cs.newLineAtOffset(colX[0], y);
                        cs.showText(String.valueOf(wp.getId()));
                        cs.endText();

                        // Client
                        cs.beginText();
                        cs.newLineAtOffset(colX[1], y);
                        String client = wp.getClientName() != null ? wp.getClientName() : "";
                        if (client.length() > 18)
                            client = client.substring(0, 15) + "...";
                        cs.showText(removeAccents(client));
                        cs.endText();

                        // Workout
                        cs.beginText();
                        cs.newLineAtOffset(colX[2], y);
                        String workout = wp.getWorkoutTitle() != null ? wp.getWorkoutTitle() : "";
                        if (workout.length() > 20)
                            workout = workout.substring(0, 17) + "...";
                        cs.showText(removeAccents(workout));
                        cs.endText();

                        // Day
                        cs.beginText();
                        cs.newLineAtOffset(colX[3], y);
                        cs.showText(wp.getDayOfWeek() != null ? wp.getDayOfWeek() : "");
                        cs.endText();

                        // Notes
                        cs.beginText();
                        cs.setNonStrokingColor(new java.awt.Color(127, 140, 141));
                        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                        cs.newLineAtOffset(colX[4], y);
                        String notes = wp.getNotes() != null ? wp.getNotes() : "-";
                        if (notes.length() > 20)
                            notes = notes.substring(0, 17) + "...";
                        cs.showText(removeAccents(notes));
                        cs.endText();

                        y -= 22;
                    }

                    // Table bottom border
                    cs.setStrokingColor(new java.awt.Color(200, 200, 200));
                    cs.setLineWidth(0.5f);
                    cs.moveTo(25, y + 15);
                    cs.lineTo(pw - 25, y + 15);
                    cs.stroke();

                    // Footer
                    cs.beginText();
                    cs.setNonStrokingColor(new java.awt.Color(160, 160, 160));
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                    cs.newLineAtOffset(30, 25);
                    cs.showText("Total plans: " + data.size() + "  |  Fitness Coach Dashboard");
                    cs.endText();
                }
            }

            doc.save(file);
            doc.close();

            showAlert("Success",
                    "PDF exported!\nFile: " + file.getName() + "\nPlans: " + data.size(),
                    Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "PDF generation failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String removeAccents(String text) {
        if (text == null)
            return "";
        text = text.replace("\n", " ").replace("\r", " ");
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /* ==================== UTILITIES ==================== */

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

    @FXML
    private void goToWorkoutCrud() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/fitness/workout_crud.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) manageWorkoutsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Workout Management");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open workout management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
