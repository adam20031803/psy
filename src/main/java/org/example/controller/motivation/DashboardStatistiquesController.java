package org.example.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.dao.ChallengeCrud;
import org.example.dao.CoachMotivationCrud;
import org.example.dao.RecompenseCrud;
import org.example.model.Challenge;
import org.example.model.CoachMotivation;
import org.example.model.Recompense;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;



import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

public class DashboardStatistiquesController implements Initializable {

    @FXML private Label totalChallengesLabel;
    @FXML private Label actifsChallengesLabel;
    @FXML private Label inactifsChallengesLabel;
    @FXML private Label totalCoachsLabel;
    @FXML private Label actifsCoachsLabel;
    @FXML private Label specialisesCoachsLabel;
    @FXML private Label totalRecompensesLabel;
    @FXML private Label activesRecompensesLabel;
    @FXML private Label typesRecompensesLabel;
    @FXML private Label tauxActiviteLabel;
    @FXML private Label dateActuelleLabel;
    @FXML private Label heureActuelleLabel;
    @FXML private Label derniereMajLabel;
    @FXML private Label objectifMensuelLabel;
    @FXML private Label progressionPourcentageLabel;
    @FXML private Rectangle progressionBar;

    @FXML private PieChart difficultePieChart;
    @FXML private PieChart typePieChart;
    @FXML private BarChart<String, Number> recompensesBarChart;

    @FXML private VBox topChallengesList;
    @FXML private VBox topCoachsList;
    @FXML private VBox topRecompensesList;

    @FXML
    private StackPane logoAccueil;

