package com.learnhub.controller.admin;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class OffresStageController {

    @FXML private Label totalOffresLabel;
    @FXML private Label observationLabel;
    @FXML private Label pfeLabel;
    @FXML private Label eteLabel;
    @FXML private Label bannerSubtitleLabel;
    @FXML private Label tableTitleLabel;
    @FXML private TableView<OffreStage> offreTable;
    @FXML private ComboBox<String> filtreType;
    @FXML private TextField searchField;
    @FXML private TextField navbarSearchField;

    private final OffreStageDAO offreDao = new OffreStageDAO();
    private final DemandeStageDAO demandeDao = new DemandeStageDAO();
    private final PartenaireDAO partenaireDao = new PartenaireDAO();
    private final FiliereDAO filiereDao = new FiliereDAO();
    private final ObservableList<OffreStage> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filtreType.getItems().addAll("", "observation", "initiation", "perfectionnement", "pfe", "ete");
        setupColumns();
        offreTable.setItems(data);
        if (navbarSearchField != null && searchField != null) {
            navbarSearchField.textProperty().bindBidirectional(searchField.textProperty());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        if (filtreType != null) {
            filtreType.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        loadData();
    }

    private void setupColumns() {
        offreTable.getColumns().clear();

        TableColumn<OffreStage, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(55);

        TableColumn<OffreStage, String> colTitre = new TableColumn<>("Offre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(230);

        TableColumn<OffreStage, String> colPartenaire = new TableColumn<>("Partenaire");
        colPartenaire.setCellValueFactory(new PropertyValueFactory<>("partenaireNom"));
        colPartenaire.setPrefWidth(160);

        TableColumn<OffreStage, String> colFiliere = new TableColumn<>("Filiere");
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiereNom"));
        colFiliere.setPrefWidth(150);

        TableColumn<OffreStage, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("typeStage"));
        colType.setPrefWidth(130);
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item.toUpperCase());
                setStyle("-fx-text-fill:#1d4ed8;-fx-font-weight:700;");
            }
        });

        TableColumn<OffreStage, Integer> colDuree = new TableColumn<>("Duree");
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        colDuree.setPrefWidth(90);
        colDuree.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item + " mois");
            }
        });

        TableColumn<OffreStage, String> colPublication = new TableColumn<>("Publication");
        colPublication.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colPublication.setPrefWidth(120);

        TableColumn<OffreStage, Integer> colCandidatures = new TableColumn<>("Candidatures");
        colCandidatures.setCellValueFactory(new PropertyValueFactory<>("candidatureCount"));
        colCandidatures.setPrefWidth(120);

        TableColumn<OffreStage, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(200);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.getStyleClass().add("btn-warning");
                deleteButton.getStyleClass().add("btn-danger");
                editButton.setStyle("-fx-cursor: hand; -fx-font-size: 11px;");
                deleteButton.setStyle("-fx-cursor: hand; -fx-font-size: 11px;");
                editButton.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteItem(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                setGraphic(new HBox(8, editButton, deleteButton));
            }
        });

        offreTable.getColumns().addAll(
                colId,
                colTitre,
                colPartenaire,
                colFiliere,
                colType,
                colDuree,
                colPublication,
                colCandidatures,
                colActions
        );
    }

    private void loadData() {
        try {
            List<OffreStage> offres = offreDao.findAll();
            data.setAll(offres);
            
            int total = offres.size();
            long observation = offres.stream().filter(o -> o.getTypeStage() != null && o.getTypeStage().toLowerCase().contains("observation")).count();
            long pfe = offres.stream().filter(o -> o.getTypeStage() != null && o.getTypeStage().toLowerCase().contains("pfe")).count();
            long ete = offres.stream().filter(o -> o.getTypeStage() != null && (o.getTypeStage().toLowerCase().contains("ete") || o.getTypeStage().toLowerCase().contains("été"))).count();
            
            totalOffresLabel.setText(String.valueOf(total));
            observationLabel.setText(String.valueOf(observation));
            pfeLabel.setText(String.valueOf(pfe));
            eteLabel.setText(String.valueOf(ete));
            
            bannerSubtitleLabel.setText("Administrateur | " + total + " offre(s) disponible(s)");
            tableTitleLabel.setText("Liste des Offres (" + total + ")");
        } catch (SQLException e) {
            showError("Impossible de charger les offres : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        String type = filtreType.getValue();
        try {
            data.setAll(offreDao.search(query, type, null));
        } catch (SQLException e) {
            showError("Recherche impossible : " + e.getMessage());
        }
    }

    @FXML private void handleAdd() { showDialog(new OffreStage(), false); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML
    private void handleEdit() {
        OffreStage selected = offreTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez une offre.");
            return;
        }
        showDialog(selected, true);
    }

    @FXML
    private void handleDelete() {
        OffreStage selected = offreTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez une offre.");
            return;
        }
        handleDeleteItem(selected);
    }

    private void handleEdit(OffreStage offre) {
        showDialog(offre, true);
    }

    private void handleDeleteItem(OffreStage offre) {
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
            details.put("Titre", safe(offre.getTitre()));
            details.put("Partenaire", safe(offre.getPartenaireNom()));
            details.put("Filière", safe(offre.getFiliereNom()));
            details.put("Type", safe(offre.getTypeStage()));

            controller.initData(stage, "offre", "Êtes-vous sûr de vouloir supprimer l'offre \"" + offre.getTitre() + "\" ?", details);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                offreDao.delete(offre.getId());
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void showDialog(OffreStage offre, boolean edit) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin/offre_stage_form.fxml"));
            javafx.scene.Parent root = loader.load();
            OffreStageFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(edit ? "Modifier une offre" : "Nouvelle offre de stage");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Pass data to the form controller
            controller.initData(offre, edit, stage, this::loadData);
            
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
        }
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException ignored) {
            return LocalDate.now();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
        Stage stage = (Stage) offreTable.getScene().getWindow();
        NavigationUtil.navigateToFixed(stage, "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) offreTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
