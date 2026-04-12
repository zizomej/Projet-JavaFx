package com.learnhub.controller.parent;

import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Note;
import com.learnhub.models.Presence;
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

public class ParentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> colModule;
    @FXML private TableColumn<Note, Double> colNote;
    @FXML private TableColumn<Note, String> colMention;
    @FXML private TableView<Presence> presencesTable;
    @FXML private TableColumn<Presence, String> colSeance;
    @FXML private TableColumn<Presence, String> colStatut;
    @FXML private TableColumn<Presence, String> colDate;
    @FXML private Label moyenneLabel;
    @FXML private Label absencesLabel;
    @FXML private Label modulesLabel;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) welcomeLabel.setText("Bonjour, " + user.getNomComplet());

        if (notesTable != null) {
            colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
            colNote.setCellValueFactory(new PropertyValueFactory<>("valeur"));
            colMention.setCellValueFactory(new PropertyValueFactory<>("mention"));
        }
        if (presencesTable != null) {
            colSeance.setCellValueFactory(new PropertyValueFactory<>("seanceInfo"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // In a real app, the parent would be linked to a specific student
        // For demo, load first available student's data
        loadDemoData();
    }

    private void loadDemoData() {
        try {
            var etudiants = new UtilisateurDAO().findByRole("ROLE_ETUDIANT");
            if (!etudiants.isEmpty()) {
                int etudiantId = etudiants.get(0).getId();

                var notes = new NoteDAO().findByEtudiant(etudiantId);
                if (notesTable != null) notesTable.setItems(FXCollections.observableArrayList(notes));

                double moy = new NoteDAO().getMoyenneEtudiant(etudiantId);
                if (moyenneLabel != null) moyenneLabel.setText(String.format("%.2f / 20", moy));

                var presences = new PresenceDAO().findByEtudiant(etudiantId);
                if (presencesTable != null) presencesTable.setItems(FXCollections.observableArrayList(presences));

                long absences = presences.stream().filter(p -> "ABSENT".equals(p.getStatut())).count();
                if (absencesLabel != null) absencesLabel.setText(String.valueOf(absences));
                if (modulesLabel != null) modulesLabel.setText(String.valueOf(notes.size()));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void goDashboard()  { navigate("/fxml/parent/dashboard.fxml", "Espace Parent"); }
    @FXML private void goNotes()      { navigate("/fxml/student/notes.fxml",    "Notes"); }
    @FXML private void goPresences()  { navigate("/fxml/student/presences.fxml","Présences"); }
    @FXML private void goEmploi()     { navigate("/fxml/student/emploi.fxml",   "Emploi du temps"); }

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
