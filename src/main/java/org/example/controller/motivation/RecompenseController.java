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
import org.example.dao.RecompenseCrud;
import org.example.model.Recompense;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;



import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import java.util.Map;


// ===== JAVA IO =====
import java.io.File;

// ===== DATE =====
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ===== JAVAFX FILECHOOSER =====
import javafx.stage.FileChooser;

// ===== PDFBOX =====
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;


import java.awt.Color;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.text.Normalizer;


public class RecompenseController implements Initializable {

    // ================= LISTVIEW =================
    @FXML
    private ListView<Recompense> recompenseListView;

    // ================= FORMULAIRE =================
    @FXML
    private TextField searchField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button refreshBtn; // Ajouter ce bouton dans le FXML
    @FXML
    private Button challengeBtn; // Renommé depuis backBtn
    @FXML
    private Button coachBtn; // Nouveau bouton
    @FXML
    private Button exportBtn; // Nouveau bouton

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descField; // Renommé depuis descArea
    @FXML
    private ComboBox<String> typeComboBox; // Renommé depuis typeCombo
    @FXML
    private TextField conditionField;
    @FXML
    private CheckBox actifCheckBox;

    // Nouveaux champs du FXML
    @FXML
    private TextField pointsField;
    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private Button addBtn;
    @FXML
    private Button updateBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button clearBtn;

    // ================= STATISTIQUES =================
    @FXML
    private Label totalLabel;
    @FXML
    private Label activesLabel;
    @FXML
    private Label inactivesLabel;
    @FXML
    private Label typesLabel; // Nouveau
    @FXML
    private Label popularTypeLabel; // Nouveau
    @FXML
    private Label avgPointsLabel; // Nouveau

    // ================= STATISTIQUES RÉCOMPENSE SÉLECTIONNÉE =================
    @FXML
    private Label selectedRecompenseLabel;
    @FXML
    private Label selectedTypeLabel;
    @FXML
    private Label selectedPointsLabel;
    @FXML
    private Label selectedCategorieLabel;

    private ObservableList<Recompense> recompenseList = FXCollections.observableArrayList();
    private RecompenseCrud recompenseCrud = new RecompenseCrud();

    // ================= MÉTHODES D'INITIALISATION =================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INITIALIZATION RECOMPENSE CONTROLLER START ===");

        setupListView(); // Remplacé setupTableColumns()
        setupComboBox();
        setupCategorieComboBox(); // Nouvelle méthode
        setupButtons();
        setupInputValidation();
        loadData();
        updateStatistics();

