package com.learnhub.controller.student;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.models.DemandeStage;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StudentDemandesStageController {

    @FXML private Label topUserName;
    @FXML private Label statusLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label refuseesLabel;
    @FXML private TableView<DemandeStage> demandeTable;
    @FXML private TableColumn<DemandeStage, Integer> colId;
    @FXML private TableColumn<DemandeStage, String> colOffre;
    @FXML private TableColumn<DemandeStage, String> colPartenaire;
    @FXML private TableColumn<DemandeStage, String> colDate;
    @FXML private TableColumn<DemandeStage, String> colStatut;
    @FXML private TableColumn<DemandeStage, String> colCommentaire;

    // Popup overlay fields
    @FXML private StackPane popupOverlay;
    @FXML private ComboBox<OffreStage> offreCombo;
    @FXML private TextArea motivationArea;
    @FXML private Button cvUploadBtn;
    @FXML private Label cvFileLabel;
    @FXML private Label formErrorLabel;

    private final DemandeStageDAO demandeDAO = new DemandeStageDAO();
    private final OffreStageDAO offreDAO = new OffreStageDAO();
    private File selectedCvFile = null;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        if (topUserName != null) {
            topUserName.setText(user.getPrenom() + " " + user.getNom());
        }

        // Configuration des colonnes
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colOffre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOffreTitre()));
        colPartenaire.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPartenaireNom()));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateDemande()));
        colStatut.setCellValueFactory(cellData -> new SimpleStringProperty(formatStatut(cellData.getValue().getStatut())));
        colCommentaire.setCellValueFactory(cellData -> new SimpleStringProperty(valueForLastColumn(cellData.getValue())));

        loadData(user.getId());
    }

    private String formatStatut(String statut) {
        if (statut == null) return "Inconnu";
        switch (statut.toLowerCase()) {
            case "en_attente": return "⏳ En attente";
            case "acceptee": return "✅ Acceptée";
            case "refusee": return "❌ Refusée";
            default: return statut;
        }
    }

    private void loadData(int etudiantId) {
        try {
            List<DemandeStage> demandes = demandeDAO.findByEtudiant(etudiantId);
            ObservableList<DemandeStage> data = FXCollections.observableArrayList(demandes);
            demandeTable.setItems(data);

            long enAttente = demandes.stream().filter(d -> "en_attente".equalsIgnoreCase(d.getStatut())).count();
            long acceptees = demandes.stream().filter(d -> "acceptee".equalsIgnoreCase(d.getStatut())).count();
            long refusees = demandes.stream().filter(d -> "refusee".equalsIgnoreCase(d.getStatut())).count();

            statusLabel.setText(demandes.size() + " demande(s)");
            enAttenteLabel.setText(String.valueOf(enAttente));
            accepteesLabel.setText(String.valueOf(acceptees));
            refuseesLabel.setText(String.valueOf(refusees));

        } catch (SQLException e) {
            statusLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    private String valueForLastColumn(DemandeStage demande) {
        if (demande.getPieceJointe() != null && !demande.getPieceJointe().isBlank()) {
            return "📎 " + demande.getPieceJointe();
        }
        if (demande.getMotivation() != null && !demande.getMotivation().isBlank()) {
            String motivation = demande.getMotivation();
            return motivation.length() > 40 ? motivation.substring(0, 39) + "..." : motivation;
        }
        return "-";
    }

    // ---------------------------------------------------------------
    // Popup dialog handlers
    // ---------------------------------------------------------------

    @FXML
    private void openNewDemandeDialog() {
        // Load offers into ComboBox
        try {
            List<OffreStage> offres = offreDAO.findAll();
            offreCombo.setItems(FXCollections.observableArrayList(offres));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Reset fields
        offreCombo.getSelectionModel().clearSelection();
        motivationArea.clear();
        cvFileLabel.setText("Aucun fichier selectionne");
        selectedCvFile = null;
        formErrorLabel.setVisible(false);
        formErrorLabel.setManaged(false);

        // Show overlay
        popupOverlay.setVisible(true);
        popupOverlay.setManaged(true);
    }

    @FXML
    private void closeNewDemandeDialog() {
        popupOverlay.setVisible(false);
        popupOverlay.setManaged(false);
    }

    @FXML
    private void handleCvUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre CV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx")
        );
        Stage stage = (Stage) popupOverlay.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedCvFile = file;
            cvFileLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmitDemande() {
        // Validate required fields
        OffreStage selectedOffre = offreCombo.getSelectionModel().getSelectedItem();
        String motivation = motivationArea.getText();

        if (selectedOffre == null) {
            showFormError("Veuillez selectionner une offre de stage.");
            return;
        }
        if (motivation == null || motivation.isBlank()) {
            showFormError("Veuillez rediger votre lettre de motivation.");
            return;
        }

        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            showFormError("Session expriree. Veuillez vous reconnecter.");
            return;
        }

        // Build the demande
        DemandeStage demande = new DemandeStage();
        demande.setEtudiantId(user.getId());
        demande.setOffreStageId(selectedOffre.getId());
        demande.setMotivation(motivation.trim());
        demande.setStatut("en_attente");
        demande.setDateDemande(LocalDate.now().toString());
        if (selectedCvFile != null) {
            demande.setPieceJointe(selectedCvFile.getAbsolutePath());
        }

        try {
            demandeDAO.insert(demande);
            closeNewDemandeDialog();
            loadData(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            showFormError("Erreur lors de l'envoi de la demande. Veuillez reessayer.");
        }
    }

    private void showFormError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);
    }

    // ---------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------

    @FXML
    private void goDashboard() {
        navigate("/fxml/student/dashboard.fxml", "Espace étudiant");
    }

    @FXML
    private void goModules() {
        navigate("/fxml/student/modules.fxml", "Mes modules");
    }

    @FXML
    private void goNotes() {
        navigate("/fxml/student/notes.fxml", "Mes notes");
    }

    @FXML
    private void goEmploi() {
        navigate("/fxml/student/emploi.fxml", "Emploi du temps");
    }

    @FXML
    private void goPresences() {
        navigate("/fxml/student/presences.fxml", "Mes Présences");
    }

    @FXML
    private void goOffresStage() {
        navigate("/fxml/student/offres_stage.fxml", "Offres de stage");
    }

    @FXML
    private void goDemandesStage() {
        navigate("/fxml/student/demandes_stage.fxml", "Mes demandes");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) demandeTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) demandeTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}