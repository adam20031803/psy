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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.dao.fitness.WorkoutDAO;
import org.example.dao.fitness.WorkoutPlanDAO;
import org.example.dao.fitness.WorkoutProgressDAO;
import org.example.model.fitness.Workout;
import org.example.model.fitness.WorkoutPlan;
import org.example.util.Session;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "My Plan" – client-facing motivational weekly workout schedule.
 * Shows today's workouts prominently, the full 7-day plan, and motivational
 * elements.
 */
public class MyPlanController implements Initializable {

    @FXML
    private Label dayLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label motivationLabel;
    @FXML
    private Label todayCountLabel;
    @FXML
    private Label weekTotalLabel;
    @FXML
    private Label completedTodayLabel;
    @FXML
    private VBox todayWorkoutsContainer;
    @FXML
    private VBox weekPlanContainer;
    @FXML
    private Button backBtn;

    private final WorkoutPlanDAO planDAO = new WorkoutPlanDAO();
    private final WorkoutDAO workoutDAO = new WorkoutDAO();
    private final WorkoutProgressDAO progressDAO = new WorkoutProgressDAO();

    private static final String[] MOTIVATIONAL_QUOTES = {
            "\"The only bad workout is the one that didn't happen.\" 💪",
            "\"Don't stop when you're tired. Stop when you're done.\" 🔥",
            "\"Your body can stand almost anything. It's your mind you have to convince.\" 🧠",
            "\"Success is the sum of small efforts repeated day in and day out.\" ⭐",
            "\"The pain you feel today will be the strength you feel tomorrow.\" 💎",
            "\"Motivation is what gets you started. Habit is what keeps you going.\" 🚀",
            "\"Believe you can and you're halfway there.\" ✨",
            "\"Every champion was once a contender who refused to give up.\" 🏆",
            "\"Push yourself, because no one else is going to do it for you.\" 🎯",
            "\"It does not matter how slowly you go as long as you do not stop.\" 🐢"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (backBtn != null)
            backBtn.setOnAction(e -> goBack());
        Platform.runLater(this::loadPlan);
    }

    private void loadPlan() {
        try {
            int userId = Session.getUserId();
            LocalDate today = LocalDate.now();
            String todayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            // Set header
            if (dayLabel != null)
                dayLabel.setText(todayName.toUpperCase());
            if (dateLabel != null)
                dateLabel.setText(today.toString());

            // Random motivational quote
            if (motivationLabel != null) {
                motivationLabel.setText(MOTIVATIONAL_QUOTES[new Random().nextInt(MOTIVATIONAL_QUOTES.length)]);
            }

            // Load full plan
            List<WorkoutPlan> myPlan = planDAO.findByUserId(userId);
            Map<Integer, Workout> workoutMap = new HashMap<>();
            for (Workout w : workoutDAO.readAll()) {
                workoutMap.put(w.getId(), w);
            }

            // Today's workouts
            List<WorkoutPlan> todayPlans = myPlan.stream()
                    .filter(p -> todayName.equalsIgnoreCase(p.getDayOfWeek()))
                    .collect(Collectors.toList());

            // Completed today
            long completedToday = progressDAO.findByUserId(userId).stream()
                    .filter(p -> today.equals(p.getCompletedDate()))
                    .count();

            if (todayCountLabel != null)
                todayCountLabel.setText(String.valueOf(todayPlans.size()));
            if (weekTotalLabel != null)
                weekTotalLabel.setText(String.valueOf(myPlan.size()));
            if (completedTodayLabel != null)
                completedTodayLabel.setText(String.valueOf(completedToday));

            // Build today's workout cards
            if (todayWorkoutsContainer != null) {
                todayWorkoutsContainer.getChildren().clear();
                if (todayPlans.isEmpty()) {
                    VBox emptyCard = buildEmptyTodayCard();
                    todayWorkoutsContainer.getChildren().add(emptyCard);
                } else {
                    for (WorkoutPlan plan : todayPlans) {
                        Workout w = workoutMap.get(plan.getWorkoutId());
                        if (w != null) {
                            VBox card = buildTodayWorkoutCard(w, plan, userId);
                            todayWorkoutsContainer.getChildren().add(card);
                        }
                    }
                }
            }

            // Build week plan overview
            if (weekPlanContainer != null) {
                weekPlanContainer.getChildren().clear();
                String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
                for (String day : days) {
                    List<WorkoutPlan> dayPlans = myPlan.stream()
                            .filter(p -> day.equalsIgnoreCase(p.getDayOfWeek()))
                            .collect(Collectors.toList());
                    HBox dayRow = buildDayRow(day, dayPlans, workoutMap, todayName.equalsIgnoreCase(day));
                    weekPlanContainer.getChildren().add(dayRow);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buildTodayWorkoutCard(Workout w, WorkoutPlan plan, int userId) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; " +
                "-fx-border-color: rgba(0,255,198,0.35); -fx-border-width: 2; -fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.2), 20, 0, 0, 8);");

        // Hover effect
        card.setOnMouseEntered(
                e -> card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 20; " +
                        "-fx-border-color: rgba(0,255,198,0.6); -fx-border-width: 2; -fx-border-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.4), 25, 0, 0, 10);"));
        card.setOnMouseExited(
                e -> card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; " +
                        "-fx-border-color: rgba(0,255,198,0.35); -fx-border-width: 2; -fx-border-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.2), 20, 0, 0, 8);"));

        // Header row with icon + title + duration badge
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getCategoryIcon(w.getCategory()));
        icon.setStyle("-fx-font-size: 32px;");

