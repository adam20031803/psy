package org.example.controller;

import javafx.beans.property.SimpleIntegerProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.motivation.ChallengeCoachCrud;
import org.example.dao.motivation.ChallengeCrud;
import org.example.dao.motivation.CoachMotivationCrud;
import org.example.model.motivation.CoachMotivation;
import org.example.model.motivation.Challenge;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManageChallengeCoachesController implements Initializable {

    @FXML
    private TableView<Challenge> challengeTable;
    @FXML
    private TableColumn<Challenge, Integer> idColumn;
    @FXML
    private TableColumn<Challenge, String> titleColumn;
    @FXML
    private TableColumn<Challenge, String> descColumn;
    @FXML
    private TableColumn<Challenge, Integer> coachCountColumn;
    @FXML
    private TableColumn<Challenge, Void> actionsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button backBtn;
    @FXML
    private Label totalLabel;
    @FXML
    private Label coachesTotalLabel;

    private final ObservableList<Challenge> challengeList = FXCollections.observableArrayList();
    private final ChallengeCoachCrud challengeCoachCrud = new ChallengeCoachCrud();
    private final ChallengeCrud challengeCrud = new ChallengeCrud();
    private final CoachMotivationCrud coachCrud = new CoachMotivationCrud();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupButtons();
        loadData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idChallenge"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Colonne nombre de coaches
        coachCountColumn.setCellValueFactory(cellData -> {
            Challenge challenge = cellData.getValue();
            List<CoachMotivation> coaches = challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge());
            return new SimpleIntegerProperty(coaches.size()).asObject();
        });

        coachCountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("👥 " + count);
                    if (count == 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (count <= 2) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colonne actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button manageButton = new Button("👨‍🏫 Gérer Coaches");
            private final Button detailsButton = new Button("📋 Détails");

            {
                manageButton.setStyle(
                        "-fx-background-color: #3498DB; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 5 10; " +
                                "-fx-cursor: hand;");
                manageButton.setOnAction(event -> {
                    Challenge challenge = getTableView().getItems().get(getIndex());
                    openCoachManager(challenge);
                });

                detailsButton.setStyle(
                        "-fx-background-color: #2ECC71; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 5 10; " +
                                "-fx-cursor: hand;");
                detailsButton.setOnAction(event -> {
                    Challenge challenge = getTableView().getItems().get(getIndex());
                    showChallengeDetails(challenge);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttonBox = new HBox(10, manageButton, detailsButton);
                    buttonBox.setAlignment(Pos.CENTER);
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void setupButtons() {
        searchBtn.setOnAction(e -> searchChallenges());
        backBtn.setOnAction(e -> goBack());
    }

    private void loadData() {
        try {
            challengeList.setAll(challengeCrud.readAll());
            challengeTable.setItems(challengeList);
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des données", Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        if (challengeList.isEmpty()) {
            totalLabel.setText("0");
            coachesTotalLabel.setText("0");
            return;
        }

        int totalChallenges = challengeList.size();
        int totalCoaches = 0;

        for (Challenge challenge : challengeList) {
            totalCoaches += challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge()).size();
        }

        totalLabel.setText(String.valueOf(totalChallenges));
        coachesTotalLabel.setText(String.valueOf(totalCoaches));
    }

    private void searchChallenges() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        challengeList.setAll(
                challengeCrud.readAll().stream()
                        .filter(c -> c.getTitre().toLowerCase().contains(keyword) ||
                                c.getDescription().toLowerCase().contains(keyword) ||
                                c.getTypeChallenge().toLowerCase().contains(keyword))
                        .collect(Collectors.toList()));
        updateStatistics();
    }

    private void openCoachManager(Challenge challenge) {
        try {
            Stage stage = new Stage();
            stage.setTitle("👨‍🏫 Gérer les coaches - " + challenge.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(20);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f5f5f5;");

            // En-tête
            Label titleLabel = new Label("👨‍🏫 Gérer les coaches pour : " + challenge.getTitre());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            // Liste des coaches actuels
            ListView<CoachMotivation> currentList = new ListView<>();
            ObservableList<CoachMotivation> currentCoaches = FXCollections.observableArrayList(
                    challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge()));
            currentList.setItems(currentCoaches);
            currentList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(CoachMotivation coach, boolean empty) {
                    super.updateItem(coach, empty);
                    if (empty || coach == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);

                        Label nameLabel = new Label("👤 " + coach.getNomCoach());
                        nameLabel.setStyle("-fx-font-weight: bold;");

                        Label styleLabel = new Label("🎭 " + coach.getStyle());
                        styleLabel.setStyle("-fx-text-fill: #7F8C8D;");

                        Button removeButton = new Button("❌");
                        removeButton.setStyle(
                                "-fx-background-color: #E74C3C; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-padding: 2 5;");
                        removeButton.setOnAction(e -> {
                            boolean success = challengeCoachCrud.dissociateCoachFromChallenge(
                                    challenge.getIdChallenge(), coach.getIdCoach());
                            if (success) {
                                currentCoaches.remove(coach);
                                showAlert("Succès", "Coach retiré avec succès", Alert.AlertType.INFORMATION);
                            }
                        });

                        hbox.getChildren().addAll(nameLabel, styleLabel, removeButton);
                        setGraphic(hbox);
                    }
                }
            });

            VBox currentBox = new VBox(10);
            currentBox.getChildren().addAll(
                    new Label("✅ Coaches assignés (" + currentCoaches.size() + ") :"),
                    currentList);

            // Bouton pour ajouter un nouveau coach
            Button addNewButton = new Button("➕ Ajouter un coach");
            addNewButton.setStyle(
                    "-fx-background-color: #2ECC71; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10 20;");
            addNewButton.setOnAction(e -> {
                openCoachSelector(challenge, currentCoaches);
            });

            // Boutons de contrôle
            HBox buttonBox = new HBox(15);
            Button saveButton = new Button("💾 Enregistrer");
            saveButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
            saveButton.setOnAction(e -> {
                showAlert("Succès", "Modifications enregistrées", Alert.AlertType.INFORMATION);
                stage.close();
                refreshTable();
            });

            Button cancelButton = new Button("❌ Fermer");
            cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white;");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(saveButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(
                    titleLabel,
                    currentBox,
                    addNewButton,
                    buttonBox);

            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le gestionnaire", Alert.AlertType.ERROR);
        }
    }

    private void openCoachSelector(Challenge challenge, ObservableList<CoachMotivation> currentList) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Sélectionner des coaches");
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));

            Label titleLabel = new Label("👤 Sélectionnez les coaches à ajouter :");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Récupérer tous les coaches disponibles
            List<CoachMotivation> allCoaches = coachCrud.readAll();

            // Filtrer ceux déjà assignés
            List<CoachMotivation> availableCoaches = allCoaches.stream()
                    .filter(c -> currentList.stream()
                            .noneMatch(cc -> cc.getIdCoach() == c.getIdCoach()))
                    .collect(Collectors.toList());

            ListView<CoachMotivation> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(availableCoaches));
            listView.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(CoachMotivation coach, boolean empty) {
                    super.updateItem(coach, empty);
                    if (empty || coach == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);

                        Label nameLabel = new Label("👤 " + coach.getNomCoach());
                        nameLabel.setStyle("-fx-font-weight: bold;");

                        Label styleLabel = new Label("🎭 Style: " + coach.getStyle());
                        styleLabel.setStyle("-fx-text-fill: #7F8C8D;");

                        Label statusLabel = new Label(coach.isActif() ? "✅" : "❌");
                        statusLabel.setStyle(coach.isActif() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

                        hbox.getChildren().addAll(nameLabel, styleLabel, statusLabel);
                        setGraphic(hbox);
                    }
                }
            });

            // Boutons
            HBox buttonBox = new HBox(10);
            Button addButton = new Button("➕ Ajouter sélection");
            addButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
            addButton.setOnAction(e -> {
                CoachMotivation selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    boolean success = challengeCoachCrud.associateCoachToChallenge(
                            challenge.getIdChallenge(),
                            selected.getIdCoach());
                    if (success) {
                        currentList.add(selected);
                        stage.close();
                    }
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(addButton, cancelButton);

            root.getChildren().addAll(titleLabel, listView, buttonBox);

            Scene scene = new Scene(root, 500, 400);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChallengeDetails(Challenge challenge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📋 Détails du Challenge");
        alert.setHeaderText("Challenge: " + challenge.getTitre());

        List<CoachMotivation> coaches = challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge());

        StringBuilder content = new StringBuilder();
        content.append("Titre: ").append(challenge.getTitre()).append("\n");
        content.append("Description: ").append(challenge.getDescription()).append("\n");
        content.append("Durée: ").append(challenge.getDureeJours()).append(" jours\n");
        content.append("Difficulté: ").append(challenge.getNiveauDifficulte()).append("\n");
        content.append("Type: ").append(challenge.getTypeChallenge()).append("\n");
        content.append("Statut: ").append(challenge.isActif() ? "Actif" : "Inactif").append("\n\n");

        content.append("👨‍🏫 Coaches assignés (").append(coaches.size()).append("):\n");
        if (coaches.isEmpty()) {
            content.append("  Aucun coach assigné\n");
        } else {
            for (CoachMotivation coach : coaches) {
                content.append("  • ").append(coach.getNomCoach())
                        .append(" (").append(coach.getStyle()).append(")\n");
            }
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void refreshTable() {
        challengeTable.refresh();
        updateStatistics();
    }

    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/motivation/MainView.fxml"));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}