    private final ChallengeCrud challengeCrud = new ChallengeCrud();
    private final CoachMotivationCrud coachCrud = new CoachMotivationCrud();
    private final RecompenseCrud recompenseCrud = new RecompenseCrud();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== DASHBOARD INITIALIZATION ===");
        chargerStatistiques();
        initialiserHorloge();
    }

    private void chargerStatistiques() {
        try {
            // Charger les données depuis la base
            List<Challenge> challenges = challengeCrud.readAll();
            List<CoachMotivation> coachs = coachCrud.readAll();
            List<Recompense> recompenses = recompenseCrud.readAll();

            System.out.println("Données chargées - Challenges: " + challenges.size() +
                    ", Coachs: " + coachs.size() +
                    ", Récompenses: " + recompenses.size());

            // Mettre à jour les KPIs
            mettreAJourKPIs(challenges, coachs, recompenses);

            // Mettre à jour les graphiques
            mettreAJourGraphiques(challenges, recompenses);

            // Mettre à jour les listes
            mettreAJourTopListes(challenges, coachs, recompenses);

            // Date de dernière mise à jour
            derniereMajLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    private void mettreAJourKPIs(List<Challenge> challenges, List<CoachMotivation> coachs, List<Recompense> recompenses) {
        // Challenges
        long totalChallenges = challenges.size();
        long actifsChallenges = challenges.stream().filter(Challenge::isActif).count();
        long inactifsChallenges = totalChallenges - actifsChallenges;

        totalChallengesLabel.setText(String.valueOf(totalChallenges));
        actifsChallengesLabel.setText(String.valueOf(actifsChallenges));
        inactifsChallengesLabel.setText(String.valueOf(inactifsChallenges));

        // Coachs
        long totalCoachs = coachs.size();
        long actifsCoachs = coachs.stream().filter(CoachMotivation::isActif).count();
        long specialises = coachs.stream().filter(c -> c.getStyle() != null && !c.getStyle().isEmpty()).count();

        totalCoachsLabel.setText(String.valueOf(totalCoachs));
        actifsCoachsLabel.setText(String.valueOf(actifsCoachs));
        specialisesCoachsLabel.setText(String.valueOf(specialises));

        // Récompenses
        long totalRecompenses = recompenses.size();
        long activesRecompenses = recompenses.stream().filter(Recompense::isActif).count();
        long typesUniques = recompenses.stream()
                .map(Recompense::getTypeRecompense)
                .distinct()
                .count();

        totalRecompensesLabel.setText(String.valueOf(totalRecompenses));
        activesRecompensesLabel.setText(String.valueOf(activesRecompenses));
        typesRecompensesLabel.setText(String.valueOf(typesUniques));

        // Taux d'activité global
        double taux = totalChallenges > 0 ? (actifsChallenges * 100.0 / totalChallenges) : 0;
        tauxActiviteLabel.setText(String.format("%.1f%%", taux));

        // Objectif mensuel (exemple: 20 challenges actifs)
        int objectifMensuel = 20;

        // Calculer le pourcentage SANS limite
        double pourcentage = totalChallenges > 0 ? (actifsChallenges * 100.0 / objectifMensuel) : 0;

        // Afficher le pourcentage réel (non limité)
        progressionPourcentageLabel.setText(String.format("%.1f%%", pourcentage));
        objectifMensuelLabel.setText(actifsChallenges + "/" + objectifMensuel);

        // Calculer la largeur de la barre SANS limite (basée sur 300px max théorique)
        // Mais on peut dépasser 300px pour montrer le dépassement
        double largeurBase = 300; // largeur maximale de référence
        double largeurBarre = (pourcentage / 100.0) * largeurBase;

        // Appliquer la largeur (peut dépasser 300px)
        progressionBar.setWidth(largeurBarre);

        // Optionnel: Changer la couleur si >100%
        if (pourcentage > 100) {
            progressionBar.setStyle("-fx-fill: linear-gradient(to right, #2ECC71, #F1C40F, #E74C3C);");
        } else {
            progressionBar.setStyle("-fx-fill: linear-gradient(to right, #3498DB, #9B59B6);");
        }
    }
    private void mettreAJourGraphiques(List<Challenge> challenges, List<Recompense> recompenses) {
        // Graphique par difficulté
        ObservableList<PieChart.Data> difficulteData = FXCollections.observableArrayList();
        Map<String, Long> difficulteStats = challenges.stream()
                .collect(Collectors.groupingBy(Challenge::getNiveauDifficulte, Collectors.counting()));

        difficulteStats.forEach((difficulte, count) ->
                difficulteData.add(new PieChart.Data(difficulte + " (" + count + ")", count))
        );

        if (difficulteData.isEmpty()) {
            difficulteData.add(new PieChart.Data("Aucune donnée", 1));
        }
        difficultePieChart.setData(difficulteData);

        // Graphique par type
        ObservableList<PieChart.Data> typeData = FXCollections.observableArrayList();
        Map<String, Long> typeStats = challenges.stream()
                .collect(Collectors.groupingBy(Challenge::getTypeChallenge, Collectors.counting()));

        typeStats.forEach((type, count) ->
                typeData.add(new PieChart.Data(type + " (" + count + ")", count))
        );

        if (typeData.isEmpty()) {
            typeData.add(new PieChart.Data("Aucune donnée", 1));
        }
        typePieChart.setData(typeData);

        // Graphique des récompenses par type
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Récompenses");

        Map<String, Long> recompensesStats = recompenses.stream()
                .collect(Collectors.groupingBy(Recompense::getTypeRecompense, Collectors.counting()));

        recompensesStats.forEach((type, count) ->
                series.getData().add(new XYChart.Data<>(type, count))
        );

        recompensesBarChart.getData().clear();
        if (!series.getData().isEmpty()) {
            recompensesBarChart.getData().add(series);
        }
    }

    private void mettreAJourTopListes(List<Challenge> challenges, List<CoachMotivation> coachs, List<Recompense> recompenses) {
        // Top 5 challenges (par durée)
        topChallengesList.getChildren().clear();
        challenges.stream()
                .sorted((c1, c2) -> Integer.compare(c2.getDureeJours(), c1.getDureeJours()))
                .limit(5)
                .forEach(challenge -> {
                    Label item = new Label("• " + challenge.getTitre() + " (" + challenge.getDureeJours() + "j)");
                    item.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 12px; -fx-padding: 2 0;");
                    topChallengesList.getChildren().add(item);
                });

        if (topChallengesList.getChildren().isEmpty()) {
            Label empty = new Label("Aucun challenge disponible");
            empty.setStyle("-fx-text-fill: #95A5A6; -fx-font-style: italic; -fx-padding: 5;");
            topChallengesList.getChildren().add(empty);
        }

        // Top 5 coachs
        topCoachsList.getChildren().clear();
        coachs.stream()
                .limit(5)
                .forEach(coach -> {
                    Label item = new Label("• " + coach.getNomCoach() + " - " + coach.getStyle());
                    item.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 12px; -fx-padding: 2 0;");
                    topCoachsList.getChildren().add(item);
                });

        if (topCoachsList.getChildren().isEmpty()) {
            Label empty = new Label("Aucun coach disponible");
            empty.setStyle("-fx-text-fill: #95A5A6; -fx-font-style: italic; -fx-padding: 5;");
            topCoachsList.getChildren().add(empty);
        }

        // Top 5 récompenses
        topRecompensesList.getChildren().clear();
        recompenses.stream()
                .limit(5)
                .forEach(recompense -> {
                    Label item = new Label("• " + recompense.getTitre() + " (" + recompense.getTypeRecompense() + ")");
                    item.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 12px; -fx-padding: 2 0;");
                    topRecompensesList.getChildren().add(item);
                });

        if (topRecompensesList.getChildren().isEmpty()) {
            Label empty = new Label("Aucune récompense disponible");
            empty.setStyle("-fx-text-fill: #95A5A6; -fx-font-style: italic; -fx-padding: 5;");
            topRecompensesList.getChildren().add(empty);
        }
    }

    private void initialiserHorloge() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            dateActuelleLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
            heureActuelleLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    private void rafraichirDonnees() {
        chargerStatistiques();
    }



    @FXML
    private void retournerVersChallenges() {
        try {
            // Charger le fichier FXML de la page des challenges
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) logoAccueil.getScene().getWindow();

            // Changer de scène
            stage.setScene(new Scene(root));
            stage.setTitle("Challenge Manager Pro");
            stage.centerOnScreen();

            // Animation de transition (optionnelle)
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), root
            );
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de retourner à la page des challenges");
        }
    }

    // Ajoutez aussi cette méthode utilitaire si elle n'existe pas déjà
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}