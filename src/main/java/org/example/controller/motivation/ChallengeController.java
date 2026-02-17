package org.example.controller.motivation;

import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javafx.beans.property.SimpleStringProperty;
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
import org.example.dao.motivation.*;
import org.example.model.motivation.Challenge;
import org.example.model.motivation.Recompense;
import org.example.model.motivation.CoachMotivation;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// Nouveaux imports pour l'export PDF professionnel
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.scene.control.Tooltip;

import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

import java.awt.Color;
import javafx.scene.layout.StackPane;

import javafx.scene.Cursor;
import javafx.scene.shape.Circle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;

import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;

public class ChallengeController implements Initializable {

    /* ================= TABLE ================= */
    @FXML
    private ListView<Challenge> challengeListView;
    @FXML
    private TableColumn<Challenge, Integer> idColumn;
    @FXML
    private TableColumn<Challenge, String> titleColumn;
    @FXML
    private TableColumn<Challenge, String> descColumn;
    @FXML
    private TableColumn<Challenge, Integer> dureeColumn;
    @FXML
    private TableColumn<Challenge, String> difficulteColumn;
    @FXML
    private TableColumn<Challenge, String> typeColumn;
    @FXML
    private TableColumn<Challenge, Boolean> statusColumn;
    @FXML
    private TableColumn<Challenge, String> recompensesColumn;
    @FXML
    private TableColumn<Challenge, Void> actionsColumn;

    /* ================= FORM ================= */
    @FXML
    private TextField searchField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descField;
    @FXML
    private TextField dureeField;
    @FXML
    private ComboBox<String> difficulteField;
    @FXML
    private ComboBox<String> typeField;
    @FXML
    private CheckBox actifField;
    @FXML
    private TableColumn<Challenge, String> coachesColumn;

    @FXML
    private Button searchBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Button coachBtn;
    @FXML
    private Button recBtn;
    @FXML
    private Button exportBtn;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Label totalLabel;
    @FXML
    private Label actifsLabel;
    @FXML
    private Label inactifsLabel;

    /* ================= DATA ================= */
    // flexible au modification
    private final ObservableList<Challenge> challengeList = FXCollections.observableArrayList();
    private final ChallengeCrud challengeCrud = new ChallengeCrud();
    private final CrudCoach.ChallengeRecompenseCrud challengeRecompenseCrud = new CrudCoach.ChallengeRecompenseCrud();

    private final ChallengeCoachCrud challengeCoachCrud = new ChallengeCoachCrud();

    /* ================= INIT ================= */

    // Méthode appelée automatiquement par JavaFX après le chargement du fichier
    // FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALIZATION START ===");

        setupListView();
        setupForm();
        setupButtons();
        setupInputValidation();

        // Check Role
        checkRole();

        // Ensure the ListView always observes the main list
        challengeListView.setItems(challengeList);

        // Load data once - this will also trigger updateStatistics()
        loadData();

