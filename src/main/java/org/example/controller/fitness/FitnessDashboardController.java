package org.example.controller.fitness;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.fitness.WorkoutDAO;
import org.example.dao.fitness.WorkoutProgressDAO;
import org.example.model.fitness.Workout;
import org.example.util.Session;

import java.net.URL;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Fitness dashboard: streak, weekly completion %, workout cards, mark complete,
 * link to CRUD.
 */
public class FitnessDashboardController implements Initializable {

    @FXML
    private Label streakLabel;
    @FXML
    private Label weeklyPercentLabel;
    @FXML
    private Label totalWorkoutsLabel;
    @FXML
    private ProgressBar weeklyProgressBar;
    @FXML
    private VBox workoutCardsContainer;
    @FXML
    private VBox emptyStateContainer;
    @FXML
    private Button manageWorkoutsBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button quickAddBtn;
    @FXML
    private Button coachBtn;
    @FXML
    private Button myPlanBtn;
    @FXML
    private Button historyBtn;
    @FXML
    private Button homeBtn;

    private final WorkoutDAO workoutDAO = new WorkoutDAO();
    private final WorkoutProgressDAO progressDAO = new WorkoutProgressDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (manageWorkoutsBtn != null) {
            manageWorkoutsBtn.setOnAction(e -> goToWorkoutCrud());
            boolean isCoach = Session.isCoach();
            manageWorkoutsBtn.setVisible(isCoach);
            manageWorkoutsBtn.setManaged(isCoach);
        }
        if (refreshBtn != null)
            refreshBtn.setOnAction(e -> refresh());
        if (quickAddBtn != null)
            quickAddBtn.setOnAction(e -> goToWorkoutCrud());
        if (homeBtn != null)
            homeBtn.setOnAction(e -> goToHome());

        if (coachBtn != null) {
            coachBtn.setOnAction(e -> goToCoachDashboard());
            // Only show Coach button for coaches/admins
            coachBtn.setVisible(Session.isCoach());
            coachBtn.setManaged(Session.isCoach());
        }
        if (myPlanBtn != null)
            myPlanBtn.setOnAction(e -> goToMyPlan());
        if (historyBtn != null)
            historyBtn.setOnAction(e -> goToHistory());

        // Apply custom ProgressBar styling
        if (weeklyProgressBar != null) {
            weeklyProgressBar.setStyle("-fx-accent: #00ffc6; -fx-background-radius: 10;");
        }