        VBox titleBox = new VBox(3);
        Label title = new Label(w.getTitle());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");
        title.setWrapText(true);

        Label meta = new Label(w.getCategory().toUpperCase() + "  •  " + getDifficultyDisplay(w.getDifficultyLevel()));
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #00ffc6; -fx-font-weight: 700; -fx-letter-spacing: 0.5px;");
        titleBox.getChildren().addAll(title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Duration badge
        VBox durationBadge = new VBox(2);
        durationBadge.setAlignment(Pos.CENTER);
        durationBadge.setPadding(new Insets(10, 16, 10, 16));
        durationBadge.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, rgba(255,107,107,0.25), rgba(255,82,82,0.2)); "
                        +
                        "-fx-background-radius: 16; -fx-border-color: rgba(255,107,107,0.4); -fx-border-radius: 16; -fx-border-width: 1;");
        Label durNum = new Label(w.getDurationMinutes() + "");
        durNum.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #ff6b6b;");
        Label durLabel = new Label("min");
        durLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6); -fx-font-weight: 600;");
        durationBadge.getChildren().addAll(durNum, durLabel);

        header.getChildren().addAll(icon, titleBox, spacer, durationBadge);

        // Description
        Label desc = new Label(w.getDescription() != null && !w.getDescription().isEmpty()
                ? w.getDescription()
                : "No description available");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.65); -fx-wrap-text: true;");
        desc.setWrapText(true);

        // Coach notes
        VBox notesBox = new VBox();
        if (plan.getNotes() != null && !plan.getNotes().isEmpty()) {
            Label notesHeader = new Label("📝 Coach Notes:");
            notesHeader.setStyle("-fx-font-size: 11px; -fx-text-fill: #a5b4ff; -fx-font-weight: 700;");
            Label notesText = new Label(plan.getNotes());
            notesText.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.55); -fx-wrap-text: true; -fx-font-style: italic;");
            notesText.setWrapText(true);
            notesBox.setSpacing(4);
            notesBox.setPadding(new Insets(8, 12, 8, 12));
            notesBox.setStyle("-fx-background-color: rgba(103,128,255,0.1); -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(103,128,255,0.2); -fx-border-radius: 12; -fx-border-width: 1;");
            notesBox.getChildren().addAll(notesHeader, notesText);
        }

        // Mark complete button
        Button completeBtn = new Button("✓  MARK COMPLETE  •  LET'S GO!");
        completeBtn.setMaxWidth(Double.MAX_VALUE);
        completeBtn.setStyle("-fx-background-color: linear-gradient(to right, #00c9a7, #00ffc6); " +
                "-fx-text-fill: #0a0820; -fx-font-weight: 900; -fx-font-size: 13px; " +
                "-fx-background-radius: 14; -fx-padding: 16; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,255,198,0.5), 12, 0, 0, 4);");
        completeBtn.setOnAction(e -> {
            boolean ok = progressDAO.markCompleted(userId, w.getId());
            if (ok) {
                showAlert("🎉 Awesome!", "You crushed \"" + w.getTitle() + "\"! Keep the streak alive! 🔥",
                        Alert.AlertType.INFORMATION);
                loadPlan();
            } else {
                showAlert("Already Done ✅", "You already completed this workout today. Rest up! 😌",
                        Alert.AlertType.INFORMATION);
            }
        });

        card.getChildren().addAll(header, desc);
        if (!notesBox.getChildren().isEmpty())
            card.getChildren().add(notesBox);
        card.getChildren().add(completeBtn);

        return card;
    }

    private VBox buildEmptyTodayCard() {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 24, 40, 24));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 20; " +
                "-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-radius: 20; " +
                "-fx-border-style: dashed;");

        Label emoji = new Label("🌴");
        emoji.setStyle("-fx-font-size: 42px;");

        Label msg = new Label("REST DAY!");
        msg.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #00ffc6;");

        Label sub = new Label("No workouts planned for today. Use this time to recover and recharge!");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.5);");
        sub.setWrapText(true);

        card.getChildren().addAll(emoji, msg, sub);
        return card;
    }

    private HBox buildDayRow(String day, List<WorkoutPlan> plans, Map<Integer, Workout> workoutMap, boolean isToday) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));

        String bgColor = isToday
                ? "rgba(0,255,198,0.15)"
                : "rgba(255,255,255,0.05)";
        String borderColor = isToday
                ? "rgba(0,255,198,0.5)"
                : "rgba(255,255,255,0.1)";

        row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 14; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 14; -fx-border-width: 1.5;");

        // Day label
        Label dayLbl = new Label(day.substring(0, 3).toUpperCase());
        dayLbl.setMinWidth(45);
        dayLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 900; -fx-text-fill: " +
                (isToday ? "#00ffc6" : "rgba(255,255,255,0.7)") + "; -fx-letter-spacing: 0.5px;");

        // Today indicator
        if (isToday) {
            Label todayTag = new Label("TODAY");
            todayTag.setStyle("-fx-font-size: 9px; -fx-font-weight: 800; -fx-text-fill: #0a0820; " +
                    "-fx-background-color: #00ffc6; -fx-background-radius: 8; -fx-padding: 3 8; -fx-letter-spacing: 0.5px;");
            row.getChildren().addAll(dayLbl, todayTag);
        } else {
            row.getChildren().add(dayLbl);
        }

        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: rgba(255,255,255,0.2); -fx-font-size: 16px;");
        row.getChildren().add(sep);

        if (plans.isEmpty()) {
            Label rest = new Label("🌴 Rest Day");
            rest.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.35); -fx-font-style: italic;");
            row.getChildren().add(rest);
        } else {
            for (WorkoutPlan p : plans) {
                Workout w = workoutMap.get(p.getWorkoutId());
                if (w != null) {
                    Label wLabel = new Label(getCategoryIcon(w.getCategory()) + " " + w.getTitle() + " ("
                            + w.getDurationMinutes() + "min)");
                    wLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: 600;");
                    row.getChildren().add(wLabel);
                }
            }
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().add(spacer);

        // Total duration for day
        int totalMin = plans.stream()
                .mapToInt(p -> {
                    Workout w = workoutMap.get(p.getWorkoutId());
                    return w != null ? w.getDurationMinutes() : 0;
                }).sum();
        if (totalMin > 0) {
            Label durLabel = new Label("⏱ " + totalMin + "min");
            durLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff8e53; -fx-font-weight: 700;");
            row.getChildren().add(durLabel);
        }

        return row;
    }

    private String getCategoryIcon(String category) {
        if (category == null)
            return "🏋️";
        switch (category.toLowerCase()) {
            case "strength":
                return "💪";
            case "cardio":
                return "🏃";
            case "mobility":
                return "🧘";
            case "flexibility":
                return "🤸";
            case "balance":
                return "⚖️";
            default:
                return "🏋️";
        }
    }

    private String getDifficultyDisplay(String difficulty) {
        if (difficulty == null)
            return "";
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