        System.out.println("=== INITIALIZATION END ===");
    }

    private void checkRole() {
        if (!org.example.util.Session.isAdmin()) {
            addBtn.setVisible(false);
            updateBtn.setVisible(false);
            deleteBtn.setVisible(false);
            // coachBtn.setVisible(false); // Visible for Users now
            // recBtn.setVisible(false); // Visible for Users now
            exportBtn.setVisible(false);

            // Disable actions column if needed, or hide it
            if (actionsColumn != null) {
                actionsColumn.setVisible(false);
            }

            // Hide form fields if desired, or make them read-only
            // For now, hiding buttons prevents actions.
            // You might want to hide the whole form container if users shouldn't see it.
        }
    }

    /*
     * configure un ListView JavaFX pour afficher des Challenge
     * sous forme de cartes personnalisées avec du style, des boutons et des
     * interactions.
     */
    private void setupListView() {
        // setCellFactory permet de personnaliser l’apparence et le comportement de
        // chaque cellule dans le ListView
        // on crée une classe anonyme qui hérite de ListCell<Challenge> pour définir le
        // rendu des challenges.
        challengeListView.setCellFactory(param -> new ListCell<Challenge>() {
            // Hbox:conteneur pour organiser les element horizentallement
            // Vbox:conteneur pour organiser les element verticallement
            private final HBox card = new HBox(15);
            private final VBox content = new VBox(8);
            private final HBox header = new HBox(10);
            private final HBox infoRow = new HBox(15);
            private final HBox bottomRow = new HBox(10);
            private final VBox rightColumn = new VBox(5);

            private final Label titleLabel = new Label();
            private final Label statusLabel = new Label();
            private final Label descLabel = new Label();
            private final Label dureeLabel = new Label();
            private final Label diffLabel = new Label();
            private final Label typeLabel = new Label();
            private final Label coachesLabel = new Label();
            private final Label rewardsLabel = new Label();

            private final HBox actionBox = new HBox(5);
            private final Button coachesBtn = new Button("👨‍🏫");
            private final Button rewardsBtn = new Button("🎁");
            private final Button detailsBtn = new Button("👁️");

            {
                // Configuration initiale des composants

                // Style de la carte principale
                card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 15; " +
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1; " +
                        "-fx-cursor: hand;");
                // contenu aligné à gauche.
                card.setAlignment(Pos.CENTER_LEFT);
                // la carte peut s’étendre pour remplir horizontalement.
                card.setMaxWidth(Double.MAX_VALUE);

                // Configuration du header
                header.setAlignment(Pos.CENTER_LEFT);

                titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: black;");
                // evite le debordement(text retoure a la ligne)
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(250);

                statusLabel.setStyle(
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");

                // Description
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
                descLabel.setMaxWidth(350);
                descLabel.setMaxHeight(40);

                // Configuration de la ligne d'info
                infoRow.setAlignment(Pos.CENTER_LEFT);

                dureeLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-size: 11px; -fx-font-weight: bold;");
                diffLabel.setStyle("-fx-text-fill: #9B59B6; -fx-font-size: 11px; -fx-font-weight: bold;");
                typeLabel.setStyle("-fx-text-fill: #E67E22; -fx-font-size: 11px; -fx-font-weight: bold;");

                // Configuration de la ligne du bas
                bottomRow.setAlignment(Pos.CENTER_LEFT);
                bottomRow.setSpacing(15);

                // Configuration des boutons
                detailsBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                detailsBtn.setTooltip(new Tooltip("Voir les détails du challenge"));

                coachesBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                coachesBtn.setTooltip(new Tooltip("Gérer les coaches"));

                rewardsBtn.setStyle("-fx-background-color: linear-gradient(to right, #9B59B6, #8E44AD); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 5 10; " +
                        "-fx-font-size: 11px; -fx-cursor: hand;");
                rewardsBtn.setTooltip(new Tooltip("Gérer les récompenses"));

                // ✅ AJOUT UNIQUE des boutons à actionBox
                actionBox.getChildren().addAll(detailsBtn, coachesBtn, rewardsBtn);
                actionBox.setAlignment(Pos.CENTER_RIGHT);

                // Labels pour coaches et récompenses
                coachesLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;");
                rewardsLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");

                // Configuration de la colonne de droite
                rightColumn.setAlignment(Pos.CENTER_RIGHT);
                rightColumn.setMinWidth(100);

                // Assemblage des composants
                VBox leftContent = new VBox(10);
                leftContent.getChildren().addAll(header, descLabel, infoRow, bottomRow);

                HBox mainContent = new HBox(15);
                mainContent.setAlignment(Pos.CENTER_LEFT);
                mainContent.getChildren().addAll(leftContent, rightColumn);
                // Elle retourne la liste des nœuds enfants de ce conteneur
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
                    Challenge challenge = getItem();
                    if (challenge != null) {
                        challengeListView.getSelectionModel().select(challenge);
                        loadChallengeData(challenge);
                    }
                });
            }

            // updateItem est appelée chaque fois qu’une cellule doit afficher un nouvel
            // élément ou se vider.

            @Override
            protected void updateItem(Challenge challenge, boolean empty) {
                // on appelle la méthode de la classe parente pour préparer la cellule.
                super.updateItem(challenge, empty);

                if (empty || challenge == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Mise à jour des données
                    titleLabel.setText("🏆 " + challenge.getTitre());

                    // Statut avec badge coloré
                    if (challenge.isActif()) {
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

                    // Description tronquée si trop longue
                    String desc = challenge.getDescription();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 97) + "...";
                    }
                    descLabel.setText(desc);

                    // Informations principales
                    dureeLabel.setText("⏱️ " + challenge.getDureeJours() + " jours");
                    diffLabel.setText("🎯 " + challenge.getNiveauDifficulte());
                    typeLabel.setText("📂 " + challenge.getTypeChallenge());

                    // Mise à jour de la ligne d'info
                    infoRow.getChildren().clear();
                    infoRow.getChildren().addAll(dureeLabel, diffLabel, typeLabel);

                    // Mise à jour du header
                    header.getChildren().clear();
                    // separateur entre titre et status(dans header)
                    Region spacer = new Region();
                    // le spacer prend tout l’espace disponible entre les éléments.
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    header.getChildren().addAll(titleLabel, spacer, statusLabel);
                    // =>Résultat : dans la ligne du header, le titre reste à gauche, le statut à
                    // droite, même si la largeur de la carte change.

                    // Coaches associés
                    List<CoachMotivation> coaches = challengeCoachCrud
                            .getCoachesForChallenge(challenge.getIdChallenge());
                    if (coaches.isEmpty()) {
                        coachesLabel.setText("👤 Aucun coach");
                        coachesLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                        coachesLabel.setOnMouseClicked(null);
                    } else {

                        // construire dynamiquement le texte du label.
                        StringBuilder coachesText = new StringBuilder("👤 ");
                        for (int i = 0; i < Math.min(coaches.size(), 2); i++) {
                            coachesText.append(coaches.get(i).getNomCoach());
                            if (i < Math.min(coaches.size(), 2) - 1) {
                                coachesText.append(", ");
                            }
                        }

                        if (coaches.size() > 2) {
                            coachesText.append(" +").append(coaches.size() - 2);
                        }

                        coachesLabel.setText(coachesText.toString());
                        coachesLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;");
                        // cursor cliquable
                        coachesLabel.setCursor(Cursor.HAND);

                        coachesLabel.setOnMouseClicked(e -> {
                            // empêche l’événement de remonter à la cellule entière
                            e.consume();
                            // ouvre un popup avec tous les coaches associés au challenge.
                            showCoachesPopup(challenge, coaches);
                        });
                    }

                    // Récompenses associées

                    /*
                     * challengeRecompenseCrud.getRecompensesByChallenge(...) → méthode qui
                     * interroge la base de données
                     * pour récupérer toutes les récompenses liées à ce challenge.
                     */
                    List<Recompense> recompenses = challengeRecompenseCrud
                            .getRecompensesByChallenge(challenge.getIdChallenge());
                    if (recompenses.isEmpty()) {
                        rewardsLabel.setText("🎁 Aucune récompense");
                        rewardsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                        rewardsLabel.setOnMouseClicked(null);
                    } else {
                        StringBuilder rewardsText = new StringBuilder("🎁 ");
                        for (int i = 0; i < Math.min(recompenses.size(), 2); i++) {
                            rewardsText.append(recompenses.get(i).getTitre());
                            if (i < Math.min(recompenses.size(), 2) - 1) {
                                rewardsText.append(", ");
                            }
                        }

                        if (recompenses.size() > 2) {
                            rewardsText.append(" +").append(recompenses.size() - 2);
                        }

                        rewardsLabel.setText(rewardsText.toString());
                        rewardsLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px; -fx-font-weight: bold;");
                        rewardsLabel.setCursor(Cursor.HAND);

                        rewardsLabel.setOnMouseClicked(e -> {
                            e.consume();
                            showRecompensesPopup(challenge, recompenses);
                        });
                    }

                    // Mise à jour de la ligne du bas
                    bottomRow.getChildren().clear();
                    bottomRow.getChildren().addAll(coachesLabel, rewardsLabel);

                    // Configuration de la colonne de droite (statistiques)
                    rightColumn.getChildren().clear();

                    VBox statsBox = new VBox(5);
                    statsBox.setAlignment(Pos.CENTER_RIGHT);

                    Label idLabel = new Label("#" + challenge.getIdChallenge());
                    idLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

                    // Badge pour le nombre de coaches
                    HBox coachBadge = new HBox(5);
                    coachBadge.setAlignment(Pos.CENTER);
                    Label coachIcon = new Label("👤");
                    Label coachCount = new Label(String.valueOf(coaches.size()));
                    coachCount.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: black;");
                    coachBadge.setStyle(
                            "-fx-background-color: rgba(52,152,219,0.2); -fx-background-radius: 10; -fx-padding: 3 8;");
                    coachBadge.getChildren().addAll(coachIcon, coachCount);

                    // Badge pour le nombre de récompenses
                    HBox rewardBadge = new HBox(5);
                    rewardBadge.setAlignment(Pos.CENTER);
                    Label rewardIcon = new Label("🎁");
                    Label rewardCount = new Label(String.valueOf(recompenses.size()));
                    rewardCount.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: black;");
                    rewardBadge.setStyle(
                            "-fx-background-color: rgba(155,89,182,0.2); -fx-background-radius: 10; -fx-padding: 3 8;");
                    rewardBadge.getChildren().addAll(rewardIcon, rewardCount);

                    // La VBox statsBox contient maintenant :
                    //
                    // ID du challenge (#12)
                    //
                    // Badge coaches 👤 + nombre
                    //
                    // Badge récompenses 🎁 + nombre
                    statsBox.getChildren().addAll(idLabel, coachBadge, rewardBadge);
                    rightColumn.getChildren().add(statsBox);

                    // Mise à jour des actions des boutons
                    // action declenché lors d un clic
                    detailsBtn.setOnAction(e -> showChallengeDetail(challenge));
                    coachesBtn.setOnAction(e -> openCoachManager(challenge));
                    rewardsBtn.setOnAction(e -> openRecompenseManager(challenge));

                    // card est un VBox ou HBox qui contient toute ta structure :
                    //
                    // header
                    //
                    // infoRow
                    //
                    // bottomRow
                    //
                    // rightColumn
                    //
                    // boutons

                    setGraphic(card);
                }
            }
        });

        // Sélection d'un élément
        // newVal contient le challenge sélectionné
        // oldval:selection precedant
        // adlistner :s je clic fais quelque chose
        challengeListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        // load les donné de l element selectionné
                        loadChallengeData(newVal);
                    }
                });

        // Style de la ListView
        challengeListView.setStyle("-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-padding: 0;");
    }

    /* ================= FORM ================= */
    private void setupForm() {
        difficulteField.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        typeField.getItems().addAll("Programmation", "Design", "Marketing", "Business", "Personnel");

        difficulteField.setValue("Moyen");
        typeField.setValue("Programmation");
    }

    /* ================= BUTTONS ================= */
    private void setupButtons() {
        addBtn.setOnAction(e -> addChallenge());
        updateBtn.setOnAction(e -> updateChallenge());
        deleteBtn.setOnAction(e -> deleteChallenge());
        clearBtn.setOnAction(e -> clearForm());
        searchBtn.setOnAction(e -> searchChallenges());
        coachBtn.setOnAction(e -> goToCoaches());
        recBtn.setOnAction(e -> goToRecompenses());
        exportBtn.setOnAction(e -> exportToPDF());

        dashboardBtn.setOnAction(e -> goToDashboard());
        // getSelectionModel:’est l’objet qui gère :Quel élément est sélectionné
        // selectedItemProperty():L’élément actuellement sélectionné dans la ListView.
        // addListener(...):Quand la sélection change → on exécute la lambda.

        challengeListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null)
                        loadChallengeData(newVal);
                });
    }

    // ==================== CONTRÔLE DE SAISIE ====================

    private void setupInputValidation() {
        // Validation pour le titre
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (Character.isDigit(newValue.charAt(0))) {
                    showFieldError(titleField, "Le titre ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(titleField, "Le titre ne doit pas être composé uniquement de chiffres");
                    return;
                }
                if (!newValue.matches(".*[a-zA-Z].*")) {
                    showFieldError(titleField, "Le titre doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 100) {
                    showFieldError(titleField, "Le titre ne doit pas dépasser 100 caractères");
                    return;
                }
                clearFieldError(titleField);
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
                if (!newValue.matches("(?s).*[\\p{L}\\p{M}].*")) {
                    showFieldError(descField, "La description doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 500) {
                    showFieldError(descField, "La description ne doit pas dépasser 500 caractères");
                    return;
                }
                clearFieldError(descField);
            }
        });

        // Validation pour la durée
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    showFieldError(dureeField, "La durée doit contenir uniquement des chiffres");
                    return;
                }
                try {
                    int value = Integer.parseInt(newValue);
                    if (value < 1 || value > 365) {
                        showFieldError(dureeField, "La durée doit être entre 1 et 365 jours");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showFieldError(dureeField, "Valeur invalide");
                    return;
                }
                clearFieldError(dureeField);
            }
        });

        // Validation pour les ComboBox
        difficulteField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                showFieldError(difficulteField, "La difficulté est obligatoire");
            } else {
                clearFieldError(difficulteField);
            }
        });

        typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                showFieldError(typeField, "Le type est obligatoire");
            } else {
                clearFieldError(typeField);
            }
        });
    }

    private void showFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        // "Est-ce que le champ est un champ texte ?"
        if (field instanceof TextInputControl) {
            // On transforme le Control en TextInputControl
            ((TextInputControl) field).setTooltip(new Tooltip(message));
        } else if (field instanceof ComboBox) {
            field.setTooltip(new Tooltip(message));
        }
    }

    // Supprimer l’affichage d’erreur d’un champ.
    private void clearFieldError(Control field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du titre
        String titre = titleField.getText().trim();
        if (titre.isEmpty()) {
            showFieldError(titleField, "Le titre est obligatoire");
            showAlert("Validation", "Le titre est obligatoire", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (Character.isDigit(titre.charAt(0))) {
            showFieldError(titleField, "Le titre ne doit pas commencer par un chiffre");
            showAlert("Validation", "Le titre ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (titre.matches("\\d+")) {
            showFieldError(titleField, "Le titre ne doit pas être composé uniquement de chiffres");
            showAlert("Validation", "Le titre ne doit pas être composé uniquement de chiffres",
                    Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (!titre.matches(".*[a-zA-Z].*")) {
            showFieldError(titleField, "Le titre doit contenir au moins une lettre");
            showAlert("Validation", "Le titre doit contenir au moins une lettre", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else if (titre.length() > 100) {
            showFieldError(titleField, "Le titre ne doit pas dépasser 100 caractères");
            showAlert("Validation", "Le titre ne doit pas dépasser 100 caractères", Alert.AlertType.WARNING);
            titleField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(titleField);
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
                showAlert("Validation", "La description ne doit pas être composée uniquement de chiffres",
                        Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (!description.matches(".*[a-zA-Z].*")) {
            showFieldError(descField, "La description doit contenir au moins une lettre");
            if (isValid) {
                showAlert("Validation", "La description doit contenir au moins une lettre", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else if (description.length() > 500) {
            showFieldError(descField, "La description ne doit pas dépasser 500 caractères");
            if (isValid) {
                showAlert("Validation", "La description ne doit pas dépasser 500 caractères", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(descField);
        }

        // Validation de la durée
        String dureeText = dureeField.getText().trim();
        if (dureeText.isEmpty()) {
            showFieldError(dureeField, "La durée est obligatoire");
            if (isValid) {
                showAlert("Validation", "La durée est obligatoire", Alert.AlertType.WARNING);
                dureeField.requestFocus();
            }
            isValid = false;
        } else if (!dureeText.matches("\\d+")) {
            showFieldError(dureeField, "La durée doit contenir uniquement des chiffres");
            if (isValid) {
                showAlert("Validation", "La durée doit contenir uniquement des chiffres", Alert.AlertType.WARNING);
                dureeField.requestFocus();
            }
            isValid = false;
        } else {
            try {
                int duree = Integer.parseInt(dureeText);
                if (duree < 1 || duree > 365) {
                    showFieldError(dureeField, "La durée doit être entre 1 et 365 jours");
                    if (isValid) {
                        showAlert("Validation", "La durée doit être entre 1 et 365 jours", Alert.AlertType.WARNING);
                        dureeField.requestFocus();
                    }
                    isValid = false;
                } else {
                    clearFieldError(dureeField);
                }
            } catch (NumberFormatException e) {
                showFieldError(dureeField, "Valeur invalide pour la durée");
                if (isValid) {
                    showAlert("Validation", "Valeur invalide pour la durée", Alert.AlertType.WARNING);
                    dureeField.requestFocus();
                }
                isValid = false;
            }
        }

        // Validation des ComboBox
        if (difficulteField.getValue() == null || difficulteField.getValue().isEmpty()) {
            showFieldError(difficulteField, "La difficulté est obligatoire");
            showAlert("Validation", "La difficulté est obligatoire", Alert.AlertType.WARNING);
            if (isValid)
                difficulteField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(difficulteField);
        }

        if (typeField.getValue() == null || typeField.getValue().isEmpty()) {
            showFieldError(typeField, "Le type est obligatoire");
            showAlert("Validation", "Le type est obligatoire", Alert.AlertType.WARNING);
            if (isValid)
                typeField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(typeField);
        }

        return isValid;
    }

    /* ================= DATA ================= */
    private void loadData() {
        try {
            // Mettre à jour la liste principale (challengeList est un membre de la classe)
            challengeList.setAll(challengeCrud.readAll());
            System.out.println("Chargé " + challengeList.size() + " challenges");

            // Calculer les statistiques après le chargement
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchChallenges() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        List<Challenge> filtered = challengeCrud.readAll().stream()
                .filter(c -> c.getTitre().toLowerCase().contains(keyword) ||
                        c.getDescription().toLowerCase().contains(keyword) ||
                        c.getTypeChallenge().toLowerCase().contains(keyword) ||
                        c.getNiveauDifficulte().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        // Pour la recherche, on peut temporairement changer la source du ListView
        // Mais attention : challengeList reste l'état complet pour les stats?
        // Habituellement, les stats sur le tableau de bord reflètent la base complète,
        // ou la vue filtrée? Dans Gestion, c'est souvent la base.
        challengeListView.setItems(FXCollections.observableArrayList(filtered));
        // On ne met pas à jour les stats ici si on veut qu'elles gardent le total
        // global
    }

    /* ================= CRUD ================= */
    private void addChallenge() {
        if (!validateForm())
            return;

        try {
            Challenge challenge = new Challenge(
                    titleField.getText().trim(),
                    descField.getText().trim(),
                    Integer.parseInt(dureeField.getText().trim()),
                    difficulteField.getValue(),
                    typeField.getValue());

            challenge.setActif(actifField.isSelected());
            challengeCrud.create(challenge);

            showAlert("Succès", "Challenge ajouté avec succès", Alert.AlertType.INFORMATION);
            clearForm();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateChallenge() {
        Challenge selected = challengeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez un challenge", Alert.AlertType.WARNING);
            return;
        }

        if (!validateForm())
            return;

        try {
            selected.setTitre(titleField.getText().trim());
            selected.setDescription(descField.getText().trim());
            selected.setDureeJours(Integer.parseInt(dureeField.getText().trim()));
            selected.setNiveauDifficulte(difficulteField.getValue());
            selected.setTypeChallenge(typeField.getValue());
            selected.setActif(actifField.isSelected());

            challengeCrud.update(selected);
            challengeListView.refresh();
            updateStatistics();

            showAlert("Succès", "Challenge modifié", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteChallenge() {
        Challenge selected = challengeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez un challenge à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le challenge");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitre() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    challengeCrud.delete_reel(selected.getIdChallenge());
                    showAlert("Succès", "Challenge supprimé!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    /* ================= GESTION DES RÉCOMPENSES ================= */
    private void openRecompenseManager(Challenge challenge) {
        try {
            Stage stage = new Stage();
            stage.setTitle("🎁 Gérer les récompenses - " + challenge.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(20);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // En-tête
            Label titleLabel = new Label("🎁 Gérer les récompenses pour : " + challenge.getTitre());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            // Liste des récompenses actuelles
            ListView<Recompense> currentList = new ListView<>();
            ObservableList<Recompense> currentRecompenses = FXCollections.observableArrayList(
                    challengeRecompenseCrud.getRecompensesByChallenge(challenge.getIdChallenge()));
            currentList.setItems(currentRecompenses);
            currentList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Recompense r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r == null) {
                        setText(null);
                    } else {
                        setText("🏆 " + r.getTitre() + " (" + r.getTypeRecompense() + ")");
                    }
                }
            });

            VBox currentBox = new VBox(10);
            currentBox.getChildren().addAll(
                    new Label("✅ Récompenses assignées (" + currentRecompenses.size() + ") :"),
                    currentList);

            // Bouton pour ajouter une nouvelle récompense
            Button addNewButton = new Button("➕ Ajouter une récompense");
            addNewButton.setStyle(
                    "-fx-background-color: #2ECC71; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10 20;");
            addNewButton.setOnAction(e -> {
                openRecompenseSelector(challenge, currentRecompenses);
            });

            // Boutons de contrôle
            HBox buttonBox = new HBox(15);
            Button saveButton = new Button("💾 Enregistrer");
            saveButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
            saveButton.setOnAction(e -> {
                showAlert("Succès", "Modifications enregistrées", Alert.AlertType.INFORMATION);
                stage.close();
                challengeListView.refresh();
            });

            Button cancelButton = new Button("❌ Annuler");
            cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white;");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(saveButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(
                    titleLabel,
                    currentBox,
                    addNewButton,
                    buttonBox);

            Scene scene = new Scene(root, 500, 400);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le gestionnaire", Alert.AlertType.ERROR);
        }
    }

    private void openRecompenseSelector(Challenge challenge, ObservableList<Recompense> currentList) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Sélectionner des récompenses");
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));

            Label titleLabel = new Label("📦 Sélectionnez les récompenses à ajouter :");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Récupérer toutes les récompenses disponibles
            RecompenseCrud recompenseCrud = new RecompenseCrud();
            List<Recompense> allRecompenses = recompenseCrud.readAll();

            // Filtrer celles déjà assignées
            List<Recompense> availableRecompenses = allRecompenses.stream()
                    .filter(r -> currentList.stream()
                            .noneMatch(cr -> cr.getIdRecompense() == r.getIdRecompense()))
                    .collect(Collectors.toList());

            ListView<Recompense> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(availableRecompenses));
            listView.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Recompense r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r == null) {
                        setText(null);
                    } else {
                        setText("🎯 " + r.getTitre() + " - " + r.getTypeRecompense());
                    }
                }
            });

            // Boutons
            HBox buttonBox = new HBox(10);
            Button addButton = new Button("➕ Ajouter sélection");
            addButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
            addButton.setOnAction(e -> {
                Recompense selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    challengeRecompenseCrud.associateRecompense(
                            challenge.getIdChallenge(),
                            selected.getIdRecompense());
                    currentList.add(selected);
                    stage.close();
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(addButton, cancelButton);

            root.getChildren().addAll(titleLabel, listView, buttonBox);

            Scene scene = new Scene(root, 400, 300);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= UTIL ================= */
    private void loadChallengeData(Challenge c) {
        titleField.setText(c.getTitre());
        descField.setText(c.getDescription());
        dureeField.setText(String.valueOf(c.getDureeJours()));
        difficulteField.setValue(c.getNiveauDifficulte());
        typeField.setValue(c.getTypeChallenge());
        actifField.setSelected(c.isActif());

        // Effacer les erreurs de validation lors du chargement
        clearFieldError(titleField);
        clearFieldError(descField);
        clearFieldError(dureeField);
        clearFieldError(difficulteField);
        clearFieldError(typeField);
    }

    private void clearForm() {
        titleField.clear();
        descField.clear();
        dureeField.clear();
        difficulteField.setValue("Moyen");
        typeField.setValue("Programmation");
        actifField.setSelected(false);
        challengeListView.getSelectionModel().clearSelection();

        // Effacer toutes les erreurs de validation
        clearFieldError(titleField);
        clearFieldError(descField);
        clearFieldError(dureeField);
        clearFieldError(difficulteField);
        clearFieldError(typeField);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /* ================= NAVIGATION VERS DASHBOARD ================= */
    private void goToDashboard() {
        try {
            // Charger le fichier FXML du dashboard
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/motivation/dashboard_statistiques.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();

            // Créer la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("📊 Dashboard Statistiques - Challenge Manager Pro");
            stage.centerOnScreen();

            // Animation de transition (optionnelle)
            stage.setOpacity(0);
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), stage.getScene().getRoot());
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
            stage.setOpacity(1);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void goToCoaches() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/motivation/coach.fxml"));
            Stage stage = (Stage) coachBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToRecompenses() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/motivation/RecompenseView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) recBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("🏆 Gestion des Récompenses");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des récompenses", Alert.AlertType.ERROR);
        }
    }

    /* ================= EXPORT PDF ================= */
    /* ================= EXPORT SIMPLE (SANS PDFBOX) ================= */
    /* ================= EXPORT PDF PROFESSIONNEL ================= */
    /* ================= EXPORT PDF PROFESSIONNEL ================= */
    private void exportToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les challenges en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

            fileChooser.setInitialFileName("challenges_" +
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

            // Pages suivantes: Détails des challenges
            createChallengeDetailsPages(document);

            // Sauvegarder le document
            document.save(file);
            document.close();

            showAlert("Succès",
                    "✅ PDF généré avec succès!\n\n" +
                            "📄 Fichier: " + file.getName() + "\n" +
                            "📁 Chemin: " + file.getAbsolutePath() + "\n" +
                            "📊 Nombre de challenges: " + challengeList.size(),
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createCoverPage(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Fond coloré en haut
            contentStream.setNonStrokingColor(new Color(41, 128, 185)); // Bleu professionnel
            contentStream.addRect(0, pageHeight - 200, pageWidth, 200);
            contentStream.fill();

            // Titre principal - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
            contentStream.newLineAtOffset(50, pageHeight - 100);
            contentStream.showText("RAPPORT DES CHALLENGES");
            contentStream.endText();

            // Sous-titre - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 16);
            contentStream.newLineAtOffset(50, pageHeight - 140);
            contentStream.showText("Analyse complete et statistiques");
            contentStream.endText();

            // Date de génération - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(50, pageHeight - 250);
            contentStream.showText("Date de generation: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Statistiques rapides (cartes colorées)
            float cardY = pageHeight - 500;
            float cardHeight = 100;
            float cardWidth = 150;
            float spacing = 20;

            long actifs = challengeList.stream().filter(Challenge::isActif).count();
            long inactifs = challengeList.size() - actifs;

            // Carte 1: Total
            drawStatCard(contentStream, 50, cardY, cardWidth, cardHeight,
                    new Color(52, 152, 219), "TOTAL",
                    String.valueOf(challengeList.size()), "Challenges");

            // Carte 2: Actifs
            drawStatCard(contentStream, 50 + cardWidth + spacing, cardY, cardWidth, cardHeight,
                    new Color(46, 204, 113), "ACTIFS",
                    String.valueOf(actifs), "Challenges");

            // Carte 3: Inactifs
            drawStatCard(contentStream, 50 + 2 * (cardWidth + spacing), cardY, cardWidth, cardHeight,
                    new Color(231, 76, 60), "INACTIFS",
                    String.valueOf(inactifs), "Challenges");

            // Pied de page avec ligne décorative
            contentStream.setStrokingColor(new Color(41, 128, 185));
            contentStream.setLineWidth(3);
            contentStream.moveTo(50, 100);
            contentStream.lineTo(pageWidth - 50, 100);
            contentStream.stroke();

            // Texte du pied de page - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
            contentStream.newLineAtOffset(50, 70);
            contentStream.showText("Genere automatiquement par le systeme de gestion des challenges");
            contentStream.endText();

            // image logo

            String imageLeftPath = getClass().getResource("/org/example/image/logo_equipe-removebg-preview (1).png")
                    .toURI().getPath();
            String imageRightPath = getClass().getResource("/org/example/image/Logo_ESPRIT_-_Tunisie.png").toURI()
                    .getPath();

            PDImageXObject imageLeft = PDImageXObject.createFromFile(imageLeftPath, document);
            PDImageXObject imageRight = PDImageXObject.createFromFile(imageRightPath, document);

            // Dimensions et marges
            float imageWidth = 50;
            float imageHeight = 50;
            float margin = 50;

            // Récupération de la largeur de la page

            // Dessiner l'image gauche (en bas à gauche)
            contentStream.drawImage(imageLeft, margin, 20, imageWidth, imageHeight);

            // Dessiner l'image droite (en bas à droite)
            contentStream.drawImage(imageRight, pageWidth - margin - imageWidth, 40, imageWidth, imageHeight);
        }
    }

    private void drawStatCard(PDPageContentStream contentStream, float x, float y,
            float width, float height, Color color,
            String label, String value, String unit) throws Exception {
        // Fond de la carte
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();

        // Label - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(x + 10, y + height - 25);
        contentStream.showText(label);
        contentStream.endText();

        // Valeur (grand) - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 32);
        contentStream.newLineAtOffset(x + 10, y + height - 65);
        contentStream.showText(value);
        contentStream.endText();

        // Unité - CORRIGÉ
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

            // En-tête de la page
            contentStream.setNonStrokingColor(new Color(41, 128, 185));
            contentStream.addRect(0, pageHeight - 80, pageWidth, 80);
            contentStream.fill();

            // CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.newLineAtOffset(50, pageHeight - 50);
            contentStream.showText("STATISTIQUES DETAILLEES");
            contentStream.endText();

            float currentY = pageHeight - 120;

            // Section: Répartition par difficulté
            currentY = drawSectionTitle(contentStream, currentY, "Repartition par difficulte");
            currentY -= 20;

            Map<String, Long> difficultyStats = challengeList.stream()
                    .collect(Collectors.groupingBy(Challenge::getNiveauDifficulte, Collectors.counting()));

            for (Map.Entry<String, Long> entry : difficultyStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, entry.getKey(), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Répartition par type
            currentY = drawSectionTitle(contentStream, currentY, "Repartition par type");
            currentY -= 20;

            Map<String, Long> typeStats = challengeList.stream()
                    .collect(Collectors.groupingBy(Challenge::getTypeChallenge, Collectors.counting()));

            for (Map.Entry<String, Long> entry : typeStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, entry.getKey(), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Durée moyenne
            currentY = drawSectionTitle(contentStream, currentY, "Analyse de duree");
            currentY -= 20;

            double avgDuration = challengeList.stream()
                    .mapToInt(Challenge::getDureeJours)
                    .average()
                    .orElse(0);

            int minDuration = challengeList.stream()
                    .mapToInt(Challenge::getDureeJours)
                    .min()
                    .orElse(0);

            int maxDuration = challengeList.stream()
                    .mapToInt(Challenge::getDureeJours)
                    .max()
                    .orElse(0);

            currentY = drawStatLine(contentStream, currentY, "Duree moyenne",
                    String.format("%.1f jours", avgDuration));
            currentY = drawStatLine(contentStream, currentY, "Duree minimale",
                    minDuration + " jours");
            currentY = drawStatLine(contentStream, currentY, "Duree maximale",
                    maxDuration + " jours");

            // Numéro de page - CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(pageWidth - 80, 30);
            contentStream.showText("Page 2");
            contentStream.endText();
        }
    }

    private float drawSectionTitle(PDPageContentStream contentStream, float y, String title) throws Exception {
        // CORRIGÉ
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(title);
        contentStream.endText();

        // Ligne sous le titre
        contentStream.setStrokingColor(new Color(41, 128, 185));
        contentStream.setLineWidth(2);
        contentStream.moveTo(50, y - 5);
        contentStream.lineTo(300, y - 5);
        contentStream.stroke();

        return y - 10;
    }

    private float drawStatLine(PDPageContentStream contentStream, float y, String label, String value)
            throws Exception {
        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.newLineAtOffset(70, y);
        contentStream.showText(label);
        contentStream.endText();

        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(41, 128, 185));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(300, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - 25;
    }

    private void createChallengeDetailsPages(PDDocument document) throws Exception {
        int pageNumber = 3;
        int challengesPerPage = 3;

        for (int i = 0; i < challengeList.size(); i += challengesPerPage) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // En-tête de la page
                contentStream.setNonStrokingColor(new Color(41, 128, 185));
                contentStream.addRect(0, pageHeight - 60, pageWidth, 60);
                contentStream.fill();

                // CORRIGÉ
                contentStream.beginText();
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.newLineAtOffset(50, pageHeight - 40);
                contentStream.showText("DETAIL DES CHALLENGES");
                contentStream.endText();

                float currentY = pageHeight - 100;

                // Afficher jusqu'à 3 challenges par page
                for (int j = i; j < Math.min(i + challengesPerPage, challengeList.size()); j++) {
                    Challenge challenge = challengeList.get(j);
                    currentY = drawChallengeCard(contentStream, challenge, currentY, pageWidth);
                    currentY -= 30; // Espacement entre les cartes
                }

                // Numéro de page - CORRIGÉ
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

    private float drawChallengeCard(PDPageContentStream contentStream, Challenge challenge,
            float y, float pageWidth) throws Exception {
        float cardHeight = 180;
        float margin = 50;
        float cardWidth = pageWidth - 2 * margin;

        // Fond de la carte avec couleur selon le statut
        Color cardColor = challenge.isActif() ? new Color(236, 240, 241) : new Color(250, 235, 235);

        contentStream.setNonStrokingColor(cardColor);
        contentStream.addRect(margin, y - cardHeight, cardWidth, cardHeight);
        contentStream.fill();

        // Bordure colorée à gauche
        Color borderColor = challenge.isActif() ? new Color(46, 204, 113) : new Color(231, 76, 60);

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(margin, y - cardHeight, 10, cardHeight);
        contentStream.fill();

        float textX = margin + 20;
        float textY = y - 25;

        // Titre du challenge (en gras) - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(44, 62, 80));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        contentStream.newLineAtOffset(textX, textY);
        String titre = challenge.getTitre();
        if (titre.length() > 60) {
            titre = titre.substring(0, 57) + "...";
        }
        contentStream.showText(titre);
        contentStream.endText();

        textY -= 25;

        // Description - CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(textX, textY);
        String desc = challenge.getDescription();
        if (desc.length() > 80) {
            desc = desc.substring(0, 77) + "...";
        }
        contentStream.showText(desc);
        contentStream.endText();

        textY -= 30;

        // Informations en colonnes - CORRIGÉ
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

        // Colonne 1
        drawInfoItem(contentStream, textX, textY, "Duree:",
                challenge.getDureeJours() + " jours");
        drawInfoItem(contentStream, textX, textY - 20, "Difficulte:",
                challenge.getNiveauDifficulte());

        // Colonne 2
        drawInfoItem(contentStream, textX + 200, textY, "Type:",
                challenge.getTypeChallenge());
        drawInfoItem(contentStream, textX + 200, textY - 20, "Statut:",
                challenge.isActif() ? "ACTIF" : "INACTIF");

        // Badge de statut - CORRIGÉ
        float badgeX = margin + cardWidth - 100;
        float badgeY = y - 30;

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(badgeX, badgeY, 80, 25);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(badgeX + 10, badgeY + 8);
        contentStream.showText(challenge.isActif() ? "ACTIF" : "INACTIF");
        contentStream.endText();

        // Récompenses (si disponibles)
        List<Recompense> recompenses = challengeRecompenseCrud
                .getRecompensesByChallenge(challenge.getIdChallenge());

        if (!recompenses.isEmpty()) {
            textY -= 50;
            // CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(230, 126, 34));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText("Recompenses: ");
            contentStream.endText();

            String recompensesText = recompenses.stream()
                    .map(Recompense::getTitre)
                    .limit(3)
                    .collect(Collectors.joining(", "));

            if (recompenses.size() > 3) {
                recompensesText += " +" + (recompenses.size() - 3);
            }

            // CORRIGÉ
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            contentStream.newLineAtOffset(textX + 75, textY);
            contentStream.showText(recompensesText);
            contentStream.endText();
        }

        return y - cardHeight;
    }

    private void drawInfoItem(PDPageContentStream contentStream, float x, float y,
            String label, String value) throws Exception {
        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(127, 140, 141));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();

        // CORRIGÉ
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        contentStream.newLineAtOffset(x + 60, y);
        contentStream.showText(value);
        contentStream.endText();
    }

    /* ================= MISE À JOUR DES STATISTIQUES ================= */
    /* ================= MISE À JOUR DES STATISTIQUES ================= */
    private void updateStatistics() {
        if (challengeList.isEmpty()) {
            if (totalLabel != null) {
                totalLabel.setText("0");
                totalLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF;"); // Blanc pur
            }
            if (actifsLabel != null) {
                actifsLabel.setText("0");
                actifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #00FF88;"); // Vert vif
                                                                                                            // néon
            }
            if (inactifsLabel != null) {
                inactifsLabel.setText("0");
                inactifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FF6B6B;"); // Rouge
                                                                                                              // vif
            }
            return;
        }

        // Calcul des statistiques
        int total = challengeList.size();
        long actifs = challengeList.stream().filter(Challenge::isActif).count();
        long inactifs = total - actifs;

        // Mise à jour des labels avec vérification null et couleurs améliorées
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(total));
            totalLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF;"); // Blanc pur
        }
        if (actifsLabel != null) {
            actifsLabel.setText(String.valueOf(actifs));
            actifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #00FF88;"); // Vert vif néon
        }
        if (inactifsLabel != null) {
            inactifsLabel.setText(String.valueOf(inactifs));
            inactifsLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #FF6B6B;"); // Rouge vif
        }

        // Optionnel: animation
        if (totalLabel != null)
            animateLabel(totalLabel);
        if (actifsLabel != null)
            animateLabel(actifsLabel);
        if (inactifsLabel != null)
            animateLabel(inactifsLabel);
    }

    /* ================= ANIMATION DES LABELS ================= */
    private void animateLabel(Label label) {
        label.setScaleX(1.1);
        label.setScaleY(1.1);

        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), label);
        scaleTransition.setFromX(1.1);
        scaleTransition.setFromY(1.1);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();
    }

    /* ================= GESTION DES COACHES ================= */
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
            currentList.setCellFactory(list -> new ListCell<CoachMotivation>() {
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
                                // Rafraîchir la table
                                challengeListView.refresh();
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
                challengeListView.refresh();
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
            CoachMotivationCrud coachCrud = new CoachMotivationCrud(); // Assure-toi d'avoir cette instance
            List<CoachMotivation> allCoaches = coachCrud.readAll();

            // Filtrer ceux déjà assignés
            List<CoachMotivation> availableCoaches = allCoaches.stream()
                    .filter(c -> currentList.stream()
                            .noneMatch(cc -> cc.getIdCoach() == c.getIdCoach()))
                    .collect(Collectors.toList());

            ListView<CoachMotivation> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(availableCoaches));
            listView.setCellFactory(list -> new ListCell<CoachMotivation>() {
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

    /* ================= POPUP COACHES ================= */
    private void showCoachesPopup(Challenge challenge, List<CoachMotivation> coaches) {
        try {
            Stage popupStage = new Stage();
            popupStage.setTitle("👤 Coaches - " + challenge.getTitre());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(challengeListView.getScene().getWindow());

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

            Label titleLabel = new Label("👤 COACHES ASSOCIÉS");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

            ListView<CoachMotivation> coachesList = new ListView<>();
            coachesList.setItems(FXCollections.observableArrayList(coaches));
            coachesList.setCellFactory(param -> new ListCell<CoachMotivation>() {
                @Override
                protected void updateItem(CoachMotivation coach, boolean empty) {
                    super.updateItem(coach, empty);
                    if (empty || coach == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.setStyle(
                                "-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;");

                        // Avatar/icône
                        Label icon = new Label("👤");
                        icon.setStyle("-fx-font-size: 18px;");

                        VBox infoBox = new VBox(5);
                        Label nameLabel = new Label(coach.getNomCoach());
                        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");

                        Label styleLabel = new Label("Style: " + coach.getStyle());
                        styleLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px;");

                        // Statut avec badge coloré
                        Label statusLabel = new Label(coach.isActif() ? "✅ ACTIF" : "❌ INACTIF");
                        statusLabel.setStyle(
                                coach.isActif() ? "-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;"
                                        : "-fx-text-fill: #E74C3C; -fx-font-size: 11px; -fx-font-weight: bold;");

                        infoBox.getChildren().addAll(nameLabel, styleLabel, statusLabel);

                        Button detailBtn = new Button("📋 Détails");
                        detailBtn.setStyle(
                                "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                        detailBtn.setOnAction(e -> showCoachDetail(coach));

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        hbox.getChildren().addAll(icon, infoBox, spacer, detailBtn);
                        setGraphic(hbox);
                        setCursor(Cursor.HAND);

                        // Effet au survol
                        setOnMouseEntered(e -> {
                            setStyle("-fx-background-color: rgba(52,152,219,0.2);");
                        });
                        setOnMouseExited(e -> {
                            setStyle("");
                        });
                    }
                }
            });

            Button closeButton = new Button("Fermer");
            closeButton.setStyle(
                    "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            closeButton.setOnAction(e -> popupStage.close());

            root.getChildren().addAll(titleLabel, coachesList, closeButton);

            Scene scene = new Scene(root, 500, 400); // Un peu plus large
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= POPUP RÉCOMPENSES ================= */
    private void showRecompensesPopup(Challenge challenge, List<Recompense> recompenses) {
        try {
            Stage popupStage = new Stage();
            popupStage.setTitle("🎁 Récompenses - " + challenge.getTitre());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(challengeListView.getScene().getWindow());

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10;");

            Label titleLabel = new Label("🎁 RÉCOMPENSES DISPONIBLES");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

            ListView<Recompense> recompensesList = new ListView<>();
            recompensesList.setItems(FXCollections.observableArrayList(recompenses));
            recompensesList.setCellFactory(param -> new ListCell<Recompense>() {
                @Override
                protected void updateItem(Recompense recompense, boolean empty) {
                    super.updateItem(recompense, empty);
                    if (empty || recompense == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(10);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        hbox.setStyle(
                                "-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;");

                        // Icône selon le type
                        Label icon = new Label(getIconForRecompenseType(recompense.getTypeRecompense()));
                        icon.setStyle("-fx-font-size: 18px;");

                        VBox infoBox = new VBox(5);
                        Label nameLabel = new Label(recompense.getTitre());
                        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");

                        Label typeLabel = new Label("Type: " + recompense.getTypeRecompense());
                        typeLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px;");

                        // Condition d'obtention
                        String condition = recompense.getConditionObtention();
                        if (condition != null && !condition.trim().isEmpty()) {
                            Label conditionLabel = new Label("Condition: " + condition);
                            conditionLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 11px;");
                            conditionLabel.setWrapText(true);
                            conditionLabel.setMaxWidth(350);
                            infoBox.getChildren().addAll(nameLabel, typeLabel, conditionLabel);
                        } else {
                            infoBox.getChildren().addAll(nameLabel, typeLabel);
                        }

                        // Statut
                        Label statusLabel = new Label(recompense.isActif() ? "✅ ACTIF" : "❌ INACTIF");
                        statusLabel.setStyle(recompense.isActif()
                                ? "-fx-text-fill: #2ECC71; -fx-font-size: 11px; -fx-font-weight: bold;"
                                : "-fx-text-fill: #E74C3C; -fx-font-size: 11px; -fx-font-weight: bold;");
                        infoBox.getChildren().add(statusLabel);

                        // Bouton pour voir les détails
                        Button detailBtn = new Button("👁️ Voir");
                        detailBtn.setStyle(
                                "-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-font-size: 11px;");
                        detailBtn.setOnAction(e -> showRecompenseDetail(recompense));

                        HBox bottomBox = new HBox(10);
                        bottomBox.setAlignment(Pos.CENTER_LEFT);
                        bottomBox.getChildren().addAll(statusLabel, detailBtn);

                        hbox.getChildren().addAll(icon, infoBox);
                        setGraphic(hbox);

                        // Rendre toute la cellule cliquable
                        setCursor(Cursor.HAND);
                        setOnMouseClicked(e -> {
                            if (e.getClickCount() == 1) {
                                showRecompenseDetail(recompense);
                            }
                        });

                        // Effet au survol
                        setOnMouseEntered(e -> {
                            setStyle("-fx-background-color: rgba(241,196,15,0.2);");
                        });
                        setOnMouseExited(e -> {
                            setStyle("");
                        });
                    }
                }
            });

            Button closeButton = new Button("Fermer");
            closeButton.setStyle(
                    "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            closeButton.setOnAction(e -> popupStage.close());

            root.getChildren().addAll(titleLabel, recompensesList, closeButton);

            Scene scene = new Scene(root, 550, 450);
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= MÉTHODE POUR ICÔNES DE RÉCOMPENSES ================= */
    private String getIconForRecompenseType(String type) {
        if (type == null)
            return "🎁";

        switch (type.toLowerCase()) {
            case "médaille":
            case "medaille":
                return "🥇";
            case "badge":
                return "🎖️";
            case "certificat":
                return "📜";
            case "points":
                return "⭐";
            case "réduction":
            case "reduction":
                return "💸";
            case "cadeau":
                return "🎁";
            case "accès premium":
            case "acces premium":
                return "👑";
            case "autre":
                return "🏆";
            default:
                return "🎁";
        }
    }

    /* ================= CARTE DÉTAIL COACH ================= */
    private void showCoachDetail(CoachMotivation coach) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("👤 Détails du Coach");
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(challengeListView.getScene().getWindow());

            // Création d'une carte similaire à une carte étudiant
            VBox card = new VBox(20);
            card.setPadding(new Insets(25));
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498DB, #2C3E50); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: rgba(255,255,255,0.2); " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 5);");

            // En-tête avec photo de profil (simulée)
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            Circle profileCircle = new Circle(40);
            profileCircle.setFill(javafx.scene.paint.Color.web("#1ABC9C")); // Utiliser javafx.scene.paint.Color
            profileCircle.setStroke(javafx.scene.paint.Color.WHITE); // Utiliser javafx.scene.paint.Color
            profileCircle.setStrokeWidth(3);

            Label profileIcon = new Label("👨‍🏫");
            profileIcon.setStyle("-fx-font-size: 32px;");
            StackPane profilePane = new StackPane(profileCircle, profileIcon);

            VBox headerInfo = new VBox(5);
            Label nameLabel = new Label(coach.getNomCoach());
            nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label roleLabel = new Label("Coach Motivation");
            roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-style: italic;");

            headerInfo.getChildren().addAll(nameLabel, roleLabel);
            header.getChildren().addAll(profilePane, headerInfo);

            // Section des informations
            VBox infoSection = new VBox(15);
            infoSection.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10; -fx-padding: 15;");

            // Style de coaching
            HBox styleBox = new HBox(10);
            styleBox.setAlignment(Pos.CENTER_LEFT);
            Label styleIcon = new Label("🎭");
            Label styleTitle = new Label("Style:");
            styleTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");
            Label styleValue = new Label(coach.getStyle());
            styleValue.setStyle("-fx-text-fill: #F1C40F; -fx-font-weight: bold;");
            styleBox.getChildren().addAll(styleIcon, styleTitle, styleValue);

            // Description
            HBox descBox = new HBox(10);
            descBox.setAlignment(Pos.CENTER_LEFT);
            Label descIcon = new Label("📝");
            Label descTitle = new Label("Description:");
            descTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");

            String description = coach.getDescription();
            Label descValue = new Label(
                    description != null && !description.isEmpty() ? description : "Aucune description disponible");
            descValue.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 12px;");
            descValue.setWrapText(true);
            descValue.setMaxWidth(350);

            descBox.getChildren().addAll(descIcon, descTitle, descValue);

            // Statut
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            Label statusIcon = new Label("📈");
            Label statusTitle = new Label("Statut:");
            statusTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");
            Label statusValue = new Label(coach.isActif() ? "ACTIF" : "INACTIF");
            statusValue.setStyle(coach.isActif() ? "-fx-text-fill: #2ECC71; -fx-font-weight: bold;"
                    : "-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            statusBox.getChildren().addAll(statusIcon, statusTitle, statusValue);

            // ID Coach (informations techniques)
            HBox idBox = new HBox(10);
            idBox.setAlignment(Pos.CENTER_LEFT);
            Label idIcon = new Label("🆔");
            Label idTitle = new Label("ID Coach:");
            idTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 100;");
            Label idValue = new Label(String.valueOf(coach.getIdCoach()));
            idValue.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px;");
            idBox.getChildren().addAll(idIcon, idTitle, idValue);

            infoSection.getChildren().addAll(styleBox, descBox, statusBox, idBox);

            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; " +
                    "-fx-padding: 10 30;");
            closeButton.setOnAction(e -> detailStage.close());

            // Assemblage
            card.getChildren().addAll(header, infoSection, closeButton);
            card.setAlignment(Pos.CENTER);

            Scene scene = new Scene(card, 500, 500); // Ajusté pour la description
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du coach", Alert.AlertType.ERROR);
        }
    }

    /* ================= CARTE DÉTAIL RÉCOMPENSE ================= */
    private void showRecompenseDetail(Recompense recompense) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("🎁 Détails de la Récompense");
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(challengeListView.getScene().getWindow());

            // Création d'une carte colorée selon le type
            VBox card = new VBox(20);
            card.setPadding(new Insets(25));

            // Couleur de fond selon le type
            String backgroundColor = getColorForRecompenseType(recompense.getTypeRecompense());
            card.setStyle("-fx-background-color: " + backgroundColor + "; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 8);");

            // En-tête avec grande icône
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            // Icône principale
            Label mainIcon = new Label(getIconForRecompenseType(recompense.getTypeRecompense()));
            mainIcon.setStyle("-fx-font-size: 48px;");

            VBox headerInfo = new VBox(5);
            Label nameLabel = new Label(recompense.getTitre());
            nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: white;");

            Label typeLabel = new Label(recompense.getTypeRecompense().toUpperCase());
            typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.9); " +
                    "-fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.2); " +
                    "-fx-padding: 3 10; -fx-background-radius: 10;");

            headerInfo.getChildren().addAll(nameLabel, typeLabel);
            header.getChildren().addAll(mainIcon, headerInfo);

            // Section des informations détaillées
            VBox infoSection = new VBox(15);
            infoSection.setStyle("-fx-background-color: rgba(255,255,255,0.15); " +
                    "-fx-background-radius: 12; -fx-padding: 20;");

            // Description
            HBox descBox = new HBox(10);
            descBox.setAlignment(Pos.CENTER_LEFT);
            Label descIcon = new Label("📄");
            Label descTitle = new Label("Description:");
            descTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120;");

            String description = recompense.getDescription();
            Label descValue = new Label(
                    description != null && !description.isEmpty() ? description : "Aucune description disponible");
            descValue.setStyle("-fx-text-fill: #ECF0F1; -fx-font-size: 13px;");
            descValue.setWrapText(true);
            descValue.setMaxWidth(400);

            VBox descContent = new VBox(5);
            descContent.getChildren().addAll(descTitle, descValue);
            descBox.getChildren().addAll(descIcon, descContent);

            // Condition d'obtention
            HBox conditionBox = new HBox(10);
            conditionBox.setAlignment(Pos.CENTER_LEFT);
            Label conditionIcon = new Label("🎯");
            Label conditionTitle = new Label("Condition:");
            conditionTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120;");

            String condition = recompense.getConditionObtention();
            Label conditionValue = new Label(
                    condition != null && !condition.trim().isEmpty() ? condition : "Aucune condition spécifique");
            conditionValue.setStyle("-fx-text-fill: #F1C40F; -fx-font-weight: bold; -fx-font-size: 13px;");
            conditionValue.setWrapText(true);
            conditionValue.setMaxWidth(400);

            VBox conditionContent = new VBox(5);
            conditionContent.getChildren().addAll(conditionTitle, conditionValue);
            conditionBox.getChildren().addAll(conditionIcon, conditionContent);

            // Informations techniques
            HBox techBox = new HBox(20);
            techBox.setAlignment(Pos.CENTER_LEFT);

            // Statut
            VBox statusBox = new VBox(5);
            Label statusIcon = new Label("📊");
            statusIcon.setStyle("-fx-font-size: 16px;");
            Label statusLabel = new Label("Statut:");
            statusLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
            Label statusValue = new Label(recompense.isActif() ? "ACTIVE" : "INACTIVE");
            statusValue.setStyle(
                    recompense.isActif() ? "-fx-text-fill: #2ECC71; -fx-font-weight: bold; -fx-font-size: 14px;"
                            : "-fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-font-size: 14px;");
            statusBox.getChildren().addAll(statusIcon, statusLabel, statusValue);

            // ID
            VBox idBox = new VBox(5);
            Label idIcon = new Label("🆔");
            idIcon.setStyle("-fx-font-size: 16px;");
            Label idLabel = new Label("ID:");
            idLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
            Label idValue = new Label(String.valueOf(recompense.getIdRecompense()));
            idValue.setStyle("-fx-text-fill: #BDC3C7; -fx-font-weight: bold; -fx-font-size: 14px;");
            idBox.getChildren().addAll(idIcon, idLabel, idValue);

            techBox.getChildren().addAll(statusBox, idBox);

            infoSection.getChildren().addAll(descBox, conditionBox, techBox);

            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                    "-fx-text-fill: #2C3E50; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-padding: 10 30; " +
                    "-fx-font-size: 14px; -fx-cursor: hand;");
            closeButton.setOnAction(e -> detailStage.close());

            // Effet au survol du bouton
            closeButton.setOnMouseEntered(e -> {
                closeButton.setStyle("-fx-background-color: white; " +
                        "-fx-text-fill: #2C3E50; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 30; " +
                        "-fx-font-size: 14px; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            });
            closeButton.setOnMouseExited(e -> {
                closeButton.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                        "-fx-text-fill: #2C3E50; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 10 30; " +
                        "-fx-font-size: 14px; -fx-cursor: hand;");
            });

            // Assemblage
            card.getChildren().addAll(header, infoSection, closeButton);
            card.setAlignment(Pos.CENTER);

            Scene scene = new Scene(card, 500, 550);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails de la récompense", Alert.AlertType.ERROR);
        }
    }

    /* ================= MÉTHODE POUR COULEURS DE RÉCOMPENSES ================= */
    private String getColorForRecompenseType(String type) {
        if (type == null)
            return "linear-gradient(to bottom right, #9B59B6, #8E44AD)";

        switch (type.toLowerCase()) {
            case "médaille":
            case "medaille":
                return "linear-gradient(to bottom right, #F1C40F, #F39C12)"; // Or
            case "badge":
                return "linear-gradient(to bottom right, #3498DB, #2980B9)"; // Bleu
            case "certificat":
                return "linear-gradient(to bottom right, #2ECC71, #27AE60)"; // Vert
            case "points":
                return "linear-gradient(to bottom right, #E74C3C, #C0392B)"; // Rouge
            case "réduction":
            case "reduction":
                return "linear-gradient(to bottom right, #9B59B6, #8E44AD)"; // Violet
            case "cadeau":
                return "linear-gradient(to bottom right, #E67E22, #D35400)"; // Orange
            case "accès premium":
            case "acces premium":
                return "linear-gradient(to bottom right, #1ABC9C, #16A085)"; // Turquoise
            case "autre":
                return "linear-gradient(to bottom right, #34495E, #2C3E50)"; // Gris foncé
            default:
                return "linear-gradient(to bottom right, #9B59B6, #8E44AD)";
        }

    }

    /* ================= CARTE DÉTAIL CHALLENGE ================= */
    private void showChallengeDetail(Challenge challenge) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("🏆 Détails du Challenge");
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(challengeListView.getScene().getWindow());

            // ============= CRÉATION DE LA CARTE PROFESSIONNELLE =============
            VBox card = new VBox(25);
            card.setPadding(new Insets(30));

            // Dégradé de couleur selon le statut et la difficulté
            String gradientColor = getGradientForChallenge(challenge);
            card.setStyle(
                    "-fx-background-color: " + gradientColor + ";" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-color: rgba(255,255,255,0.3);" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0, 0, 10);" +
                            "-fx-min-width: 600;" +
                            "-fx-max-width: 700;");

            // ============= EN-TÊTE AVEC BADGE ET TITRE =============
            HBox headerBox = new HBox(20);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // Icône principale avec cercle de fond
            StackPane iconContainer = new StackPane();
            iconContainer.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.2);" +
                            "-fx-background-radius: 30;" +
                            "-fx-padding: 20;");

            Label mainIcon = new Label(getIconForChallenge(challenge));
            mainIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");
            iconContainer.getChildren().add(mainIcon);

            // Titre et type
            VBox titleBox = new VBox(10);

            Label titleLabel = new Label(challenge.getTitre());
            titleLabel.setStyle(
                    "-fx-font-size: 28px;" +
                            "-fx-font-weight: 900;" +
                            "-fx-text-fill: white;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            titleLabel.setWrapText(true);

            HBox typeBadgeBox = new HBox(10);
            typeBadgeBox.setAlignment(Pos.CENTER_LEFT);

            Label typeBadge = new Label(challenge.getTypeChallenge());
            typeBadge.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.25);" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 8 20;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;");

            Label difficultyBadge = new Label(challenge.getNiveauDifficulte());
            difficultyBadge.setStyle(getDifficultyStyle(challenge.getNiveauDifficulte()));

            typeBadgeBox.getChildren().addAll(typeBadge, difficultyBadge);
            titleBox.getChildren().addAll(titleLabel, typeBadgeBox);

            headerBox.getChildren().addAll(iconContainer, titleBox);

            // ============= BADGE DE STATUT =============
            HBox statusBox = new HBox();
            statusBox.setAlignment(Pos.CENTER_RIGHT);

            HBox.setHgrow(titleBox, Priority.ALWAYS);

            Label statusLabel = new Label();
            if (challenge.isActif()) {
                statusLabel.setText("● ACTIF");
                statusLabel.setStyle(
                        "-fx-background-color: rgba(46,204,113,0.3);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 25;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: rgba(46,204,113,0.8);" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;");
            } else {
                statusLabel.setText("● INACTIF");
                statusLabel.setStyle(
                        "-fx-background-color: rgba(231,76,60,0.3);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 25;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: rgba(231,76,60,0.8);" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;");
            }

            headerBox.getChildren().add(statusBox);

            // ============= SÉPARATEUR ÉLÉGANT =============
            Separator separator = new Separator();
            separator.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.3);" +
                            "-fx-background-insets: 0;" +
                            "-fx-background-radius: 2;" +
                            "-fx-pref-height: 2;");

            // ============= SECTION DESCRIPTION =============
            VBox descriptionSection = new VBox(15);

            Label descTitle = new Label("📝 DESCRIPTION");
            descTitle.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-text-fill: rgba(255,255,255,0.9);");

            Label descriptionContent = new Label(challenge.getDescription());
            descriptionContent.setWrapText(true);
            descriptionContent.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-line-spacing: 5;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 20;" +
                            "-fx-background-color: rgba(255,255,255,0.1);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: rgba(255,255,255,0.2);" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;");
            descriptionContent.setMaxWidth(620);

            descriptionSection.getChildren().addAll(descTitle, descriptionContent);

            // ============= GRILLE D'INFORMATIONS PRINCIPALES =============
            // ============= GRILLE D'INFORMATIONS PRINCIPALES - VERSION AMÉLIORÉE
            // =============
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(15);
            infoGrid.setStyle(
                    "-fx-padding: 20;" +
                            "-fx-background-color: white;" + // Fond blanc
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);" // Ombre légère
            );

            // Configuration des colonnes
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(200);
            col1.setHgrow(Priority.SOMETIMES);

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(200);
            col2.setHgrow(Priority.SOMETIMES);

            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPrefWidth(220);
            col3.setHgrow(Priority.ALWAYS);

            infoGrid.getColumnConstraints().addAll(col1, col2, col3);

            // ===== LIGNE 1 =====
            // DURÉE
            VBox durationBox = createModernInfoBox("⏱️", "DURÉE",
                    challenge.getDureeJours() + " jours", "#2C3E50");
            infoGrid.add(durationBox, 0, 0);

            // DIFFICULTÉ
            VBox difficultyBox = createModernInfoBox("🎯", "DIFFICULTÉ",
                    challenge.getNiveauDifficulte(), getDifficultyTextColor(challenge.getNiveauDifficulte()));
            infoGrid.add(difficultyBox, 1, 0);

            // DATE DE CRÉATION
            VBox creationBox = createModernInfoBox("📅", "DATE DE CRÉATION",
                    getFormattedDate(challenge), "#16A085");
            infoGrid.add(creationBox, 2, 0);

            // ===== LIGNE 2 =====
            // TYPE
            VBox typeInfoBox = createModernInfoBox("📂", "TYPE",
                    challenge.getTypeChallenge(), "#E67E22");
            infoGrid.add(typeInfoBox, 0, 1);

            // ID CHALLENGE
            VBox idBox = createModernInfoBox("🆔", "ID CHALLENGE",
                    "#" + challenge.getIdChallenge(), "#7F8C8D");
            infoGrid.add(idBox, 1, 1);

            // DATE D'EXPIRATION
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(challenge.getDureeJours());
            String expirationDateStr = expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            VBox expirationBox = createModernInfoBox("⏰", "DATE D'EXPIRATION",
                    expirationDateStr, "#C0392B");
            infoGrid.add(expirationBox, 2, 1);

            // Récupérer les coaches et récompenses associés
            List<CoachMotivation> coaches = challengeCoachCrud.getCoachesForChallenge(challenge.getIdChallenge());
            List<Recompense> recompenses = challengeRecompenseCrud
                    .getRecompensesByChallenge(challenge.getIdChallenge());

            // ============= AJOUTER LE BLOCK statsBox ICI =============
            HBox statsBox = new HBox(20);
            statsBox.setAlignment(Pos.CENTER_LEFT);
            statsBox.setStyle(
                    "-fx-padding: 20;" +
                            "-fx-background-color: rgba(0,0,0,0.2);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: rgba(255,255,255,0.1);" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;");
            // =======================================================

            // Badge Coaches
            VBox coachStatsBox = new VBox(8);
            coachStatsBox.setAlignment(Pos.CENTER_LEFT);

            HBox coachHeader = new HBox(10);
            coachHeader.setAlignment(Pos.CENTER_LEFT);
            Label coachIcon = new Label("👨‍🏫");
            coachIcon.setStyle("-fx-font-size: 20px;");
            Label coachCount = new Label(String.valueOf(coaches.size()));
            coachCount.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #3498DB;");
            coachHeader.getChildren().addAll(coachIcon, coachCount);

            Label coachLabel = new Label("Coaches associés");
            coachLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
            coachStatsBox.getChildren().addAll(coachHeader, coachLabel);

            // Badge Récompenses
            VBox rewardStatsBox = new VBox(8);
            rewardStatsBox.setAlignment(Pos.CENTER_LEFT);

            HBox rewardHeader = new HBox(10);
            rewardHeader.setAlignment(Pos.CENTER_LEFT);
            Label rewardIcon = new Label("🎁");
            rewardIcon.setStyle("-fx-font-size: 20px;");
            Label rewardCount = new Label(String.valueOf(recompenses.size()));
            rewardCount.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #F1C40F;");
            rewardHeader.getChildren().addAll(rewardIcon, rewardCount);

            Label rewardLabel = new Label("Récompenses disponibles");
            rewardLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
            rewardStatsBox.getChildren().addAll(rewardHeader, rewardLabel);

            // Badge Statut
            VBox statusStatsBox = new VBox(8);
            statusStatsBox.setAlignment(Pos.CENTER_LEFT);

            HBox statusHeader = new HBox(10);
            statusHeader.setAlignment(Pos.CENTER_LEFT);
            Label statusIcon = new Label("⚡");
            statusIcon.setStyle("-fx-font-size: 20px;");
            Label statusValue = new Label(challenge.isActif() ? "ACTIF" : "INACTIF");
            statusValue
                    .setStyle(challenge.isActif() ? "-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #2ECC71;"
                            : "-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #E74C3C;");
            statusHeader.getChildren().addAll(statusIcon, statusValue);

            Label statusDesc = new Label("État actuel");
            statusDesc.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
            statusStatsBox.getChildren().addAll(statusHeader, statusDesc);

            statsBox.getChildren().addAll(coachStatsBox, rewardStatsBox, statusStatsBox);

            // ============= LISTE DES COACHES (si disponibles) =============
            VBox coachesSection = new VBox(15);
            if (!coaches.isEmpty()) {
                Label coachesTitle = new Label("👨‍🏫 COACHES ASSOCIÉS");
                coachesTitle.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-font-weight: 700;" +
                                "-fx-text-fill: rgba(255,255,255,0.9);");

                // CORRECTION: Utiliser FlowPane au lieu de HBox avec setWrapText
                FlowPane coachesList = new FlowPane(Orientation.HORIZONTAL, 15, 15);
                coachesList.setAlignment(Pos.CENTER_LEFT);

                for (int i = 0; i < Math.min(coaches.size(), 3); i++) {
                    CoachMotivation coach = coaches.get(i);
                    VBox coachCard = createMiniCoachCard(coach);
                    coachesList.getChildren().add(coachCard);
                }

                if (coaches.size() > 3) {
                    Label moreLabel = new Label("+" + (coaches.size() - 3) + " autres");
                    moreLabel.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.2);" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-padding: 8 15;" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-text-fill: white;");
                    moreLabel.setAlignment(Pos.CENTER);

                    // Centrer verticalement
                    VBox moreBox = new VBox(moreLabel);
                    moreBox.setAlignment(Pos.CENTER);
                    moreBox.setPrefHeight(70);
                    coachesList.getChildren().add(moreBox);
                }

                coachesSection.getChildren().addAll(coachesTitle, coachesList);
            }

            // ============= LISTE DES RÉCOMPENSES (si disponibles) =============
            VBox rewardsSection = new VBox(15);
            if (!recompenses.isEmpty()) {
                Label rewardsTitle = new Label("🎁 RÉCOMPENSES ASSOCIÉES");
                rewardsTitle.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-font-weight: 700;" +
                                "-fx-text-fill: rgba(255,255,255,0.9);");

                // CORRECTION: Utiliser FlowPane au lieu de HBox avec setWrapText
                FlowPane rewardsList = new FlowPane(Orientation.HORIZONTAL, 15, 15);
                rewardsList.setAlignment(Pos.CENTER_LEFT);

                for (int i = 0; i < Math.min(recompenses.size(), 3); i++) {
                    Recompense reward = recompenses.get(i);
                    VBox rewardCard = createMiniRewardCard(reward);
                    rewardsList.getChildren().add(rewardCard);
                }

                if (recompenses.size() > 3) {
                    Label moreLabel = new Label("+" + (recompenses.size() - 3) + " autres");
                    moreLabel.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.2);" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-padding: 8 15;" +
                                    "-fx-font-size: 12px;" +
                                    "-fx-text-fill: white;");
                    moreLabel.setAlignment(Pos.CENTER);

                    // Centrer verticalement
                    VBox moreBox = new VBox(moreLabel);
                    moreBox.setAlignment(Pos.CENTER);
                    moreBox.setPrefHeight(70);
                    rewardsList.getChildren().add(moreBox);
                }

                rewardsSection.getChildren().addAll(rewardsTitle, rewardsList);
            }

            // ============= PIED DE CARTE =============
            HBox footerBox = new HBox();
            footerBox.setAlignment(Pos.CENTER_RIGHT);
            footerBox.setStyle("-fx-padding: 15 0 0 0;");

            Label footerLabel = new Label("✨ Challenge créé le " + getFormattedDate(challenge));
            footerLabel.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: rgba(255,255,255,0.6);" +
                            "-fx-font-style: italic;");
            footerBox.getChildren().add(footerLabel);

            // ============= BOUTON FERMER =============
            Button closeButton = new Button("Fermer");
            closeButton.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.9);" +
                            "-fx-text-fill: #2C3E50;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 40;" +
                            "-fx-background-radius: 30;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

            closeButton.setOnMouseEntered(e -> {
                closeButton.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-text-fill: #2C3E50;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 40;" +
                                "-fx-background-radius: 30;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);");
            });

            closeButton.setOnMouseExited(e -> {
                closeButton.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.9);" +
                                "-fx-text-fill: #2C3E50;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 40;" +
                                "-fx-background-radius: 30;" +
                                "-fx-cursor: hand;");
            });

            closeButton.setOnAction(e -> detailStage.close());

            // ============= ASSEMBLAGE FINAL =============
            VBox content = new VBox(20);
            content.getChildren().addAll(
                    headerBox,
                    separator,
                    descriptionSection,
                    infoGrid,
                    statsBox);

            if (!coaches.isEmpty()) {
                content.getChildren().add(coachesSection);
            }

            if (!recompenses.isEmpty()) {
                content.getChildren().add(rewardsSection);
            }

            content.getChildren().addAll(footerBox, closeButton);

            card.getChildren().add(content);

            // ScrollPane pour le contenu
            ScrollPane scrollPane = new ScrollPane(card);
            scrollPane.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background: transparent;" +
                            "-fx-border-color: transparent;");
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Scene scene = new Scene(scrollPane, 700, 800);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du challenge: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /*
     * ================= MISSING HELPER METHODS FOR CHALLENGE DETAIL CARD
     * =================
     */

    /**
     * Returns a gradient color based on challenge status and difficulty
     */
    private String getGradientForChallenge(Challenge challenge) {
        if (challenge == null) {
            return "linear-gradient(to bottom right, #34495E, #2C3E50)";
        }

        if (!challenge.isActif()) {
            return "linear-gradient(to bottom right, #7F8C8D, #2C3E50)";
        }

        switch (challenge.getNiveauDifficulte().toLowerCase()) {
            case "facile":
                return "linear-gradient(to bottom right, #27AE60, #229954)";
            case "moyen":
                return "linear-gradient(to bottom right, #F39C12, #E67E22)";
            case "difficile":
                return "linear-gradient(to bottom right, #E74C3C, #C0392B)";
            case "expert":
                return "linear-gradient(to bottom right, #8E44AD, #6C3483)";
            default:
                return "linear-gradient(to bottom right, #3498DB, #2980B9)";
        }
    }

    /**
     * Returns an appropriate icon based on challenge type
     */
    private String getIconForChallenge(Challenge challenge) {
        if (challenge == null) {
            return "🏆";
        }

        switch (challenge.getTypeChallenge().toLowerCase()) {
            case "programmation":
                return "💻";
            case "design":
                return "🎨";
            case "marketing":
                return "📈";
            case "business":
                return "💼";
            case "personnel":
                return "🧘";
            default:
                return "🏆";
        }
    }

    /**
     * Returns CSS style for difficulty badge
     */
    private String getDifficultyStyle(String difficulty) {
        if (difficulty == null) {
            difficulty = "Moyen";
        }

        String baseStyle = "-fx-background-radius: 20; -fx-padding: 8 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;";

        switch (difficulty.toLowerCase()) {
            case "facile":
                return baseStyle + " -fx-background-color: #27AE60;";
            case "moyen":
                return baseStyle + " -fx-background-color: #F39C12;";
            case "difficile":
                return baseStyle + " -fx-background-color: #E74C3C;";
            case "expert":
                return baseStyle + " -fx-background-color: #8E44AD;";
            default:
                return baseStyle + " -fx-background-color: #3498DB;";
        }
    }

    /**
     * Creates an information box for the challenge detail card
     */
    private VBox createInfoBox(String icon, String title, String value, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-padding: 12;" +
                        "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 12;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);");

        header.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: " + color + ";");
        valueLabel.setWrapText(true);

        box.getChildren().addAll(header, valueLabel);

        return box;
    }

    /**
     * Returns a color based on difficulty level
     */
    private String getDifficultyColor(String difficulty) {
        if (difficulty == null) {
            return "#3498DB";
        }

        switch (difficulty.toLowerCase()) {
            case "facile":
                return "#2ECC71";
            case "moyen":
                return "#F39C12";
            case "difficile":
                return "#E74C3C";
            case "expert":
                return "#9B59B6";
            default:
                return "#3498DB";
        }
    }

    /**
     * Returns formatted date for challenge
     */
    private String getFormattedDate(Challenge challenge) {
        // Since Challenge class doesn't seem to have a creation date field,
        // you can either:
        // 1. Add a creationDate field to Challenge class
        // 2. Return a default value
        // 3. Use the current date

        // Option 2: Return default formatted date
        // You can modify this based on your actual Challenge class structure
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // If you add a creationDate field to Challenge class, use:
        // return
        // challenge.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Creates a mini coach card for the challenge detail view
     */
    private VBox createMiniCoachCard(CoachMotivation coach) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 15;" +
                        "-fx-min-width: 140;" +
                        "-fx-max-width: 140;");

        // Avatar
        Label avatarIcon = new Label("👤");
        avatarIcon.setStyle("-fx-font-size: 24px;");

        // Name
        Label nameLabel = new Label(coach.getNomCoach());
        nameLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        // Style
        Label styleLabel = new Label(coach.getStyle());
        styleLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);");

        card.getChildren().addAll(avatarIcon, nameLabel, styleLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(52,152,219,0.3);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;");
        });

        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> showCoachDetail(coach));

        return card;
    }

    /**
     * Creates a mini reward card for the challenge detail view
     */
    private VBox createMiniRewardCard(Recompense reward) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 15;" +
                        "-fx-min-width: 140;" +
                        "-fx-max-width: 140;");

        // Icon based on reward type
        Label iconLabel = new Label(getIconForRecompenseType(reward.getTypeRecompense()));
        iconLabel.setStyle("-fx-font-size: 24px;");

        // Title
        Label titleLabel = new Label(reward.getTitre());
        titleLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // Type
        Label typeLabel = new Label(reward.getTypeRecompense());
        typeLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);");

        card.getChildren().addAll(iconLabel, titleLabel, typeLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(241,196,15,0.3);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 12 15;" +
                            "-fx-min-width: 140;" +
                            "-fx-max-width: 140;");
        });

        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> showRecompenseDetail(reward));

        return card;
    }

    /**
     * Crée une carte d'information moderne avec texte noir sur fond blanc
     */
    private VBox createModernInfoBox(String icon, String title, String value, String accentColor) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(
                "-fx-padding: 15;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #ECF0F1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        box.setPrefWidth(200);
        box.setMaxWidth(Double.MAX_VALUE);

        // Header avec icône et titre
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #7F8C8D;" + // Gris foncé
                        "-fx-uppercase: true;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Valeur
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: " + accentColor + ";" // Couleur d'accent pour la valeur
        );
        valueLabel.setWrapText(true);

        box.getChildren().addAll(header, valueLabel);

        return box;
    }

    /**
     * Retourne une couleur de texte selon la difficulté
     */
    private String getDifficultyTextColor(String difficulty) {
        if (difficulty == null)
            return "#2C3E50";

        switch (difficulty.toLowerCase()) {
            case "facile":
                return "#27AE60"; // Vert
            case "moyen":
                return "#F39C12"; // Orange
            case "difficile":
                return "#E74C3C"; // Rouge
            case "expert":
                return "#8E44AD"; // Violet
            default:
                return "#2C3E50"; // Gris foncé
        }
    }
}