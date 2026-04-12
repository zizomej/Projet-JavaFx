package com.learnhub.controller.student;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.models.DemandeStage;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StudentOffresStageController {

    @FXML private Label topUserName;
    @FXML private Label totalOffresLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreType;
    @FXML private TableView<OffreStage> offreTable;
    @FXML private TableColumn<OffreStage, Integer> colId;
    @FXML private TableColumn<OffreStage, String> colTitre;
    @FXML private TableColumn<OffreStage, String> colPartenaire;
    @FXML private TableColumn<OffreStage, String> colType;
    @FXML private TableColumn<OffreStage, Integer> colDuree;
    @FXML private TableColumn<OffreStage, String> colDateDebut;
    @FXML private TableColumn<OffreStage, String> colStatut;

    private final ObservableList<OffreStage> allOffres = FXCollections.observableArrayList();
    private final OffreStageDAO offreDAO = new OffreStageDAO();
    private final DemandeStageDAO demandeDAO = new DemandeStageDAO();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        if (topUserName != null) {
            topUserName.setText(user.getPrenom() + " " + user.getNom());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colPartenaire.setCellValueFactory(new PropertyValueFactory<>("partenaireNom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeStage"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colStatut.setCellValueFactory(cell -> new ReadOnlyStringWrapper(valueOrFallback(cell.getValue().getFiliereNom(), "—")));

        filtreType.setItems(FXCollections.observableArrayList("", "observation", "initiation", "perfectionnement", "pfe", "ete"));

        loadStats(user.getId());
        loadData();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterTable());
        filtreType.valueProperty().addListener((obs, oldValue, newValue) -> filterTable());
    }

    private void loadStats(int etudiantId) {
        try {
            totalOffresLabel.setText(String.valueOf(offreDAO.count()));

            List<DemandeStage> demandes = demandeDAO.findByEtudiant(etudiantId);
            long pending = demandes.stream().filter(d -> "en_attente".equalsIgnoreCase(d.getStatut())).count();
            long accepted = demandes.stream().filter(d -> "acceptee".equalsIgnoreCase(d.getStatut())).count();

            enAttenteLabel.setText(String.valueOf(pending));
            accepteesLabel.setText(String.valueOf(accepted));
        } catch (SQLException e) {
            showError("Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            allOffres.setAll(offreDAO.findAll());
            offreTable.setItems(allOffres);
        } catch (SQLException e) {
            showError("Impossible de charger les offres : " + e.getMessage());
        }
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String type = filtreType.getValue();

        ObservableList<OffreStage> filtered = allOffres.filtered(offre -> {
            boolean matchesSearch = search.isEmpty()
                    || contains(offre.getTitre(), search)
                    || contains(offre.getPartenaireNom(), search)
                    || contains(offre.getFiliereNom(), search)
                    || contains(offre.getDescription(), search);

            boolean matchesType = type == null || type.isBlank()
                    || (offre.getTypeStage() != null && offre.getTypeStage().equalsIgnoreCase(type));

            return matchesSearch && matchesType;
        });

        offreTable.setItems(filtered);
    }

    @FXML
    private void handlePostuler() {
        OffreStage selected = offreTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Veuillez selectionner une offre de stage.");
            return;
        }

        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        try {
            boolean alreadyApplied = demandeDAO.findByEtudiant(user.getId()).stream()
                    .anyMatch(d -> d.getOffreStageId() == selected.getId());
            if (alreadyApplied) {
                showInfo("Vous avez deja postule a cette offre.");
                return;
            }
        } catch (SQLException e) {
            showError("Verification impossible : " + e.getMessage());
            return;
        }

        Dialog<DemandeStage> dialog = new Dialog<>();
        dialog.setTitle("Postuler a l'offre");
        dialog.setHeaderText(selected.getTitre());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        ButtonType applyButton = new ButtonType("Envoyer ma candidature", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        Label partenaireLabel = new Label(valueOrFallback(selected.getPartenaireNom(), "-"));
        Label filiereLabel = new Label(valueOrFallback(selected.getFiliereNom(), "-"));
        Label typeLabel = new Label(valueOrFallback(selected.getTypeStage(), "-"));
        TextField pieceJointeField = new TextField();
        pieceJointeField.setPromptText("Nom du CV ou reference de piece jointe");
        TextArea motivationArea = new TextArea();
        motivationArea.setPromptText("Expliquez pourquoi vous etes un bon candidat...");
        motivationArea.setWrapText(true);
        motivationArea.setPrefRowCount(6);

        grid.addRow(0, new Label("Partenaire"), partenaireLabel);
        grid.addRow(1, new Label("Filiere"), filiereLabel);
        grid.addRow(2, new Label("Type"), typeLabel);
        grid.addRow(3, new Label("Piece jointe"), pieceJointeField);
        grid.addRow(4, new Label("Motivation *"), motivationArea);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == applyButton) {
                DemandeStage demande = new DemandeStage();
                demande.setEtudiantId(user.getId());
                demande.setOffreStageId(selected.getId());
                demande.setPieceJointe(pieceJointeField.getText().trim());
                demande.setMotivation(motivationArea.getText().trim());
                demande.setDateDemande(LocalDate.now().toString());
                demande.setStatut("en_attente");
                return demande;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(demande -> {
            if (demande.getMotivation() == null || demande.getMotivation().isBlank()) {
                showError("La motivation est obligatoire.");
                return;
            }
            try {
                demandeDAO.insert(demande);
                loadStats(user.getId());
                showInfo("Votre candidature a ete envoyee avec succes.");
            } catch (SQLException e) {
                showError("Erreur lors de l'envoi : " + e.getMessage());
            }
        });
    }

    private boolean contains(String source, String search) {
        return source != null && source.toLowerCase().contains(search);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml", "Espace etudiant"); }
    @FXML private void goModules() { navigate("/fxml/student/modules.fxml", "Modules"); }
    @FXML private void goNotes() { navigate("/fxml/student/notes.fxml", "Notes"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml", "Emploi du temps"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml", "Mes Présences"); }
    @FXML private void goStages() { navigate("/fxml/student/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goRdv() { navigate("/fxml/student/rdv.fxml", "RDV medical"); }
    @FXML private void goEvenements() { navigate("/fxml/student/evenements.fxml", "Evenements"); }
    @FXML private void goOffresStage() { navigate("/fxml/student/offres_stage.fxml", "Offres de stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/student/demandes_stage.fxml", "Mes demandes"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) searchField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) searchField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
