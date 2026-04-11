package com.learnhub.controller.admin;

import com.learnhub.dao.*;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalModulesLabel;
    @FXML private Label totalEvenementsLabel;
    @FXML private Label totalRdvLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label totalPartenairesLabel;
    @FXML private Label totalOffresLabel;
    @FXML private Label totalDemandesLabel;
    @FXML private Label totalSeancesLabel;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final EvenementDAO evenementDAO = new EvenementDAO();
    private final RdvDAO rdvDAO = new RdvDAO();
    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private final OffreStageDAO offreStageDAO = new OffreStageDAO();
    private final DemandeStageDAO demandeStageDAO = new DemandeStageDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();

    @FXML
    public void initialize() {
        loadStats();
        String userName = SessionManager.getInstance().getCurrentUser() != null ?
                SessionManager.getInstance().getCurrentUser().getPrenom() : "Admin";
        welcomeLabel.setText("👋 Bienvenue, " + userName);
    }

    private void loadStats() {
        try {
            totalUsersLabel.setText(String.valueOf(utilisateurDAO.count()));
            totalModulesLabel.setText(String.valueOf(moduleDAO.count()));
            totalEvenementsLabel.setText(String.valueOf(evenementDAO.count()));
            totalRdvLabel.setText(String.valueOf(rdvDAO.count()));
            totalPartenairesLabel.setText(String.valueOf(partenaireDAO.count()));
            totalOffresLabel.setText(String.valueOf(offreStageDAO.count()));
            totalDemandesLabel.setText(String.valueOf(demandeStageDAO.count()));
            totalSeancesLabel.setText(String.valueOf(seanceDAO.count()));
        } catch (SQLException e) {
            e.printStackTrace();
            totalUsersLabel.setText("0");
            totalModulesLabel.setText("0");
            totalEvenementsLabel.setText("0");
            totalRdvLabel.setText("0");
            totalPartenairesLabel.setText("0");
            totalOffresLabel.setText("0");
            totalDemandesLabel.setText("0");
            totalSeancesLabel.setText("0");
        }
    }

    // Navigation méthodes
    @FXML private void goUtilisateurs() { navigate("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules() { navigate("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances() { navigate("/fxml/admin/seances.fxml", "Séances"); }
    @FXML private void goNotes() { navigate("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences() { navigate("/fxml/admin/presences.fxml", "Présences"); }
    @FXML private void goFilieres() { navigate("/fxml/admin/filieres.fxml", "Filières"); }
    @FXML private void goRdv() { navigate("/fxml/admin/rdv.fxml", "RDV Médicaux"); }
    @FXML private void goCreneaux() { navigate("/fxml/admin/creneaux.fxml", "Créneaux"); }
    @FXML private void goEvenements() { navigate("/fxml/admin/evenements.fxml", "Événements"); }
    @FXML private void goPartenaires() { navigate("/fxml/admin/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage() { navigate("/fxml/admin/offrestage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/admin/demandestage.fxml", "Demandes de Stage"); }
    @FXML private void goLieux() { navigate("/fxml/admin/lieux.fxml", "Lieux"); }
    @FXML private void goStatistiques() { navigate("/fxml/admin/statistiques.fxml", "Statistiques"); }
    @FXML private void goParametres() { navigate("/fxml/admin/parametres.fxml", "Paramètres"); }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}
