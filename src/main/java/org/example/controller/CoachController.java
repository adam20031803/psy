package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.example.dao.CoachMotivationCrud;
import org.example.dao.ChallengeCoachCrud;
import org.example.model.CoachMotivation;
import org.example.model.Challenge;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;



// ================= IMPORTS POUR EXPORT PDF =================
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.awt.Color;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.text.Normalizer;

public class CoachController implements Initializable {

    // ================= LISTVIEW =================
    @FXML private ListView<CoachMotivation> coachListView;

    // ================= FORMULAIRE =================
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;
    @FXML private Button challengeBtn;
    @FXML private Button recBtn;
    @FXML private Button exportBtn;

    @FXML private TextField nomField;
    @FXML private ComboBox<String> styleComboBox;
    @FXML private TextArea descField;
    @FXML private CheckBox actifField;
    @FXML private TextField specialitesField;

    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;

    // ================= STATISTIQUES =================
    @FXML private Label totalLabel;
    @FXML private Label actifsLabel;
    @FXML private Label stylesLabel;
    @FXML private Label popularStyleLabel;
    @FXML private Label engagementLabel;

    @FXML private Label selectedCoachLabel;
    @FXML private Label challengesCountLabel;
    @FXML private Label dateAjoutLabel;
    @FXML private Label successRateLabel;

    private ObservableList<CoachMotivation> coachList = FXCollections.observableArrayList();
    private CoachMotivationCrud coachCrud = new CoachMotivationCrud();
    private final ChallengeCoachCrud challengeCoachCrud = new ChallengeCoachCrud();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALIZATION COACH CONTROLLER START ===");

        setupListView();
        setupComboBox();
        setupButtons();
        setupInputValidation();
        loadData();
        updateStatistics();