        System.out.println("=== INITIALIZATION RECOMPENSE CONTROLLER END ===");
    }

    // ================= SETUP LISTVIEW AVEC CARTES PERSONNALISÉES =================

    private void setupListView() {
        recompenseListView.setCellFactory(param -> new ListCell<Recompense>() {
            private final HBox card = new HBox(15);
            private final VBox content = new VBox(8);
            private final HBox header = new HBox(10);
            private final HBox infoRow = new HBox(15);
            private final HBox bottomRow = new HBox(10);
            private final VBox rightColumn = new VBox(5);

            private final Label titleLabel = new Label();
            private final Label statusLabel = new Label();
            private final Label typeLabel = new Label();
            private final Label descLabel = new Label();
            private final Label conditionLabel = new Label();
            private final Label pointsLabel = new Label();

            private final HBox actionBox = new HBox(5);
            private final Button detailsBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");

            // REMPLACEZ CE BLOC DANS setupListView() - PARTIE HEADER ET ICÔNES
            {
                // Configuration initiale des composants
                card.setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-background-radius: 15; " + // Augmenté de 12 à 15
                        "-fx-padding: 18; " + // Augmenté de 15 à 18
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 1.5; " + // Augmenté de 1 à 1.5
                        "-fx-cursor: hand;");
                card.setAlignment(Pos.CENTER_LEFT);
                card.setMaxWidth(Double.MAX_VALUE);

                // Configuration du header avec EMOJIS PLUS GRANDS
                header.setAlignment(Pos.CENTER_LEFT);
                header.setSpacing(15); // Ajout d'espacement

                // TITRE avec icône agrandie
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: black;"); // Augmenté de 15 à 18
                titleLabel.setWrapText(true);
                titleLabel.setMaxWidth(280); // Augmenté de 250 à 280

                // STATUT avec badge plus visible
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;"); // Augmenté

                // TYPE avec GRAND EMOJI
                typeLabel.setStyle("-fx-text-fill: #f093fb; -fx-font-size: 14px; -fx-font-weight: bold;"); // Augmenté de 12 à 14
                typeLabel.setGraphicTextGap(8); // Espace entre icône et texte

                // DESCRIPTION
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px;"); // Augmenté de 12 à 13
                descLabel.setMaxWidth(380); // Augmenté de 350 à 380
                descLabel.setMaxHeight(60); // Augmenté de 40 à 60

                // CONDITION avec GRAND EMOJI
                conditionLabel.setWrapText(true);
                conditionLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 12px;"); // Augmenté de 11 à 12
                conditionLabel.setMaxWidth(350); // Augmenté de 300 à 350
                conditionLabel.setGraphicTextGap(6);

                // POINTS avec GRAND EMOJI
                pointsLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 13px; -fx-font-weight: bold;"); // Augmenté de 11 à 13
                pointsLabel.setGraphicTextGap(6);

                // Configuration de la ligne d'info
                infoRow.setAlignment(Pos.CENTER_LEFT);
                infoRow.setSpacing(20); // Augmenté de 15 à 20

                // Configuration de la ligne du bas
                bottomRow.setAlignment(Pos.CENTER_LEFT);
                bottomRow.setSpacing(20); // Augmenté de 15 à 20

                // Configuration de la colonne de droite
                rightColumn.setAlignment(Pos.CENTER_RIGHT);
                rightColumn.setMinWidth(120); // Augmenté de 100 à 120
                rightColumn.setSpacing(8); // Augmenté de 5 à 8

                // BOUTONS D'ACTION avec EMOJIS PLUS GRANDS
                actionBox.setAlignment(Pos.CENTER_RIGHT);
                actionBox.setSpacing(8); // Augmenté de 5 à 8

                detailsBtn.setStyle("-fx-background-color: linear-gradient(to right, #3498DB, #2980B9); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 10; -fx-padding: 8 15; " + // Augmenté de 5 10 à 8 15
                        "-fx-font-size: 14px; -fx-cursor: hand;"); // Augmenté de 11 à 14
                detailsBtn.setTooltip(new Tooltip("Voir les détails"));

                editBtn.setStyle("-fx-background-color: linear-gradient(to right, #f093fb, #f5576c); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 10; -fx-padding: 8 15; " + // Augmenté
                        "-fx-font-size: 14px; -fx-cursor: hand;"); // Augmenté de 11 à 14
                editBtn.setTooltip(new Tooltip("Modifier"));

                actionBox.getChildren().addAll(detailsBtn, editBtn);

                // Assemblage des composants
                VBox leftContent = new VBox(12); // Augmenté de 10 à 12
                leftContent.getChildren().addAll(header, descLabel, infoRow, conditionLabel, bottomRow);

                HBox mainContent = new HBox(20); // Augmenté de 15 à 20
                mainContent.setAlignment(Pos.CENTER_LEFT);
                mainContent.getChildren().addAll(leftContent, rightColumn);

                content.getChildren().addAll(mainContent, actionBox);

                VBox mainCard = new VBox(12); // Augmenté de 10 à 12
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
                    Recompense recompense = getItem();
                    if (recompense != null) {
                        recompenseListView.getSelectionModel().select(recompense);
                        loadRecompenseData(recompense);
                        updateRecompenseStatistics(recompense);
                    }
                });
            }

            @Override

            protected void updateItem(Recompense recompense, boolean empty) {
                super.updateItem(recompense, empty);

                if (empty || recompense == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // TITRE avec GRAND EMOJI 🏆
                    titleLabel.setText("🏆  " + recompense.getTitre()); // Double espace après emoji
                    titleLabel.setGraphicTextGap(10); // Espacement

                    // STATUT avec badge coloré
                    if (recompense.isActif()) {
                        statusLabel.setText("✅  ACTIF"); // Double espace
                        statusLabel.setStyle(statusLabel.getStyle() +
                                "-fx-background-color: rgba(46,204,113,0.25); " + // Plus opaque
                                "-fx-text-fill: #2ECC71; " +
                                "-fx-font-size: 13px;"); // Plus grand
                    } else {
                        statusLabel.setText("❌  INACTIF"); // Double espace
                        statusLabel.setStyle(statusLabel.getStyle() +
                                "-fx-background-color: rgba(231,76,60,0.25); " +
                                "-fx-text-fill: #E74C3C; " +
                                "-fx-font-size: 13px;");
                    }

                    // TYPE avec GRAND EMOJI selon le type
                    String iconType = getIconForType(recompense.getTypeRecompense());
                    // Doubler l'emoji pour le rendre plus visible ou ajouter un espace
                    typeLabel.setText(iconType + "  " + recompense.getTypeRecompense());
                    typeLabel.setStyle("-fx-text-fill: #f093fb; -fx-font-size: 15px; -fx-font-weight: bold;"); // 15px

                    // DESCRIPTION
                    String desc = recompense.getDescription();
                    if (desc.length() > 120) { // Augmenté de 100 à 120
                        desc = desc.substring(0, 117) + "...";
                    }
                    descLabel.setText(desc);
                    descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13.5px;");

                    // CONDITION avec GRAND EMOJI
                    String condition = recompense.getConditionObtention();
                    if (condition.length() > 90) { // Augmenté de 80 à 90
                        condition = condition.substring(0, 87) + "...";
                    }
                    conditionLabel.setText("📋  " + condition); // Double espace
                    conditionLabel.setStyle("-fx-text-fill: #F1C40F; -fx-font-size: 13px;"); // Augmenté

                    // POINTS avec GRAND EMOJI
                    pointsLabel.setText("⭐  " + "100 points"); // Double espace
                    pointsLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 14px; -fx-font-weight: bold;"); // Augmenté

                    // HEADER avec titre et statut
                    header.getChildren().clear();
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    header.getChildren().addAll(titleLabel, spacer, statusLabel);
                    header.setSpacing(15);

                    // INFO ROW avec type
                    infoRow.getChildren().clear();
                    infoRow.getChildren().addAll(typeLabel);
                    infoRow.setSpacing(20);

                    // BOTTOM ROW avec points
                    bottomRow.getChildren().clear();
                    bottomRow.getChildren().addAll(pointsLabel);
                    bottomRow.setSpacing(20);

                    // COLONNE DE DROITE (ID et badge)
                    rightColumn.getChildren().clear();

                    VBox statsBox = new VBox(8); // Espacement augmenté
                    statsBox.setAlignment(Pos.CENTER_RIGHT);

                    Label idLabel = new Label("#" + recompense.getIdRecompense());
                    idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;"); // Augmenté

                    // Badge pour le type avec GRAND EMOJI
                    HBox typeBadge = new HBox(8); // Espacement augmenté
                    typeBadge.setAlignment(Pos.CENTER);
                    Label typeIcon = new Label(getIconForType(recompense.getTypeRecompense()));
                    typeIcon.setStyle("-fx-font-size: 18px;"); // EMOJI ENORME dans le badge
                    Label typeName = new Label(recompense.getTypeRecompense());
                    typeName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: black;"); // Augmenté
                    typeBadge.setStyle("-fx-background-color: rgba(240,147,251,0.25); -fx-background-radius: 15; -fx-padding: 6 12;"); // Plus grand
                    typeBadge.getChildren().addAll(typeIcon, typeName);

                    statsBox.getChildren().addAll(idLabel, typeBadge);
                    rightColumn.getChildren().add(statsBox);

                    // BOUTONS D'ACTION
                    detailsBtn.setOnAction(e -> showRecompenseDetail(recompense));
                    editBtn.setOnAction(e -> {
                        recompenseListView.getSelectionModel().select(recompense);
                        loadRecompenseData(recompense);
                        updateRecompenseStatistics(recompense);
                    });

                    setGraphic(card);
                }
            }
        });

        // Sélection d'un élément
        recompenseListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        loadRecompenseData(newVal);
                        updateRecompenseStatistics(newVal);
                    }
                }
        );

        // Style de la ListView
        recompenseListView.setStyle("-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-padding: 0;");
    }

    // ================= MÉTHODE POUR LES ICÔNES SELON LE TYPE =================

    private String getIconForType(String type) {
        if (type == null) return "🎁";

        switch (type.toLowerCase()) {
            case "médaille":
            case "medaille":
                return "🥇🥇"; // Doublé pour plus de visibilité
            case "badge":
                return "🎖️🎖️"; // Doublé
            case "certificat":
                return "📜📜"; // Doublé
            case "points":
                return "⭐⭐"; // Doublé
            case "réduction":
            case "reduction":
                return "💸💸"; // Doublé
            case "cadeau":
                return "🎁🎁"; // Doublé
            case "accès premium":
            case "acces premium":
                return "👑👑"; // Doublé
            case "autre":
                return "🏆🏆"; // Doublé
            default:
                return "🎁🎁"; // Doublé
        }
    }

    // ================= SETUP COMBOBOX =================

    private void setupComboBox() {
        typeComboBox.getItems().addAll(
                "Médaille",
                "Badge",
                "Certificat",
                "Points",
                "Réduction",
                "Cadeau",
                "Accès Premium",
                "Autre"
        );
        typeComboBox.getSelectionModel().selectFirst();

        // Listener pour changer l'icône selon le type
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Optionnel: afficher un aperçu de l'icône
                System.out.println("Type sélectionné: " + newVal + " icône: " + getIconForType(newVal));
            }
        });
    }

    private void setupCategorieComboBox() {
        if (categorieComboBox != null) {
            categorieComboBox.getItems().addAll(
                    "Standard",
                    "Premium",
                    "VIP",
                    "Spécial",
                    "Événement",
                    "Autre"
            );
            categorieComboBox.getSelectionModel().selectFirst();
        }
    }

    // ================= SETUP BOUTONS =================

    private void setupButtons() {
        addBtn.setOnAction(e -> addRecompense());
        updateBtn.setOnAction(e -> updateRecompense());
        deleteBtn.setOnAction(e -> deleteRecompense());
        clearBtn.setOnAction(e -> clearForm());
        searchBtn.setOnAction(e -> searchRecompenses());

        if (refreshBtn != null) refreshBtn.setOnAction(e -> loadData());

        // Navigation
        if (challengeBtn != null) challengeBtn.setOnAction(e -> goToChallenges());
        if (coachBtn != null) coachBtn.setOnAction(e -> goToCoaches());
        if (exportBtn != null) exportBtn.setOnAction(e -> exportToPDF());

        // Gestion des associations (à conserver si nécessaire)
        // manageChallengesBtn.setOnAction(e -> manageChallengeAssociations());
    }

    // ================= MÉTHODES DE RECHERCHE ET CHARGEMENT =================

    private void searchRecompenses() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadData();
            updateStatistics();
            return;
        }

        List<Recompense> allRecompenses = recompenseCrud.readAll();
        ObservableList<Recompense> filteredList = FXCollections.observableArrayList();

        for (Recompense recompense : allRecompenses) {
            if (recompense.getTitre().toLowerCase().contains(keyword) ||
                    recompense.getDescription().toLowerCase().contains(keyword) ||
                    recompense.getTypeRecompense().toLowerCase().contains(keyword) ||
                    recompense.getConditionObtention().toLowerCase().contains(keyword)) {
                filteredList.add(recompense);
            }
        }

        recompenseList.setAll(filteredList);
        updateStatistics();
    }

    private void loadData() {
        try {
            List<Recompense> recompenses = recompenseCrud.readAll();
            recompenseList.setAll(recompenses);
            recompenseListView.setItems(recompenseList);

            // Mettre à jour les statistiques
            updateStatistics();
            updateTypeStatistics(recompenses);

            System.out.println("Chargement terminé. " + recompenses.size() + " récompense(s) trouvée(s).");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MÉTHODES CRUD =================

    private void addRecompense() {
        try {
            if (!validateForm()) return;

            Recompense recompense = new Recompense(
                    titreField.getText().trim(),
                    descField.getText().trim(),
                    typeComboBox.getValue(),
                    conditionField.getText().trim()
            );

            recompense.setActif(actifCheckBox.isSelected());

            recompenseCrud.create(recompense);
            showAlert("Succès", "Récompense ajoutée avec succès!", Alert.AlertType.INFORMATION);
            clearForm();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateRecompense() {
        Recompense selected = recompenseListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Sélectionnez une récompense à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!validateForm()) return;

            selected.setTitre(titreField.getText().trim());
            selected.setDescription(descField.getText().trim());
            selected.setTypeRecompense(typeComboBox.getValue());
            selected.setConditionObtention(conditionField.getText().trim());
            selected.setActif(actifCheckBox.isSelected());

            recompenseCrud.update(selected);
            showAlert("Succès", "Récompense modifiée avec succès!", Alert.AlertType.INFORMATION);
            recompenseListView.refresh();
            updateStatistics();
            updateTypeStatistics(recompenseList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteRecompense() {
        Recompense selected = recompenseListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Sélectionnez une récompense à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la récompense");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitre() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    recompenseCrud.delete(selected.getIdRecompense());
                    showAlert("Succès", "Récompense supprimée!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ================= MÉTHODES DE CHARGEMENT ET RÉINITIALISATION =================

    private void loadRecompenseData(Recompense recompense) {
        titreField.setText(recompense.getTitre());
        descField.setText(recompense.getDescription());
        typeComboBox.setValue(recompense.getTypeRecompense());
        conditionField.setText(recompense.getConditionObtention());
        actifCheckBox.setSelected(recompense.isActif());

        // Effacer les erreurs de validation lors du chargement
        clearFieldError(titreField);
        clearFieldError(descField);
        clearFieldError(typeComboBox);
        clearFieldError(conditionField);
    }

    private void clearForm() {
        titreField.clear();
        descField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        conditionField.clear();
        if (pointsField != null) pointsField.clear();
        if (categorieComboBox != null) categorieComboBox.getSelectionModel().selectFirst();
        actifCheckBox.setSelected(true);
        recompenseListView.getSelectionModel().clearSelection();

        // Effacer toutes les erreurs de validation
        clearFieldError(titreField);
        clearFieldError(descField);
        clearFieldError(typeComboBox);
        clearFieldError(conditionField);
        if (pointsField != null) clearFieldError(pointsField);

        // Réinitialiser les statistiques de la récompense sélectionnée
        if (selectedRecompenseLabel != null) selectedRecompenseLabel.setText("Aucune récompense sélectionnée");
        if (selectedTypeLabel != null) selectedTypeLabel.setText("-");
        if (selectedPointsLabel != null) selectedPointsLabel.setText("-");
        if (selectedCategorieLabel != null) selectedCategorieLabel.setText("-");
    }

    // ================= MÉTHODES DE NAVIGATION =================

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

    private void goToCoaches() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) coachBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Gestion des Coaches");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des coaches", Alert.AlertType.ERROR);
        }
    }

/*    private void exportToPDF() {
        // À implémenter: exporter les récompenses en PDF
        showAlert("Export PDF", "Fonction d'export PDF à implémenter", Alert.AlertType.INFORMATION);
    }*/

    // ================= MÉTHODES DE STATISTIQUES =================

    private void updateStatistics() {
        if (recompenseList.isEmpty()) {
            if (totalLabel != null) totalLabel.setText("0");
            if (activesLabel != null) activesLabel.setText("0");
            if (inactivesLabel != null) inactivesLabel.setText("0");
            if (typesLabel != null) typesLabel.setText("0");
            if (popularTypeLabel != null) popularTypeLabel.setText("Aucun");
            if (avgPointsLabel != null) avgPointsLabel.setText("0");
            return;
        }

        // Calcul des statistiques
        int total = recompenseList.size();
        long actifs = recompenseList.stream().filter(Recompense::isActif).count();
        long inactifs = total - actifs;

        // Types uniques
        long uniqueTypes = recompenseList.stream()
                .map(Recompense::getTypeRecompense)
                .distinct()
                .count();

        // Mise à jour des labels avec vérification null
        if (totalLabel != null) totalLabel.setText(String.valueOf(total));
        if (activesLabel != null) activesLabel.setText(String.valueOf(actifs));
        if (inactivesLabel != null) inactivesLabel.setText(String.valueOf(inactifs));
        if (typesLabel != null) typesLabel.setText(String.valueOf(uniqueTypes));
    }

    private void updateTypeStatistics(List<Recompense> recompenses) {
        if (recompenses.isEmpty()) {
            if (popularTypeLabel != null) popularTypeLabel.setText("Aucun");
            if (avgPointsLabel != null) avgPointsLabel.setText("0");
            return;
        }

        // Trouver le type le plus populaire
        // À implémenter avec un vrai calcul
        if (popularTypeLabel != null) {
            popularTypeLabel.setText("Badge");
        }

        // Points moyens (si le modèle a des points)
        if (avgPointsLabel != null) {
            avgPointsLabel.setText("100"); // À remplacer par un calcul réel
        }
    }

    private void updateRecompenseStatistics(Recompense recompense) {
        if (recompense == null) return;

        if (selectedRecompenseLabel != null) {
            selectedRecompenseLabel.setText(recompense.getTitre());
        }

        if (selectedTypeLabel != null) {
            selectedTypeLabel.setText(recompense.getTypeRecompense());
        }

        if (selectedPointsLabel != null) {
            selectedPointsLabel.setText("100"); // À remplacer par recompense.getPoints() si existant
        }

        if (selectedCategorieLabel != null) {
            selectedCategorieLabel.setText("Standard"); // À remplacer par recompense.getCategorie() si existant
        }
    }

    /*private void showRecompenseDetail(Recompense recompense) {
        showAlert("Détails de la récompense",
                "🏆 Titre: " + recompense.getTitre() + "\n" +
                        "🎯 Type: " + recompense.getTypeRecompense() + "\n" +
                        "📝 Description: " + recompense.getDescription() + "\n" +
                        "📋 Condition: " + recompense.getConditionObtention() + "\n" +
                        "⚡ Statut: " + (recompense.isActif() ? "Actif" : "Inactif"),
                Alert.AlertType.INFORMATION);
    }*/

    // ================= CONTRÔLE DE SAISIE =================

    private void setupInputValidation() {
        // Validation pour le titre
        titreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (Character.isDigit(newValue.charAt(0))) {
                    showFieldError(titreField, "Le titre ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(titreField, "Le titre ne doit pas être composé uniquement de chiffres");
                    return;
                }
                if (!newValue.matches(".*[a-zA-Z].*")) {
                    showFieldError(titreField, "Le titre doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 100) {
                    showFieldError(titreField, "Le titre ne doit pas dépasser 100 caractères");
                    return;
                }
                clearFieldError(titreField);
            }
        });

        // Validation pour la description
        descField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                String trimmedStart = newValue.replaceAll("^[\\s\\n\\r]+", "");

                if (trimmedStart.length() > 0 && Character.isDigit(trimmedStart.charAt(0))) {
                    showFieldError(descField, "La description ne doit pas commencer par un chiffre");
                    return;
                }

                String withoutLineBreaks = newValue.replaceAll("[\\r\\n]", "");
                String withoutSpaces = withoutLineBreaks.replaceAll("\\s+", "");

                if (withoutSpaces.matches("\\d+")) {
                    showFieldError(descField, "La description ne doit pas être composée uniquement de chiffres");
                    return;
                }

                if (!newValue.matches("(?s).*[a-zA-ZÀ-ÿ].*")) {
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

        // Validation pour la condition d'obtention
        conditionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (newValue.length() > 0 && Character.isDigit(newValue.charAt(0))) {
                    showFieldError(conditionField, "La condition ne doit pas commencer par un chiffre");
                    return;
                }
                if (newValue.matches("\\d+")) {
                    showFieldError(conditionField, "La condition ne doit pas être composée uniquement de chiffres");
                    return;
                }
                if (!newValue.matches(".*[a-zA-Z].*")) {
                    showFieldError(conditionField, "La condition doit contenir au moins une lettre");
                    return;
                }
                if (newValue.length() > 200) {
                    showFieldError(conditionField, "La condition ne doit pas dépasser 200 caractères");
                    return;
                }
                clearFieldError(conditionField);
            }
        });

        // Validation pour le ComboBox type
        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                showFieldError(typeComboBox, "Le type de récompense est obligatoire");
            } else {
                clearFieldError(typeComboBox);
            }
        });
    }

    private void showFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 12px;");
        if (field instanceof TextInputControl) {
            ((TextInputControl) field).setTooltip(new Tooltip(message));
        } else if (field instanceof ComboBox) {
            ((ComboBox<?>) field).setTooltip(new Tooltip(message));
        } else if (field instanceof TextArea) {
            ((TextArea) field).setTooltip(new Tooltip(message));
        }
    }

    private void clearFieldError(Control field) {
        field.setStyle("");
        field.setTooltip(null);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du titre
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            showFieldError(titreField, "Le titre est obligatoire");
            showAlert("Validation", "Le titre est obligatoire", Alert.AlertType.WARNING);
            titreField.requestFocus();
            isValid = false;
        } else if (Character.isDigit(titre.charAt(0))) {
            showFieldError(titreField, "Le titre ne doit pas commencer par un chiffre");
            showAlert("Validation", "Le titre ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
            titreField.requestFocus();
            isValid = false;
        } else if (titre.matches("\\d+")) {
            showFieldError(titreField, "Le titre ne doit pas être composé uniquement de chiffres");
            showAlert("Validation", "Le titre ne doit pas être composé uniquement de chiffres", Alert.AlertType.WARNING);
            titreField.requestFocus();
            isValid = false;
        } else if (!titre.matches(".*[a-zA-Z].*")) {
            showFieldError(titreField, "Le titre doit contenir au moins une lettre");
            showAlert("Validation", "Le titre doit contenir au moins une lettre", Alert.AlertType.WARNING);
            titreField.requestFocus();
            isValid = false;
        } else if (titre.length() > 100) {
            showFieldError(titreField, "Le titre ne doit pas dépasser 100 caractères");
            showAlert("Validation", "Le titre ne doit pas dépasser 100 caractères", Alert.AlertType.WARNING);
            titreField.requestFocus();
            isValid = false;
        } else {
            clearFieldError(titreField);
        }

        // Validation de la description
        String description = descField.getText();
        if (description == null || description.trim().isEmpty()) {
            showFieldError(descField, "La description est obligatoire");
            if (isValid) {
                showAlert("Validation", "La description est obligatoire", Alert.AlertType.WARNING);
                descField.requestFocus();
            }
            isValid = false;
        } else {
            String withoutLineBreaks = description.replaceAll("[\\r\\n]", "");
            String withoutSpaces = withoutLineBreaks.replaceAll("\\s+", "");

            if (withoutSpaces.matches("\\d+")) {
                showFieldError(descField, "La description ne doit pas être composée uniquement de chiffres");
                if (isValid) {
                    showAlert("Validation", "La description ne doit pas être composée uniquement de chiffres", Alert.AlertType.WARNING);
                    descField.requestFocus();
                }
                isValid = false;
            } else if (!description.matches("(?s).*[a-zA-ZÀ-ÿ].*")) {
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
        }

        // Validation du type
        if (typeComboBox.getValue() == null || typeComboBox.getValue().isEmpty()) {
            showFieldError(typeComboBox, "Le type de récompense est obligatoire");
            showAlert("Validation", "Le type de récompense est obligatoire", Alert.AlertType.WARNING);
            if (isValid) typeComboBox.requestFocus();
            isValid = false;
        } else {
            clearFieldError(typeComboBox);
        }

        // Validation de la condition d'obtention
        String condition = conditionField.getText().trim();
        if (condition.isEmpty()) {
            showFieldError(conditionField, "La condition d'obtention est obligatoire");
            if (isValid) {
                showAlert("Validation", "La condition d'obtention est obligatoire", Alert.AlertType.WARNING);
                conditionField.requestFocus();
            }
            isValid = false;
        } else if (condition.length() > 0 && Character.isDigit(condition.charAt(0))) {
            showFieldError(conditionField, "La condition ne doit pas commencer par un chiffre");
            if (isValid) {
                showAlert("Validation", "La condition ne doit pas commencer par un chiffre", Alert.AlertType.WARNING);
                conditionField.requestFocus();
            }
            isValid = false;
        } else if (condition.matches("\\d+")) {
            showFieldError(conditionField, "La condition ne doit pas être composée uniquement de chiffres");
            if (isValid) {
                showAlert("Validation", "La condition ne doit pas être composée uniquement de chiffres", Alert.AlertType.WARNING);
                conditionField.requestFocus();
            }
            isValid = false;
        } else if (!condition.matches(".*[a-zA-Z].*")) {
            showFieldError(conditionField, "La condition doit contenir au moins une lettre");
            if (isValid) {
                showAlert("Validation", "La condition doit contenir au moins une lettre", Alert.AlertType.WARNING);
                conditionField.requestFocus();
            }
            isValid = false;
        } else if (condition.length() > 200) {
            showFieldError(conditionField, "La condition ne doit pas dépasser 200 caractères");
            if (isValid) {
                showAlert("Validation", "La condition ne doit pas dépasser 200 caractères", Alert.AlertType.WARNING);
                conditionField.requestFocus();
            }
            isValid = false;
        } else {
            clearFieldError(conditionField);
        }

        return isValid;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showRecompenseDetail(Recompense recompense) {
        try {
            // Create a custom dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Détails de la récompense");
            dialog.setHeaderText(null);

            // Remove default buttons
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // ============= CRÉATION DE LA CARTE PROFESSIONNELLE =============
            VBox card = new VBox(25);
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa);" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-color: linear-gradient(to right, #667eea, #764ba2);" +
                            "-fx-padding: 30;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 10);" +
                            "-fx-min-width: 550;" +
                            "-fx-max-width: 650;"
            );

            // ============= EN-TÊTE AVEC BADGE ET TITRE =============
            HBox headerBox = new HBox(15);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // Icône principale avec cercle de fond
            StackPane iconContainer = new StackPane();
            iconContainer.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 15;"
            );

            Label mainIcon = new Label(getIconForType(recompense.getTypeRecompense()));
            mainIcon.setStyle("-fx-font-size: 32px; -fx-text-fill: white;");
            iconContainer.getChildren().add(mainIcon);

            // Titre et type
            VBox titleBox = new VBox(8);
            Label titleLabel = new Label(recompense.getTitre());
            titleLabel.setStyle(
                    "-fx-font-size: 28px;" +
                            "-fx-font-weight: 900;" +
                            "-fx-text-fill: #2d3748;"
            );
            titleLabel.setWrapText(true);

            Label typeBadge = new Label(recompense.getTypeRecompense());
            typeBadge.setStyle(
                    "-fx-background-color: #edf2f7;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 8 20;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #4a5568;"
            );

            titleBox.getChildren().addAll(titleLabel, typeBadge);
            headerBox.getChildren().addAll(iconContainer, titleBox);

            // ============= BADGE DE STATUT =============
            HBox statusBox = new HBox();
            statusBox.setAlignment(Pos.CENTER_RIGHT);

            Label statusLabel = new Label();
            if (recompense.isActif()) {
                statusLabel.setText("✓ ACTIF");
                statusLabel.setStyle(
                        "-fx-background-color: #c6f6d5;" +
                                "-fx-text-fill: #22543d;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 20;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: #9ae6b4;" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;"
                );
            } else {
                statusLabel.setText("✗ INACTIF");
                statusLabel.setStyle(
                        "-fx-background-color: #fed7d7;" +
                                "-fx-text-fill: #742a2a;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 20;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: #fc8181;" +
                                "-fx-border-radius: 20;" +
                                "-fx-border-width: 1.5;"
                );
            }

            HBox.setHgrow(titleBox, Priority.ALWAYS);
            headerBox.getChildren().add(statusBox);

            // ============= SÉPARATEUR ÉLÉGANT =============
            Separator separator = new Separator();
            separator.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2, transparent);" +
                            "-fx-background-insets: 0;" +
                            "-fx-background-radius: 2;" +
                            "-fx-pref-height: 3;"
            );

            // ============= SECTION DESCRIPTION =============
            VBox descriptionSection = new VBox(12);
            descriptionSection.setStyle("-fx-padding: 10 0;");

            Label descTitle = new Label("📝 DESCRIPTION");
            descTitle.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-text-fill: #2d3748;"
            );

            Label descriptionContent = new Label(recompense.getDescription());
            descriptionContent.setWrapText(true);
            descriptionContent.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-line-spacing: 5;" +
                            "-fx-text-fill: #4a5568;" +
                            "-fx-padding: 15;" +
                            "-fx-background-color: #f7fafc;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;"
            );
            descriptionContent.setMaxWidth(580);

            descriptionSection.getChildren().addAll(descTitle, descriptionContent);

            // ============= SECTION CONDITION =============
            VBox conditionSection = new VBox(12);

            Label conditionTitle = new Label("📋 CONDITION D'OBTENTION");
            conditionTitle.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-weight: 700;" +
                            "-fx-text-fill: #2d3748;"
            );

            Label conditionContent = new Label(recompense.getConditionObtention());
            conditionContent.setWrapText(true);
            conditionContent.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-line-spacing: 5;" +
                            "-fx-text-fill: #4a5568;" +
                            "-fx-padding: 15;" +
                            "-fx-background-color: #fefcbf;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: #fbd38d;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-width: 1;"
            );
            conditionContent.setMaxWidth(580);

            conditionSection.getChildren().addAll(conditionTitle, conditionContent);

            // ============= GRILLE D'INFORMATIONS =============
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(20);
            infoGrid.setVgap(15);
            infoGrid.setStyle(
                    "-fx-padding: 20 0;" +
                            "-fx-background-color: #edf2f7;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 25;"
            );

            // ID
            Label idLabel = new Label("🆔 ID");
            idLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #4a5568; -fx-font-size: 14px;");
            Label idValue = new Label("#" + recompense.getIdRecompense());
            idValue.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #2d3748;");
            infoGrid.add(idLabel, 0, 0);
            infoGrid.add(idValue, 1, 0);

            // Points
            Label pointsLabel = new Label("⭐ POINTS");
            pointsLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #4a5568; -fx-font-size: 14px;");
            Label pointsValue = new Label("100 points");
            pointsValue.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #2ECC71;");
            infoGrid.add(pointsLabel, 0, 1);
            infoGrid.add(pointsValue, 1, 1);

            // Catégorie
            Label categorieLabel = new Label("🏷️ CATÉGORIE");
            categorieLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #4a5568; -fx-font-size: 14px;");
            Label categorieValue = new Label("Standard");
            categorieValue.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #9F7AEA;");
            infoGrid.add(categorieLabel, 0, 2);
            infoGrid.add(categorieValue, 1, 2);

            // Date création
            Label dateLabel = new Label("📅 CRÉÉ LE");
            dateLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #4a5568; -fx-font-size: 14px;");
            Label dateValue = new Label("12/05/2024");
            dateValue.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #667eea;");
            infoGrid.add(dateLabel, 2, 0);
            infoGrid.add(dateValue, 3, 0);

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(100);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(150);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPrefWidth(100);
            ColumnConstraints col4 = new ColumnConstraints();
            col4.setPrefWidth(150);
            infoGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

            // ============= BADGES DE TYPE SUPPLÉMENTAIRES =============
            HBox badgesBox = new HBox(10);
            badgesBox.setAlignment(Pos.CENTER_LEFT);
            badgesBox.setStyle("-fx-padding: 15 0 0 0;");

            Label categoryBadge = new Label(getIconForType(recompense.getTypeRecompense()) + " " + recompense.getTypeRecompense());
            categoryBadge.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea20, #764ba220);" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 8 18;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #667eea;"
            );

            Label popularityBadge = new Label("🔥 Populaire");
            popularityBadge.setStyle(
                    "-fx-background-color: #fefcbf;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 8 18;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #975a16;"
            );

            badgesBox.getChildren().addAll(categoryBadge, popularityBadge);

            // ============= PIED DE CARTE =============
            HBox footerBox = new HBox();
            footerBox.setAlignment(Pos.CENTER_RIGHT);
            footerBox.setStyle("-fx-padding: 20 0 0 0;");

            Label footerLabel = new Label("✨ Récompense " + (recompense.isActif() ? "active" : "inactive"));
            footerLabel.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: #a0aec0;" +
                            "-fx-font-style: italic;"
            );
            footerBox.getChildren().add(footerLabel);

            // ============= ASSEMBLAGE FINAL =============
            card.getChildren().addAll(
                    headerBox,
                    separator,
                    descriptionSection,
                    conditionSection,
                    infoGrid,
                    badgesBox,
                    footerBox
            );

            // ScrollPane pour le contenu
            ScrollPane scrollPane = new ScrollPane(card);
            scrollPane.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background: transparent;" +
                            "-fx-border-color: transparent;"
            );
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 20;");

            // Appliquer le style au bouton
            Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
            closeButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 10 30;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;"
            );

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'affichage des détails", Alert.AlertType.ERROR);
        }
    }



    private void exportToPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les récompenses en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            fileChooser.setInitialFileName("recompenses_" +
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

            // Pages suivantes: Details des récompenses
            createRecompenseDetailsPages(document);

            // Sauvegarder le document
            document.save(file);
            document.close();

            showAlert("Succès",
                    "PDF généré avec succès!\n\n" +
                            "Fichier: " + file.getName() + "\n" +
                            "Chemin: " + file.getAbsolutePath() + "\n" +
                            "Nombre de récompenses: " + recompenseList.size(),
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

            // Fond coloré en haut (rose/fuchsia comme dans votre interface)
            contentStream.setNonStrokingColor(new Color(240, 147, 251));
            contentStream.addRect(0, pageHeight - 200, pageWidth, 200);
            contentStream.fill();

            // Titre principal
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
            contentStream.newLineAtOffset(50, pageHeight - 100);
            contentStream.showText("RAPPORT DES RECOMPENSES");
            contentStream.endText();

            // Sous-titre
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 16);
            contentStream.newLineAtOffset(50, pageHeight - 140);
            contentStream.showText("Analyse complete et statistiques");
            contentStream.endText();

            // Date de génération
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

            long actifs = recompenseList.stream().filter(Recompense::isActif).count();
            long inactifs = recompenseList.size() - actifs;

            // Carte 1: Total
            drawStatCard(contentStream, 50, cardY, cardWidth, cardHeight,
                    new Color(240, 147, 251), "TOTAL",
                    String.valueOf(recompenseList.size()), "Recompenses");

            // Carte 2: Actifs
            drawStatCard(contentStream, 50 + cardWidth + spacing, cardY, cardWidth, cardHeight,
                    new Color(46, 204, 113), "ACTIVES",
                    String.valueOf(actifs), "Recompenses");

            // Carte 3: Inactifs
            drawStatCard(contentStream, 50 + 2 * (cardWidth + spacing), cardY, cardWidth, cardHeight,
                    new Color(231, 76, 60), "INACTIVES",
                    String.valueOf(inactifs), "Recompenses");

            // Pied de page avec ligne décorative
            contentStream.setStrokingColor(new Color(240, 147, 251));
            contentStream.setLineWidth(3);
            contentStream.moveTo(50, 100);
            contentStream.lineTo(pageWidth - 50, 100);
            contentStream.stroke();

            // Texte du pied de page
            contentStream.beginText();
            contentStream.setNonStrokingColor(new Color(127, 140, 141));
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
            contentStream.newLineAtOffset(50, 70);
            contentStream.showText("Genere automatiquement par le systeme de gestion des recompenses");
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

        // Unité
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
            contentStream.setNonStrokingColor(new Color(240, 147, 251));
            contentStream.addRect(0, pageHeight - 80, pageWidth, 80);
            contentStream.fill();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.newLineAtOffset(50, pageHeight - 50);
            contentStream.showText("STATISTIQUES DÉTAILLÉES");
            contentStream.endText();

            float currentY = pageHeight - 120;

            // Section: Statistiques générales
            currentY = drawSectionTitle(contentStream, currentY, "Statistiques générales");
            currentY -= 20;

            long total = recompenseList.size();
            long actifs = recompenseList.stream().filter(Recompense::isActif).count();
            long inactifs = total - actifs;
            long uniqueTypes = recompenseList.stream()
                    .map(Recompense::getTypeRecompense)
                    .distinct()
                    .count();

            currentY = drawStatLine(contentStream, currentY, "Total des récompenses", String.valueOf(total));
            currentY = drawStatLine(contentStream, currentY, "Récompenses actives", String.valueOf(actifs));
            currentY = drawStatLine(contentStream, currentY, "Récompenses inactives", String.valueOf(inactifs));
            currentY = drawStatLine(contentStream, currentY, "Types distincts", String.valueOf(uniqueTypes));

            currentY -= 30;

            // Section: Répartition par type
            currentY = drawSectionTitle(contentStream, currentY, "Répartition par type");
            currentY -= 20;

            Map<String, Long> typeStats = recompenseList.stream()
                    .collect(Collectors.groupingBy(Recompense::getTypeRecompense, Collectors.counting()));

            for (Map.Entry<String, Long> entry : typeStats.entrySet()) {
                currentY = drawStatLine(contentStream, currentY, removeAccents(entry.getKey()), entry.getValue().toString());
            }

            currentY -= 30;

            // Section: Statut des récompenses
            currentY = drawSectionTitle(contentStream, currentY, "Statut des récompenses");
            currentY -= 20;

            currentY = drawStatLine(contentStream, currentY, "Récompenses actives", String.valueOf(actifs));
            currentY = drawStatLine(contentStream, currentY, "Récompenses inactives", String.valueOf(inactifs));

            // Numéro de page
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
        contentStream.setStrokingColor(new Color(240, 147, 251));
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
        contentStream.setNonStrokingColor(new Color(240, 147, 251));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(300, y);
        contentStream.showText(value);
        contentStream.endText();

        return y - 25;
    }

    private void createRecompenseDetailsPages(PDDocument document) throws Exception {
        int pageNumber = 3;
        int recompensesPerPage = 2;

        for (int i = 0; i < recompenseList.size(); i += recompensesPerPage) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // En-tête de la page
                contentStream.setNonStrokingColor(new Color(240, 147, 251));
                contentStream.addRect(0, pageHeight - 60, pageWidth, 60);
                contentStream.fill();

                contentStream.beginText();
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.newLineAtOffset(50, pageHeight - 40);
                contentStream.showText("DÉTAIL DES RÉCOMPENSES");
                contentStream.endText();

                float currentY = pageHeight - 100;

                for (int j = i; j < Math.min(i + recompensesPerPage, recompenseList.size()); j++) {
                    Recompense recompense = recompenseList.get(j);
                    currentY = drawRecompenseCard(contentStream, recompense, currentY, pageWidth);
                    currentY -= 40;
                }

                // Numéro de page
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

    private float drawRecompenseCard(PDPageContentStream contentStream, Recompense recompense,
                                     float y, float pageWidth) throws Exception {
        float cardHeight = 200;
        float margin = 50;
        float cardWidth = pageWidth - 2 * margin;

        Color cardColor = recompense.isActif() ?
                new Color(236, 240, 241) : new Color(250, 235, 235);

        contentStream.setNonStrokingColor(cardColor);
        contentStream.addRect(margin, y - cardHeight, cardWidth, cardHeight);
        contentStream.fill();

        Color borderColor = recompense.isActif() ?
                new Color(46, 204, 113) : new Color(231, 76, 60);

        contentStream.setNonStrokingColor(borderColor);
        contentStream.addRect(margin, y - cardHeight, 10, cardHeight);
        contentStream.fill();

        float textX = margin + 20;
        float textY = y - 25;

        // Titre SANS emoji
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(44, 62, 80));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.newLineAtOffset(textX, textY);
        String titre = recompense.getTitre();
        if (titre.length() > 40) {
            titre = titre.substring(0, 37) + "...";
        }
        contentStream.showText("Recompense: " + removeAccents(titre));
        contentStream.endText();

        textY -= 25;

        // Type SANS icône
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(240, 147, 251));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(textX, textY);
        contentStream.showText("Type: " + removeAccents(recompense.getTypeRecompense()));
        contentStream.endText();

        textY -= 25;

        // Description
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(52, 73, 94));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(textX, textY);
        String desc = recompense.getDescription();
        if (desc.length() > 80) {
            desc = desc.substring(0, 77) + "...";
        }
        contentStream.showText(removeAccents(desc));
        contentStream.endText();

        textY -= 25;

        // Condition d'obtention SANS emoji
        contentStream.beginText();
        contentStream.setNonStrokingColor(new Color(241, 196, 15));
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(textX, textY);
        String condition = recompense.getConditionObtention();
        if (condition.length() > 70) {
            condition = condition.substring(0, 67) + "...";
        }
        contentStream.showText("Condition: " + removeAccents(condition));
        contentStream.endText();

        textY -= 25;

        // ID et Points
        drawInfoItem(contentStream, textX, textY, "ID:", String.valueOf(recompense.getIdRecompense()));
        drawInfoItem(contentStream, textX + 200, textY, "Points:", "100");

        textY -= 20;

        // Catégorie (simulée)
        drawInfoItem(contentStream, textX, textY, "Categorie:", "Standard");

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
        contentStream.showText(recompense.isActif() ? "ACTIF" : "INACTIF");
        contentStream.endText();

        return y - cardHeight;
    }

// Supprimer cette méthode car elle n'est plus utilisée
// private String getIconForTypePDF(String type) { ... }

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

    // Méthode utilitaire pour les icônes PDF
    private String getIconForTypePDF(String type) {
        if (type == null) return "🎁";

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

    // Méthode utilitaire pour supprimer les accents
    private String removeAccents(String text) {
        if (text == null) return "";

        text = text.replace("\n", " ")
                .replace("\r", " ");

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }



}