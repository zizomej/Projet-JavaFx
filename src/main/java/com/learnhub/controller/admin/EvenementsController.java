package com.learnhub.controller.admin;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class EvenementsController {

    @FXML
    private TableView<Evenement> table;
    @FXML
    private TableColumn<Evenement, String> colTitre;
    @FXML
    private TableColumn<Evenement, String> colType;
    @FXML
    private TableColumn<Evenement, String> colDate;
    @FXML
    private TableColumn<Evenement, String> colLieu;
    @FXML
    private TableColumn<Evenement, String> colStatut;
    @FXML
    private TableColumn<Evenement, Void> colActions;

    @FXML
    private Label totalEventsLabel;
    @FXML
    private Label confirmedEventsLabel;
    @FXML
    private Label pendingEventsLabel;
    @FXML
    private Label participantsLabel;
    @FXML
    private Label tableHeaderLabel;
    @FXML
    private TextField navbarSearchField;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private ComboBox<String> comboLieu;
    @FXML
    private ComboBox<String> comboStatut;

    private final EvenementDAO evenementDAO = new EvenementDAO();
    private final ObservableList<Evenement> eventList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupFilters();
    }

    private void setupFilters() {
        // Peupler les types
        comboType.setItems(FXCollections.observableArrayList("🎷 Tous les types", "Conférence", "Fête", "Formation", "Réunion", "Autre"));
        comboType.getSelectionModel().select(0);

        // Peupler les statuts
        comboStatut.setItems(FXCollections.observableArrayList("📊 Tous les statuts", "Planifié", "En cours", "Terminé", "Annulé"));
        comboStatut.getSelectionModel().select(0);

        // Peupler les lieux dynamiquement à partir des données chargées
        updateLieuFilter();

        // Écouter les changements
        navbarSearchField.textProperty().addListener((obs, old, val) -> applyFilters());
        comboType.valueProperty().addListener((obs, old, val) -> applyFilters());
        comboLieu.valueProperty().addListener((obs, old, val) -> applyFilters());
        comboStatut.valueProperty().addListener((obs, old, val) -> applyFilters());
    }

    private void updateLieuFilter() {
        ObservableList<String> lieux = FXCollections.observableArrayList("📍 Tous les lieux");
        eventList.stream()
            .map(Evenement::getLieuNom)
            .filter(l -> l != null && !l.isEmpty())
            .distinct()
            .sorted()
            .forEach(lieux::add);
        comboLieu.setItems(lieux);
        comboLieu.getSelectionModel().select(0);
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieuNom"));

        // 1. Colonne Statut avec Badges
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");

                    // Logique des couleurs de badge
                    String status = item.toLowerCase();
                    if (status.contains("cours") || status.contains("confirmé")) {
                        badge.getStyleClass().add("badge-success");
                    } else if (status.contains("planifié") || status.contains("attente")) {
                        badge.getStyleClass().add("badge-warning");
                    } else if (status.contains("annulé")) {
                        badge.getStyleClass().add("badge-danger");
                    } else {
                        badge.getStyleClass().add("badge-info");
                    }
                    setGraphic(badge);
                }
            }
        });

        // 2. Colonne Actions avec Boutons Circulaires
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final Button promoBtn = new Button("📢");
            private final HBox pane = new HBox(12, promoBtn, editBtn, deleteBtn);
            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");
                promoBtn.getStyleClass().add("action-btn-promo"); // Style à vérifier/ajouter

                promoBtn.setOnAction(e -> handlePromotion(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadData() {
        try {
            eventList.setAll(evenementDAO.findAll());
            table.setItems(eventList);

            // Statistiques
            int total = eventList.size();
            totalEventsLabel.setText(String.valueOf(total));

            long confirmed = eventList.stream()
                    .filter(e -> e.getStatut() != null && (e.getStatut().toLowerCase().contains("cours")
                            || e.getStatut().toLowerCase().contains("confirmé")))
                    .count();
            confirmedEventsLabel.setText(String.valueOf(confirmed));

            long pending = eventList.stream()
                    .filter(e -> e.getStatut() != null && (e.getStatut().toLowerCase().contains("planifié")
                            || e.getStatut().toLowerCase().contains("attente")))
                    .count();
            pendingEventsLabel.setText(String.valueOf(pending));

            // Participants (Simulation pour le style, ou calcul réel si dispo)
            int totalParticipants = eventList.stream().mapToInt(Evenement::getCapacite).sum() / 4; // Mock logic
            participantsLabel.setText(String.valueOf(totalParticipants));

            tableHeaderLabel.setText("📋 Liste des événements (" + total + ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String searchText = navbarSearchField.getText() == null ? "" : navbarSearchField.getText().toLowerCase();
        String selectedType = comboType.getValue();
        String selectedLieu = comboLieu.getValue();
        String selectedStatut = comboStatut.getValue();

        FilteredList<Evenement> filtered = new FilteredList<>(eventList, e -> {
            // Filtre Recherche
            boolean matchSearch = searchText.isEmpty() || 
                e.getTitre().toLowerCase().contains(searchText) ||
                (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchText)) ||
                (e.getLieuNom() != null && e.getLieuNom().toLowerCase().contains(searchText));

            // Filtre Type
            boolean matchType = selectedType == null || selectedType.contains("Tous") || 
                (e.getTypeEvenement() != null && e.getTypeEvenement().equals(selectedType));

            // Filtre Lieu
            boolean matchLieu = selectedLieu == null || selectedLieu.contains("Tous") || 
                (e.getLieuNom() != null && e.getLieuNom().equals(selectedLieu));

            // Filtre Statut
            boolean matchStatut = selectedStatut == null || selectedStatut.contains("Tous") || 
                (e.getStatut() != null && e.getStatut().equalsIgnoreCase(selectedStatut));

            return matchSearch && matchType && matchLieu && matchStatut;
        });

        table.setItems(filtered);
        tableHeaderLabel.setText("📋 Liste des événements (" + filtered.size() + ")");
    }

    @FXML
    private void handleAdd() {
        showForm(null);
    }

    private void handleEdit(Evenement event) {
        showForm(event);
    }

    private void showForm(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/evenement_form.fxml"));
            Parent root = loader.load();
            EvenementFormController controller = loader.getController();
            controller.setEvenement(event);

            Stage stage = (Stage) table.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Evenement event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Cette action supprimera également les inscriptions associées.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                evenementDAO.delete(event.getId());
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void handlePromotion(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/promotion_modal.fxml"));
            Parent root = loader.load();
            PromotionController controller = loader.getController();
            controller.setEvenement(event);

            Stage stage = new Stage();
            stage.setTitle("Outil de Promotion - LearnHub");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(table.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goMetiers() {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), "/fxml/admin/metiers_avances.fxml",
                "Outils Avancés");
    }

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), "/fxml/admin/dashboard.fxml",
                "Tableau de Bord");
    }
}