        System.out.println("=== INITIALIZATION COACH CONTROLLER END ===");
    }

    private void setupListView() {
        coachListView.setCellFactory(param -> new ListCell<CoachMotivation>() {
            private final HBox card = new HBox(15);
            private final VBox content = new VBox(8);
            private final HBox header = new HBox(10);
            private final HBox infoRow = new HBox(15);
            private final HBox bottomRow = new HBox(10);
            private final VBox rightColumn = new VBox(5);

            private final Label nameLabel = new Label();
            private final Label statusLabel = new Label();
            private final Label styleLabel = new Label();
            private final Label descLabel = new Label();
            private final Label challengesLabel = new Label();

            private final HBox actionBox = new HBox(5);
            private final Button detailsBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");

            {
                // Configuration initiale des composants
                card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 15; " +
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-cursor: hand;");
                card.setAlignment(Pos.CENTER_LEFT);
                card.setMaxWidth(Double.MAX_VALUE);

                // Configuration du header
                header.setAlignment(Pos.CENTER_LEFT);

                nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: black;");
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(250);

                statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");

                // Style
                styleLabel.setStyle("-fx-text-fill: #9B59B6; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Description
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
                descLabel.setMaxWidth(350);
                descLabel.setMaxHeight(40);

                // Configuration de la ligne d'info
                infoRow.setAlignment(Pos.CENTER_LEFT);
                infoRow.getChildren().addAll(styleLabel);

                // Configuration de la ligne du bas
                bottomRow.setAlignment(Pos.CENTER_LEFT);
                bottomRow.setSpacing(15);

                // Label pour challenges
                challengesLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");

                // Configuration de la colonne de droite
                rightColumn.setAlignment(Pos.CENTER_RIGHT);
                rightColumn.setMinWidth(100);

                // Configuration des boutons d'action
                actionBox.setAlignment(Pos.CENTER_RIGHT);

                detailsBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 8 17; " +
                        "-fx-font-size: 20px; -fx-cursor: hand;");
                detailsBtn.setTooltip(new Tooltip("Voir les détails"));

                editBtn.setStyle("-fx-background-color: linear-gradient(to right, #9B59B6, #8E44AD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 8 17; " +
                        "-fx-font-size: 20px; -fx-cursor: hand;");
                editBtn.setTooltip(new Tooltip("Modifier"));

                actionBox.getChildren().addAll(detailsBtn, editBtn);

                // Assemblage des composants
                VBox leftContent = new VBox(10);
                leftContent.getChildren().addAll(header, descLabel, infoRow, bottomRow);

                HBox mainContent = new HBox(15);
                mainContent.setAlignment(Pos.CENTER_LEFT);
                mainContent.getChildren().addAll(leftContent, rightColumn);

                content.getChildren().addAll(mainContent, actionBox);

                VBox mainCard = new VBox(10);
                mainCard.getChildren().addAll(content);

                card.getChildren().add(mainCard);

                // Effet au survol
                card.setOnMouseEntered(e -> {
                    card.setStyle(card.getStyle() +
                            "-fx-background-color: rgba(255,255,255,0.12); " +
                            "-fx-border-color: rgba(255,255,255,0.2); " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 3);");
                });

                card.setOnMouseExited(e -> {
                    card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 15; " +
                            "-fx-border-color: rgba(255,255,255,0.1); " +
                            "-fx-border-radius: 12; " +
                            "-fx-border-width: 1; " +
                            "-fx-effect: null;");
                });

                // Gestion des clics
                card.setOnMouseClicked(e -> {
                    CoachMotivation coach = getItem();
                    if (coach != null) {
                        coachListView.getSelectionModel().select(coach);
                        loadCoachData(coach);
                        updateCoachStatistics(coach);
                    }
                });
            }

            @Override
            protected void updateItem(CoachMotivation coach, boolean empty) {
                super.updateItem(coach, empty);

                if (empty || coach == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Mise à jour des données
                    nameLabel.setText("👤 " + coach.getNomCoach());

                    // Statut avec badge coloré
                    if (coach.isActif()) {
                        statusLabel.setText("✅ ACTIF");
                        statusLabel.setStyle(statusLabel.getStyle() +
                                "-fx-background-color: rgba(46,204,113,0.2); " +
                                "-fx-text-fill: #2ECC71;");
                    } else {
                        statusLabel.setText("❌ INACTIF");
                        statusLabel.setStyle(statusLabel.getStyle() +
                                "-fx-background-color: rgba(231,76,60,0.2); " +
                                "-fx-text-fill: #E74C3C;");
                    }

                    // Style
                    styleLabel.setText("🎭 " + coach.getStyle());

                    // Description tronquée si trop longue
                    String desc = coach.getDescription();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 97) + "...";
                    }
                    descLabel.setText(desc);

                    // Mise à jour du header
                    header.getChildren().clear();
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    header.getChildren().addAll(nameLabel, spacer, statusLabel);

                    // Challenges associés
                    int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());
                    if (challengeCount == 0) {
                        challengesLabel.setText("🏆 Aucun challenge");
                        challengesLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                    } else {
                        challengesLabel.setText("🏆 " + challengeCount + " challenge" + (challengeCount != 1 ? "s" : ""));
                        challengesLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }

                    // Mise à jour de la ligne du bas
                    bottomRow.getChildren().clear();
                    bottomRow.getChildren().addAll(challengesLabel);

                    // Configuration de la colonne de droite (statistiques)
                    rightColumn.getChildren().clear();

                    VBox statsBox = new VBox(5);
                    statsBox.setAlignment(Pos.CENTER_RIGHT);

                    Label idLabel = new Label("#" + coach.getIdCoach());
                    idLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");

                    // Badge pour le nombre de challenges
                    HBox challengeBadge = new HBox(5);
                    challengeBadge.setAlignment(Pos.CENTER);
                    Label challengeIcon = new Label("🏆");
                    Label challengeCountLabel = new Label(String.valueOf(challengeCount));
                    challengeCountLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: black;");
                    challengeBadge.setStyle("-fx-background-color: rgba(155,89,182,0.2); -fx-background-radius: 10; -fx-padding: 3 8;");
                    challengeBadge.getChildren().addAll(challengeIcon, challengeCountLabel);

                    statsBox.getChildren().addAll(idLabel, challengeBadge);
                    rightColumn.getChildren().add(statsBox);

                    // Mise à jour des actions des boutons
                    detailsBtn.setOnAction(e -> showCoachDetail(coach));
                    editBtn.setOnAction(e -> {
                        coachListView.getSelectionModel().select(coach);
                        loadCoachData(coach);
                        updateCoachStatistics(coach);
                    });

                    setGraphic(card);
                }
            }
        });

        // Sélection d'un élément
        coachListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        loadCoachData(newVal);
                        updateCoachStatistics(newVal);
                    }
                }
        );

        // Style de la ListView
        coachListView.setStyle("-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-padding: 0;");
    }

    private void setupComboBox() {
        // Initialiser les options du ComboBox de style
        styleComboBox.getItems().addAll(
                "Motivation",
                "Leadership",
                "Productivité",
                "Mindset",
                "Gestion du temps",
                "Développement personnel",
                "Coaching sportif",
                "Coaching professionnel",
                "Autre"
        );

        // Valeur par défaut
        if (!styleComboBox.getItems().isEmpty()) {
            styleComboBox.setValue("Motivation");
        }
    }

    private void setupButtons() {
        addBtn.setOnAction(e -> addCoach());
        updateBtn.setOnAction(e -> updateCoach());
        deleteBtn.setOnAction(e -> deleteCoach());
        clearBtn.setOnAction(e -> clearForm());
        searchBtn.setOnAction(e -> searchCoaches());
        refreshBtn.setOnAction(e -> loadData());

        // Navigation
        challengeBtn.setOnAction(e -> goToChallenges());
        recBtn.setOnAction(e -> goToRecompenses());
        exportBtn.setOnAction(e -> exportToPDF());
    }

    // ==================== CONTRÔLE DE SAISIE ====================
    private void setupInputValidation() {
        // Validation pour le nom du coach
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (Character.isDigit(newValue.charAt(0))) {
                    showFieldError(nomField, "Le nom ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(nomField, "Le nom ne doit pas être composé uniquement de chiffres");
                    return;
                }
                if (!newValue.matches(".*[a-zA-Z].*")) {
                    showFieldError(nomField, "Le nom doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 50) {
                    showFieldError(nomField, "Le nom ne doit pas dépasser 50 caractères");
                    return;
                }
                clearFieldError(nomField);
            }
        });

        // Validation pour la description
        descField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (newValue.length() > 0 && Character.isDigit(newValue.charAt(0))) {
                    showFieldError(descField, "La description ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(descField, "La description ne doit pas être composée uniquement de chiffres");
                    return;
                }
                if (!newValue.matches("(?s).*[\\p{L}\\p{M}].*")){
                    showFieldError(descField, "La description doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 300) {
                    showFieldError(descField, "La description ne doit pas dépasser 300 caractères");
                    return;
                }
                clearFieldError(descField);
            }
        });
    }

    private void showFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        if (field instanceof TextInputControl) {
            ((TextInputControl) field).setTooltip(new Tooltip(message));
        } else if (field instanceof ComboBox) {
            field.setTooltip(new Tooltip(message));
        }
    }

    private void clearFieldError(Control field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du nom
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showFieldError(nomField, "Le nom du coach est obligatoire");
            showAlert("Validation", "Le nom du coach est obligatoire", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (Character.isDigit(nom.charAt(0))) {
            showFieldError(nomField, "Le nom ne doit pas commencer par un chiffre");
            showAlert("Validation", "Le nom ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (nom.matches("\\d+")) {
            showFieldError(nomField, "Le nom ne doit pas être composé uniquement de chiffres");
            showAlert("Validation", "Le nom ne doit pas être composé uniquement de chiffres", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (!nom.matches(".*[a-zA-Z].*")) {
            showFieldError(nomField, "Le nom doit contenir au moins une lettre");
            showAlert("Validation", "Le nom doit contenir au moins une lettre", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else if (nom.length() > 50) {
            showFieldError(nomField, "Le nom ne doit pas dépasser 50 caractères");
            showAlert("Validation", "Le nom ne doit pas dépasser 50 caractères", Alert.AlertType.WARNING);
            nomField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(nomField);
        }

        // Validation du style (ComboBox)
        String style = styleComboBox.getValue();
        if (style == null || style.isEmpty()) {
            showFieldError(styleComboBox, "Le style est obligatoire");
            if (isValid) {
                showAlert("Validation", "Le style est obligatoire", Alert.AlertType.WARNING);
                styleComboBox.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(styleComboBox);
        }

        // Validation de la description
        String description = descField.getText().trim();
        if (description.isEmpty()) {
            showFieldError(descField, "La description est obligatoire");
            if (isValid) {
                showAlert("Validation", "La description est obligatoire", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.length() > 0 && Character.isDigit(description.charAt(0))) {
            showFieldError(descField, "La description ne doit pas commencer par un chiffre");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.matches("\\d+")) {
            showFieldError(descField, "La description ne doit pas être composée uniquement de chiffres");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas être composée uniquement de chiffres", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (!description.matches("(?s).*[\\p{L}\\p{M}].*")) {
            showFieldError(descField, "La description doit contenir au moins une lettre");
            if (isValid) {
                showAlert("Validation", "La description doit contenir au moins une lettre", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.length() > 300) {
            showFieldError(descField, "La description ne doit pas dépasser 300 caractères");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas dépasser 300 caractères", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(descField);
        }

        return isValid;
    }

    private void searchCoaches() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        List<CoachMotivation> allCoaches = coachCrud.readAll();
        ObservableList<CoachMotivation> filteredList = FXCollections.observableArrayList();

        for (CoachMotivation coach : allCoaches) {
            if (coach.getNomCoach().toLowerCase().contains(keyword) ||
                    coach.getStyle().toLowerCase().contains(keyword) ||
                    coach.getDescription().toLowerCase().contains(keyword)) {
                filteredList.add(coach);
            }
        }

        coachList.setAll(filteredList);
        updateStatistics();
    }

    private void loadData() {
        try {
            List<CoachMotivation> coaches = coachCrud.readAll();
            coachList.setAll(coaches);
            coachListView.setItems(coachList);

            // Mettre à jour les statistiques
            updateStatistics();
            updateStyleStatistics(coaches);

            System.out.println("Chargement terminé. " + coaches.size() + " coach(s) trouvé(s).");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCoach() {
        if (!validateForm()) return;

        try {
            CoachMotivation coach = new CoachMotivation(
                    nomField.getText().trim(),
                    styleComboBox.getValue(),
                    descField.getText().trim()
            );

            coach.setActif(actifField.isSelected());

            coachCrud.create(coach);
            showAlert("Succès", "Coach ajouté avec succès!", Alert.AlertType.INFORMATION);
            clearForm();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateCoach() {
        CoachMotivation selected = coachListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Sélectionnez un coach à modifier", Alert.AlertType.WARNING);
            return;
        }

        if (!validateForm()) return;

        try {
            selected.setNomCoach(nomField.getText().trim());
            selected.setStyle(styleComboBox.getValue());
            selected.setDescription(descField.getText().trim());
            selected.setActif(actifField.isSelected());

            coachCrud.update(selected);
            showAlert("Succès", "Coach modifié avec succès!", Alert.AlertType.INFORMATION);
            coachListView.refresh();
            updateStatistics();
            updateStyleStatistics(coachList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteCoach() {
        CoachMotivation selected = coachListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Sélectionnez un coach à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le coach");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getNomCoach() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    coachCrud.delete(selected.getIdCoach());
                    showAlert("Succès", "Coach désactivé!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void loadCoachData(CoachMotivation coach) {
        nomField.setText(coach.getNomCoach());
        styleComboBox.setValue(coach.getStyle());
        descField.setText(coach.getDescription());
        actifField.setSelected(coach.isActif());

        // Effacer les erreurs de validation lors du chargement
        clearFieldError(nomField);
        clearFieldError(styleComboBox);
        clearFieldError(descField);
    }

    private void clearForm() {
        nomField.clear();
        styleComboBox.setValue("Motivation");
        descField.clear();
        if (specialitesField != null) {
            specialitesField.clear();
        }
        actifField.setSelected(true);
        coachListView.getSelectionModel().clearSelection();

        // Effacer toutes les erreurs de validation
        clearFieldError(nomField);
        clearFieldError(styleComboBox);
        clearFieldError(descField);

        // Réinitialiser les statistiques du coach sélectionné
        if (selectedCoachLabel != null) selectedCoachLabel.setText("Aucun coach sélectionné");
        if (challengesCountLabel != null) challengesCountLabel.setText("0");
        if (dateAjoutLabel != null) dateAjoutLabel.setText("--/--/----");
        if (successRateLabel != null) successRateLabel.setText("--%");
    }

    private void goToChallenges() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) challengeBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Gestion des Challenges");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir aux challenges", Alert.AlertType.ERROR);
        }
    }

    private void goToRecompenses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RecompenseView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) recBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Gestion des Récompenses");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des récompenses", Alert.AlertType.ERROR);
        }
    }

   /* private void exportToPDF() {
        // À implémenter: exporter les coaches en PDF
        showAlert("Export PDF", "Fonction d'export PDF à implémenter", Alert.AlertType.INFORMATION);
    }*/

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatistics() {
        if (coachList.isEmpty()) {
            if (totalLabel != null) totalLabel.setText("0");
            if (actifsLabel != null) actifsLabel.setText("0");
            if (stylesLabel != null) stylesLabel.setText("0");
            if (popularStyleLabel != null) popularStyleLabel.setText("Aucun");
            if (engagementLabel != null) engagementLabel.setText("0%");
            return;
        }

        // Calcul des statistiques
        int total = coachList.size();
        long actifs = coachList.stream().filter(CoachMotivation::isActif).count();

        // Mise à jour des labels avec vérification null
        if (totalLabel != null) totalLabel.setText(String.valueOf(total));
        if (actifsLabel != null) actifsLabel.setText(String.valueOf(actifs));

        // Compter les styles uniques
        long uniqueStyles = coachList.stream()
                .map(CoachMotivation::getStyle)
                .distinct()
                .count();

        if (stylesLabel != null) stylesLabel.setText(String.valueOf(uniqueStyles));

        // Calcul du taux d'engagement
        if (engagementLabel != null) {
            long activeCoaches = coachList.stream().filter(CoachMotivation::isActif).count();
            int engagementRate = total > 0 ? (int) ((activeCoaches * 100) / total) : 0;
            engagementLabel.setText(engagementRate + "%");
        }
    }

    private void updateStyleStatistics(List<CoachMotivation> coaches) {
        if (coaches.isEmpty()) {
            if (popularStyleLabel != null) popularStyleLabel.setText("Aucun");
            return;
        }

        // Trouver le style le plus populaire
        // Simplifié pour l'exemple
        if (popularStyleLabel != null) {
            // Ici vous pourriez ajouter une logique pour trouver le style le plus fréquent
            popularStyleLabel.setText("Motivation");
        }
    }

    private void updateCoachStatistics(CoachMotivation coach) {
        if (coach == null) return;

        if (selectedCoachLabel != null) {
            selectedCoachLabel.setText(coach.getNomCoach());
        }

        // Récupérer le nombre de challenges assignés
        if (challengesCountLabel != null) {
            int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());
            challengesCountLabel.setText(String.valueOf(challengeCount));
        }

        // Date d'ajout (simulée - vous pourriez ajouter une date dans votre modèle)
        if (dateAjoutLabel != null) {
            dateAjoutLabel.setText("--/--/----");
        }

        // Taux de succès (simulé)
        if (successRateLabel != null) {
            successRateLabel.setText("85%");
        }
    }

    // Méthode pour afficher les détails du coach
    private void showCoachDetail(CoachMotivation coach) {
        // À implémenter: fenêtre modale avec détails complets
        showAlert("Détails Coach",
                "Nom: " + coach.getNomCoach() + "\n" +
                        "Style: " + coach.getStyle() + "\n" +
                        "Description: " + coach.getDescription() + "\n" +
                        "Statut: " + (coach.isActif() ? "Actif" : "Inactif") + "\n" +
                        "Challenges assignés: " + challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach()),
                Alert.AlertType.INFORMATION);
    }




    /* ================= EXPORT PDF ================= */
    /* ================= EXPORT PDF ================= */
    /* ================= EXPORT PDF ================= */
    private void exportToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les coaches en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            fileChooser.setInitialFileName("coaches_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

            File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());

            if (file != null) {
                generateProfessionalPDF(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur d'exportation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void generateProfessionalPDF(File file) {
        try {
            PDDocument document = new PDDocument();

            // Page 1: Page de garde
            createCoverPage(document);

            // Page 2: Statistiques et graphiques
            createStatisticsPage(document);

            // Pages suivantes: Details des coaches
            createCoachDetailsPages(document);

            // Sauvegarder le document
            document.save(file);
            document.close();

            showAlert("Succes",
                    "PDF genere avec succes!\n\n" +
                            "Fichier: " + file.getName() + "\n" +
                            "Chemin: " + file.getAbsolutePath() + "\n" +
                            "Nombre de coaches: " + coachList.size(),
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la generation du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createCoverPage(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Fond colore en haut
            contentStream.setNonStrokingColor(new Color(155, 89, 182));
            contentStream.addRect(0, pageHeight - 200, pageWidth, 200);
            contentStream.fill();

            // Titre principal
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
            contentStream.newLineAtOffset(50, pageHeight - 100);
            contentStream.showText("RAPPORT DES COACHES");
            contentStream.endText();

            // Sous-titre
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 16);
            contentStream.newLineAtOffset(50, pageHeight - 140);
            contentStream.showText("Analyse complete et statistiques");
            contentStream.endText();

            // Date de generation
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(50, pageHeight - 250);
            contentStream.showText("Date de generation: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Statistiques rapides (cartes colorees)
            float cardY = pageHeight - 500;
            float cardHeight = 100;
            float cardWidth = 150;
            float spacing = 20;

            long actifs = coachList.stream().filter(CoachMotivation::isActif).count();
            long inactifs = coachList.size() - actifs;

            // Carte 1: Total
            drawStatCard(contentStream, 50, cardY, cardWidth, cardHeight,
                    new Color(155, 89, 182), "TOTAL",
                    String.valueOf(coachList.size()), "Coaches");

            // Carte 2: Actifs
            drawStatCard(contentStream, 50 + cardWidth + spacing, cardY, cardWidth, cardHeight,
                    new Color(46, 204, 113), "ACTIFS",
                    String.valueOf(actifs), "Coaches");

            // Carte 3: Inactifs
            drawStatCard(contentStream, 50 + 2 * (cardWidth + spacing), cardY, cardWidth, cardHeight,
                    new Color(231, 76, 60), "INACTIFS",
                    String.valueOf(inactifs), "Coaches");

            // Pied de page avec ligne decorative
            contentStream.setStrokingColor(new Color(155, 89, 182));
            contentStream.setLineWidth(3);
            contentStream.moveTo(50, 100);
            contentStream.lineTo(pageWidth - 50, 100);
            contentStream.stroke();

            // Texte du pied de page
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
            contentStream.newLineAtOffset(50, 70);
            contentStream.showText("Genere automatiquement par le systeme de gestion des coaches");
            contentStream.endText();
        }
    }

    private void drawStatCard(PDPageContentStream contentStream, float x, float y,
                              float width, float height, Color color,
                              String label, String value, String unit) throws Exception {
        // Fond de la carte
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();

        // Label
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(x + 10, y + height - 25);
        contentStream.showText(label);
        contentStream.endText();

        // Valeur (grand)
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 32);
        contentStream.newLineAtOffset(x + 10, y + height - 65);
        contentStream.showText(value);
        contentStream.endText();

        // Unite
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(x + 10, y + 15);
        contentStream.showText(unit);
        contentStream.endText();
    }

    private void createStatisticsPage(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // En-tete de la page
            contentStream.setNonStrokingColor(new Color(155, 89, 182));
            contentStream.addRect(0, pageHeight - 80, pageWidth, 80);
            contentStream.fill();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.newLineAtOffset(50, pageHeight - 50);
            contentStream.showText("STATISTIQUES DETAILLEES");
            contentStream.endText();

            float currentY = pageHeight - 120;

            // Section: Repartition par style
            currentY = drawSectionTitle(contentStream, currentY, "Repartition par style");
            currentY -= 20;

            Map<String, Long> styleStats = coachList.stream()
                    .collect(Collectors.groupingBy(CoachMotivation::getStyle, Collectors.counting()));

            for (Map.Entry<String, Long> entry : styleStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, entry.getKey(), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Statut des coaches
            currentY = drawSectionTitle(contentStream, currentY, "Statut des coaches");
            currentY -= 20;

            long actifs = coachList.stream().filter(CoachMotivation::isActif).count();
            long inactifs = coachList.size() - actifs;

            currentY = drawStatLine(contentStream, currentY, "Coaches actifs", String.valueOf(actifs));
            currentY = drawStatLine(contentStream, currentY, "Coaches inactifs", String.valueOf(inactifs));

            // Numero de page
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(pageWidth - 80, 30);
            contentStream.showText("Page 2");
            contentStream.endText();
        }
    }

    private float drawSectionTitle(PDPageContentStream contentStream, float y, String title) throws Exception {
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(title);
        contentStream.endText();

        // Ligne sous le titre
        contentStream.setStrokingColor(new Color(155, 89, 182));
        contentStream.setLineWidth(2);
        contentStream.moveTo(50, y - 5);
        contentStream.lineTo(300, y - 5);
        contentStream.stroke();

        return y - 10;
    }

    private float drawStatLine(PDPageContentStream contentStream, float y, String label, String value) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.newLineAtOffset(70, y);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(155, 89, 182));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(300, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - 25;
    }

    private void createCoachDetailsPages(PDDocument document) throws Exception {
        int pageNumber = 3;
        int coachesPerPage = 2;

        for (int i = 0; i < coachList.size(); i += coachesPerPage) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // En-tete de la page
                contentStream.setNonStrokingColor(new Color(155, 89, 182));
                contentStream.addRect(0, pageHeight - 60, pageWidth, 60);
                contentStream.fill();

                contentStream.beginText();
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.newLineAtOffset(50, pageHeight - 40);
                contentStream.showText("DETAIL DES COACHES");
                contentStream.endText();

                float currentY = pageHeight - 100;

                for (int j = i; j < Math.min(i + coachesPerPage, coachList.size()); j++) {
                    CoachMotivation coach = coachList.get(j);
                    currentY = drawCoachCard(contentStream, coach, currentY, pageWidth);
                    currentY -= 40;
                }

                // Numero de page
                contentStream.beginText();
                contentStream.setNonStrokingColor(new Color(127, 140, 141));
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(pageWidth - 80, 30);
                contentStream.showText("Page " + pageNumber);
                contentStream.endText();
            }
            pageNumber++;
        }
    }

    private float drawCoachCard(PDPageContentStream contentStream, CoachMotivation coach,
                                float y, float pageWidth) throws Exception {
        float cardHeight = 200;
        float margin = 50;
        float cardWidth = pageWidth - 2 * margin;

        Color cardColor = coach.isActif() ?
                new Color(236, 240, 241) : new Color(250, 235, 235);

        contentStream.setNonStrokingColor(cardColor);
        contentStream.addRect(margin, y - cardHeight, cardWidth, cardHeight);
        contentStream.fill();

        Color borderColor = coach.isActif() ?
                new Color(46, 204, 113) : new Color(231, 76, 60);

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(margin, y - cardHeight, 10, cardHeight);
        contentStream.fill();

        float textX = margin + 20;
        float textY = y - 25;

        // Nom du coach
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(44, 62, 80));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.newLineAtOffset(textX, textY);
        String nom = coach.getNomCoach();
        if (nom.length() > 40) {
            nom = nom.substring(0, 37) + "...";
        }
        contentStream.showText("Coach: " + removeAccents(nom));
        contentStream.endText();

        textY -= 25;

        // Style
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(155, 89, 182));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(textX, textY);
        contentStream.showText("Style: " + removeAccents(coach.getStyle()));
        contentStream.endText();

        textY -= 25;

        // Description
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(textX, textY);
        String desc = coach.getDescription();
        if (desc.length() > 80) {
            desc = desc.substring(0, 77) + "...";
        }
        contentStream.showText(removeAccents(desc));
        contentStream.endText();

        textY -= 25;

        int challengeCount = challengeCoachCrud.getChallengeCountForCoach(coach.getIdCoach());

        // ID
        drawInfoItem(contentStream, textX, textY, "ID:", String.valueOf(coach.getIdCoach()));

        // Statut
        drawInfoItem(contentStream, textX + 200, textY, "Statut:",
                coach.isActif() ? "ACTIF" : "INACTIF");

        textY -= 20;

        // Challenges
        drawInfoItem(contentStream, textX, textY, "Challenges:",
                String.valueOf(challengeCount));

        // Badge de statut
        float badgeX = margin + cardWidth - 100;
        float badgeY = y - 30;

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(badgeX, badgeY, 80, 25);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(badgeX + 10, badgeY + 8);
        contentStream.showText(coach.isActif() ? "ACTIF" : "INACTIF");
        contentStream.endText();

        return y - cardHeight;
    }

    private void drawInfoItem(PDPageContentStream contentStream, float x, float y,
                              String label, String value) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(127, 140, 141));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(x + 60, y);
        contentStream.showText(removeAccents(value));
        contentStream.endText();
    }

    // Methode utilitaire pour supprimer les accents
    private String removeAccents(String text) {
        if (text == null) return "";

        text = text.replace("\n", " ")
                .replace("\r", " ");

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}