package com.learnhub.controller.admin;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.dao.CoursDAO;
import com.learnhub.dao.RdvDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalModulesLabel;
    @FXML private Label totalEvenementsLabel;
    @FXML private Label totalRdvLabel;
    @FXML private Label totalPartenairesLabel;
    @FXML private Label offresStageLabel;
    @FXML private Label demandesStageLabel;
    @FXML private Label filieresLabel;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) welcomeLabel.setText("Bienvenue, " + user.getNomComplet());

        try {
            totalUsersLabel.setText(String.valueOf(new UtilisateurDAO().count()));
            totalModulesLabel.setText(String.valueOf(new CoursDAO().count()));
            totalEvenementsLabel.setText(String.valueOf(new EvenementDAO().count()));
            totalRdvLabel.setText(String.valueOf(new RdvDAO().count()));
            totalPartenairesLabel.setText(String.valueOf(new PartenaireDAO().count()));
            offresStageLabel.setText(String.valueOf(new OffreStageDAO().findAll().size()));
            demandesStageLabel.setText(String.valueOf(new DemandeStageDAO().countByStatut("en_attente")));
            filieresLabel.setText(String.valueOf(new FiliereDAO().count()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void goUtilisateurs() { navigate("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules()      { navigate("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances()      { navigate("/fxml/admin/seances.fxml", "Séances"); }
    @FXML private void goNotes()        { navigate("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences()    { navigate("/fxml/admin/presences.fxml", "Présences"); }
    @FXML private void goEvenements()   { navigate("/fxml/admin/evenements.fxml", "Événements"); }
    @FXML private void goRdv()          { navigate("/fxml/admin/rdv.fxml", "RDV Médicaux"); }
    @FXML private void goCreneaux()     { navigate("/fxml/admin/creneaux.fxml", "Créneaux"); }
    @FXML private void goFilieres()       { navigate("/fxml/admin/filieres.fxml", "Filières"); }
    @FXML private void goPartenaires()    { navigate("/fxml/admin/partenaires/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage()    { navigate("/fxml/admin/offrestage/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage()  { navigate("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage"); }
    @FXML private void goMailing()        { navigate("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing"); }
    @FXML private void goDashboard()      { navigate("/fxml/admin/dashboard.fxml", "Tableau de bord"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