        // Defer refresh so FXML load completes first
        Platform.runLater(this::refresh);
    }

    private void refresh() {
        try {
            if (streakLabel != null)
                streakLabel.setText("0");
            if (weeklyPercentLabel != null)
                weeklyPercentLabel.setText("0%");
            if (weeklyProgressBar != null)
                weeklyProgressBar.setProgress(0);
            if (totalWorkoutsLabel != null)
                totalWorkoutsLabel.setText("0");
            if (workoutCardsContainer == null)
                return;

            // Hide empty state initially
            if (emptyStateContainer != null)
                emptyStateContainer.setVisible(false);

            workoutCardsContainer.getChildren().clear();

            int userId = getCurrentUserId();
            if (userId <= 0) {
                Label msg = new Label("Please log in to track fitness.");
                msg.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 14px;");
                workoutCardsContainer.getChildren().add(msg);
                return;
            }
            updateStreakAndWeekly(userId);
            loadWorkoutCards(userId);
        } catch (Throwable t) {
            t.printStackTrace();
            if (workoutCardsContainer != null) {
                workoutCardsContainer.getChildren().clear();
                Label err = new Label("Fitness data error. Run fitness_schema.sql in MySQL to create workout tables. "
                        + t.getMessage());
                err.setWrapText(true);
                err.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
                workoutCardsContainer.getChildren().add(err);
            }
        }
    }

    private int getCurrentUserId() {
        try {
            return org.example.util.Session.getUserId();
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateStreakAndWeekly(int userId) {
        int streak = progressDAO.getCurrentStreak(userId);
        long completedDays = progressDAO.getCompletedDaysInCurrentWeek(userId);
        double percent = (completedDays / 7.0) * 100.0;
        if (streakLabel != null)
            streakLabel.setText(String.valueOf(streak));
        if (weeklyPercentLabel != null)
            weeklyPercentLabel.setText(String.format("%.0f%%", percent));
        if (weeklyProgressBar != null)
            weeklyProgressBar.setProgress(percent / 100.0);
    }

    private void loadWorkoutCards(int userId) {
        workoutCardsContainer.getChildren().clear();
        List<Workout> workouts = workoutDAO.readAll();

        // Update total workouts count
        if (totalWorkoutsLabel != null) {
            long totalCompleted = progressDAO.findByUserId(userId).size();
            totalWorkoutsLabel.setText(String.valueOf(totalCompleted));
        }

        if (workouts.isEmpty()) {
            // Show empty state
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(true);
                // Add action to "Get Started" button in empty state
                emptyStateContainer.lookupAll("Button").forEach(node -> {
                    if (node instanceof Button) {
                        ((Button) node).setOnAction(e -> goToWorkoutCrud());
                    }
                });
            }
            return;
        }

        // Hide empty state when workouts exist
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(false);
        }

        for (Workout w : workouts) {
            VBox card = buildWorkoutCard(w, userId);
            workoutCardsContainer.getChildren().add(card);
        }
    }

    private VBox buildWorkoutCard(Workout w, int userId) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-padding: 24; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6); -fx-max-width: 380; " +
                "-fx-border-color: rgba(0,201,167,0.25); -fx-border-width: 1.5; -fx-border-radius: 20;");
        card.setPadding(new Insets(24));

        // Add hover effect
        card.setOnMouseEntered(e -> card
                .setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 20; -fx-padding: 24; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.3), 20, 0, 0, 8); -fx-max-width: 380; " +
                        "-fx-border-color: rgba(0,255,198,0.5); -fx-border-width: 2; -fx-border-radius: 20;"));
        card.setOnMouseExited(e -> card
                .setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-padding: 24; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 6); -fx-max-width: 380; " +
                        "-fx-border-color: rgba(0,201,167,0.25); -fx-border-width: 1.5; -fx-border-radius: 20;"));

        // Header with category icon
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        String categoryIcon = getCategoryIcon(w.getCategory());
        Label iconLabel = new Label(categoryIcon);
        iconLabel.setStyle("-fx-font-size: 28px;");

        VBox headerText = new VBox(2);
        Label title = new Label(w.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        title.setWrapText(true);

        Label meta = new Label(w.getCategory().toUpperCase() + " • " + w.getDurationMinutes() + " min • " +
                getDifficultyDisplay(w.getDifficultyLevel()));
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #00ffc6; -fx-font-weight: 700; -fx-letter-spacing: 0.5px;");

        headerText.getChildren().addAll(title, meta);
        header.getChildren().addAll(iconLabel, headerText);

        // Description
        Label desc = new Label(w.getDescription() != null && !w.getDescription().isEmpty() ? w.getDescription()
                : "No description available");
        desc.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.7); -fx-wrap-text: true; -fx-padding: 0 0 8 0;");
        desc.setWrapText(true);
        desc.setMaxWidth(300);

        // Action button
        Button completeBtn = new Button("✓ MARK COMPLETE");
        completeBtn.setStyle("-fx-background-color: linear-gradient(to bottom right, #00c9a7 0%, #00ffc6 100%); " +
                "-fx-text-fill: #0a0820; -fx-font-weight: 800; -fx-font-size: 12px; " +
                "-fx-background-radius: 25; -fx-padding: 12 24; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.5), 10, 0, 0, 3);");
        completeBtn.setOnAction(e -> markDone(userId, w));

        // Add hover effect
        completeBtn.setOnMouseEntered(e -> completeBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #00ffc6 0%, #00e6b8 100%); " +
                        "-fx-text-fill: #0a0820; -fx-font-weight: 800; -fx-font-size: 12px; " +
                        "-fx-background-radius: 25; -fx-padding: 12 24; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.7), 15, 0, 0, 5);"));

        completeBtn.setOnMouseExited(e -> completeBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #00c9a7 0%, #00ffc6 100%); " +
                        "-fx-text-fill: #0a0820; -fx-font-weight: 800; -fx-font-size: 12px; " +
                        "-fx-background-radius: 25; -fx-padding: 12 24; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.5), 10, 0, 0, 3);"));

        card.getChildren().addAll(header, desc, new Region(), completeBtn);
        VBox.setVgrow(new Region(), Priority.ALWAYS);

        return card;
    }

    private void markDone(int userId, Workout w) {
        boolean added = progressDAO.markCompleted(userId, w.getId());
        if (added) {
            showAlert("Done", "Workout \"" + w.getTitle() + "\" marked complete for today. Keep the streak!",
                    Alert.AlertType.INFORMATION);
            refresh();
        } else {
            showAlert("Info", "You already completed this workout today.", Alert.AlertType.INFORMATION);
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

    @FXML
    private void goToCoachDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/fitness/coach_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coachBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Coach Dashboard");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open coach dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToMyPlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/fitness/my_plan.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) myPlanBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Workout Plan");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open my plan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/fitness/workout_history.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) refreshBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Workout History");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToHome() {
        try {
            String targetFxml = "/org/example/ui/Home.fxml";
            String title = "Home";

            if (Session.isAdmin() || "COACH".equalsIgnoreCase(Session.getRole())) {
                targetFxml = "/org/example/ui/gestion.fxml";
                title = "Dashboard";
            }

            // Navigate back to the appropriate dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Parent root = loader.load();
            Stage stage = (Stage) refreshBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Cannot open main menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Helper methods for workout card styling
    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "strength":
                return "\uD83D\uDCAA"; // flexed biceps
            case "cardio":
                return "\u2764\uFE0F"; // heart
            case "mobility":
                return "\uD83E\uDDD8"; // person in lotus
            case "flexibility":
                return "\uD83E\uDD38"; // person cartwheeling
            case "balance":
                return "\uD83E\uDDD1\u200D\uD83C\uDFCB\uFE0F"; // person lifting
            case "hiit":
                return "\u26A1"; // lightning bolt
            case "yoga":
                return "\uD83E\uDD4A"; // peace / boxing glove intensity
            default:
                return "\uD83C\uDFCB\uFE0F"; // weight lifter
        }
    }

    private String getDifficultyDisplay(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy":
                return "BEGINNER";
            case "medium":
                return "INTERMEDIATE";
            case "hard":
                return "ADVANCED";
            default:
                return difficulty.toUpperCase();
        }
    }
}
