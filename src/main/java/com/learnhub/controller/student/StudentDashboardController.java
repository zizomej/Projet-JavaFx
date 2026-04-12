package com.learnhub.controller.student;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.RdvDAO;
import com.learnhub.models.Evenement;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;

public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label moyenneLabel;
    @FXML private Label absencesLabel;
    @FXML private Label rdvLabel;
    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> colModule;
    @FXML private TableColumn<Note, Double> colNote;
    @FXML private TableColumn<Note, String> colMention;
    @FXML private TableView<Evenement> eventsTable;
    @FXML private TableColumn<Evenement, String> colEvtTitre;
    @FXML private TableColumn<Evenement, String> colEvtDate;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getNomComplet());
            loadStats(user.getId());
        }
        if (notesTable != null) {
            colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
            colNote.setCellValueFactory(new PropertyValueFactory<>("valeur"));
            colMention.setCellValueFactory(new PropertyValueFactory<>("mention"));
        }
        if (eventsTable != null) {
            colEvtTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
            colEvtDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
    }

    private void loadStats(int id) {
        try {
            NoteDAO noteDAO = new NoteDAO();
            PresenceDAO presenceDAO = new PresenceDAO();
            RdvDAO rdvDAO = new RdvDAO();

            double moy = noteDAO.getMoyenneEtudiant(id);
            moyenneLabel.setText(String.format("%.2f / 20", moy));

            long absences = presenceDAO.findByEtudiant(id).stream()
                .filter(p -> "ABSENT".equals(p.getStatut())).count();
            absencesLabel.setText(String.valueOf(absences));

            long rdvCount = rdvDAO.findByPatient(id).stream()
                .filter(r -> "EN_ATTENTE".equals(r.getStatut()) || "CONFIRME".equals(r.getStatut())).count();
            rdvLabel.setText(String.valueOf(rdvCount));

            if (notesTable != null) {
                notesTable.setItems(FXCollections.observableArrayList(noteDAO.findByEtudiant(id)));
            }
            if (eventsTable != null) {
                eventsTable.setItems(FXCollections.observableArrayList(new EvenementDAO().findUpcoming()));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void goNotes()          { navigate("/fxml/student/notes.fxml",          "Mes Notes"); }
    @FXML private void goPresences()      { navigate("/fxml/student/presences.fxml",      "Mes Présences"); }
    @FXML private void goModules()        { navigate("/fxml/student/modules.fxml",        "Mes Modules"); }
    @FXML private void goRdv()            { navigate("/fxml/student/rdv.fxml",            "Mes RDV"); }
    @FXML private void goEvenements()     { navigate("/fxml/student/evenements.fxml",     "Événements"); }
    @FXML private void goEmploi()         { navigate("/fxml/student/emploi.fxml",         "Emploi du temps"); }
    @FXML private void goOffresStage()    { navigate("/fxml/student/offres_stage.fxml",   "Offres de Stage"); }
    @FXML private void goDemandesStage()  { navigate("/fxml/student/demandes_stage.fxml", "Mes Demandes de Stage"); }

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

    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml", "Espace Étudiant"); }
}
