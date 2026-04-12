package com.learnhub.controller.admin;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.models.DemandeStage;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DemandesStageController {

    @FXML private Label totalDemandesLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label refuseesLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label etudiantsLabel;
    @FXML private Label offresConcerneesLabel;
    @FXML private Label bannerSubtitleLabel;
    @FXML private Label tableTitleLabel;
    @FXML private TableView<DemandeStage> demandeTable;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private TextField searchField;
    @FXML private TextField navbarSearchField;

    private final DemandeStageDAO dao = new DemandeStageDAO();
    private final ObservableList<DemandeStage> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filtreStatut.getItems().addAll("", "en_attente", "acceptee", "refusee", "en_cours");
        setupColumns();
        demandeTable.setItems(data);
        if (navbarSearchField != null && searchField != null) {
            navbarSearchField.textProperty().bindBidirectional(searchField.textProperty());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        if (filtreStatut != null) {
            filtreStatut.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        loadData();
    }

    @FXML
    private void handleReset() {
        if (searchField != null) {
            searchField.clear();
        }
        if (filtreStatut != null) {
            filtreStatut.setValue("");
        }
        loadData();
    }

    private void setupColumns() {
        demandeTable.getColumns().clear();

        TableColumn<DemandeStage, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(55);

        TableColumn<DemandeStage, String> colEtudiant = new TableColumn<>("Etudiant");
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("etudiantNom"));
        colEtudiant.setPrefWidth(170);

        TableColumn<DemandeStage, String> colOffre = new TableColumn<>("Offre");
        colOffre.setCellValueFactory(new PropertyValueFactory<>("offreTitre"));
        colOffre.setPrefWidth(210);

        TableColumn<DemandeStage, String> colPartenaire = new TableColumn<>("Partenaire");
        colPartenaire.setCellValueFactory(new PropertyValueFactory<>("partenaireNom"));
        colPartenaire.setPrefWidth(150);

        TableColumn<DemandeStage, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colDate.setPrefWidth(110);

        TableColumn<DemandeStage, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item) {
                    case "acceptee" -> setStyle("-fx-text-fill:#059669;-fx-font-weight:700;");
                    case "refusee" -> setStyle("-fx-text-fill:#dc2626;-fx-font-weight:700;");
                    case "en_cours" -> setStyle("-fx-text-fill:#2563eb;-fx-font-weight:700;");
                    default -> setStyle("-fx-text-fill:#b45309;-fx-font-weight:700;");
                }
            }
        });

        TableColumn<DemandeStage, String> colPiece = new TableColumn<>("Piece jointe");
        colPiece.setCellValueFactory(cell -> new ReadOnlyStringWrapper(valueOrFallback(cell.getValue().getPieceJointe(), "Aucune")));
        colPiece.setPrefWidth(160);

        TableColumn<DemandeStage, String> colMotivation = new TableColumn<>("Motivation");
        colMotivation.setCellValueFactory(cell -> new ReadOnlyStringWrapper(truncate(cell.getValue().getMotivation(), 60)));
        colMotivation.setPrefWidth(240);

        TableColumn<DemandeStage, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(290);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button detailsButton = new Button("Details");
            private final Button acceptButton = new Button("Accepter");
            private final Button refuseButton = new Button("Refuser");
            private final Button deleteButton = new Button("Supprimer");

            {
                detailsButton.getStyleClass().add("btn-secondary");
                acceptButton.getStyleClass().add("btn-success");
                refuseButton.getStyleClass().add("btn-warning");
                deleteButton.getStyleClass().add("btn-danger");

                detailsButton.setOnAction(event -> showDetails(getTableView().getItems().get(getIndex())));
                acceptButton.setOnAction(event -> updateStatut(getTableView().getItems().get(getIndex()), "acceptee"));
                refuseButton.setOnAction(event -> updateStatut(getTableView().getItems().get(getIndex()), "refusee"));
                deleteButton.setOnAction(event -> handleDeleteItem(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                setGraphic(new HBox(6, detailsButton, acceptButton, refuseButton, deleteButton));
            }
        });

        demandeTable.getColumns().addAll(
                colId,
                colEtudiant,
                colOffre,
                colPartenaire,
                colDate,
                colStatut,
                colPiece,
                colMotivation,
                colActions
        );
    }

    private void loadData() {
        try {
            List<DemandeStage> demandes = dao.findAll();
            data.setAll(demandes);
            
            int total = demandes.size();
            long enAttente = demandes.stream().filter(d -> "en_attente".equalsIgnoreCase(d.getStatut())).count();
            long acceptees = demandes.stream().filter(d -> "acceptee".equalsIgnoreCase(d.getStatut())).count();
            long refusees = demandes.stream().filter(d -> "refusee".equalsIgnoreCase(d.getStatut())).count();
            long enCours = demandes.stream().filter(d -> "en_cours".equalsIgnoreCase(d.getStatut())).count();
            long distinctEtudiants = demandes.stream().map(DemandeStage::getEtudiantNom).distinct().count();
            long distinctOffres = demandes.stream().map(DemandeStage::getOffreTitre).distinct().count();

            totalDemandesLabel.setText(String.valueOf(total));
            enAttenteLabel.setText(String.valueOf(enAttente));
            accepteesLabel.setText(String.valueOf(acceptees));
            refuseesLabel.setText(String.valueOf(refusees));
            enCoursLabel.setText(String.valueOf(enCours));
            etudiantsLabel.setText(String.valueOf(distinctEtudiants));
            offresConcerneesLabel.setText(String.valueOf(distinctOffres));
            
            bannerSubtitleLabel.setText("Administrateur | " + total + " demande(s) au total");
            tableTitleLabel.setText("Liste des Demandes (" + total + ")");
        } catch (SQLException e) {
            showError("Impossible de charger les demandes : " + e.getMessage());
        }
    }

    @FXML private void handleRefresh() { loadData(); }

    @FXML
    private void handleAccept() {
        DemandeStage selected = demandeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez une demande.");
            return;
        }
        updateStatut(selected, "acceptee");
    }

    @FXML
    private void handleRefuse() {
        DemandeStage selected = demandeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez une demande.");
            return;
        }
        updateStatut(selected, "refusee");
    }

    @FXML
    private void handleDelete() {
        DemandeStage selected = demandeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez une demande.");
            return;
        }
        handleDeleteItem(selected);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        String statut = filtreStatut.getValue();
        try {
            data.setAll(dao.search(query, statut));
        } catch (SQLException e) {
            showError("Recherche impossible : " + e.getMessage());
        }
    }

    private void showDetails(DemandeStage demande) {
        DemandeStageDetailsController.setDemandeToShow(demande);
        navigate("/fxml/admin/demande_stage_details.fxml", "Détails Demande de Stage");
    }

    private void updateStatut(DemandeStage demande, String statut) {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Passer la demande de \"" + demande.getEtudiantNom() + "\" au statut \"" + statut + "\" ?",
                ButtonType.YES,
                ButtonType.NO
        );
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }
        try {
            dao.updateStatut(demande.getId(), statut, null);
            loadData();
        } catch (SQLException e) {
            showError("Mise a jour impossible : " + e.getMessage());
        }
    }

    private void handleDeleteItem(DemandeStage demande) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin/delete_confirmation.fxml"));
            javafx.scene.Parent root = loader.load();
            DeleteConfirmationController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Confirmation");
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Modern look
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Add a drop shadow to root if undecorated
            root.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); -fx-background-radius: 12; -fx-background-color: transparent;");
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            java.util.Map<String, String> details = new java.util.LinkedHashMap<>();
            details.put("Étudiant", safe(demande.getEtudiantNom()));
            details.put("Offre", safe(demande.getOffreTitre()));
            details.put("Partenaire", safe(demande.getPartenaireNom()));
            details.put("Date", safe(demande.getDateDemande()));
            details.put("Statut", safe(demande.getStatut()));

            controller.initData(stage, "demande de stage", "Êtes-vous sûr de vouloir supprimer la demande de \"" + demande.getEtudiantNom() + "\" ?", details);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                dao.delete(demande.getId());
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }
    
    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "...";
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    @FXML private void goDashboard() { navigate("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs() { navigate("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules() { navigate("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances() { navigate("/fxml/admin/seances.fxml", "Seances"); }
    @FXML private void goNotes() { navigate("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences() { navigate("/fxml/admin/presences.fxml", "Presences"); }
    @FXML private void goFilieres() { navigate("/fxml/admin/filieres.fxml", "Filieres"); }
    @FXML private void goEvenements() { navigate("/fxml/admin/evenements.fxml", "Evenements"); }
    @FXML private void goRdv() { navigate("/fxml/admin/rdv.fxml", "RDV medicaux"); }
    @FXML private void goCreneaux() { navigate("/fxml/admin/creneaux.fxml", "Creneaux"); }
    @FXML private void goPartenaires() { navigate("/fxml/admin/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage() { navigate("/fxml/admin/offres_stage.fxml", "Offres de stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/admin/demandes_stage.fxml", "Demandes de stage"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) demandeTable.getScene().getWindow();
        NavigationUtil.navigateToFixed(stage, "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) demandeTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
