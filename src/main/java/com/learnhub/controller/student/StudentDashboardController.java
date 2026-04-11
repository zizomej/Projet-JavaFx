package com.learnhub.controller.student;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.RdvDAO;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class StudentDashboardController {
    @FXML private Label topUserName;
    @FXML private Label welcomeTitleLabel;
    @FXML private Label emailBadge;

    @FXML private Label modulesLabel;
    @FXML private Label moyenneLabel;
    @FXML private Label absencesLabel;
    @FXML private Label stagesLabel;

    private final NoteDAO noteDAO = new NoteDAO();
    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final RdvDAO rdvDAO = new RdvDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            topUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            welcomeTitleLabel.setText("Bonjour " + currentUser.getPrenom() + " " + currentUser.getNom() + " ! 👋");
            emailBadge.setText("📧 " + currentUser.getEmail());
            loadStudentData(currentUser.getId());
        }
    }

    private void loadStudentData(int etudiantId) {
        try {
            List<Note> notes = noteDAO.findByEtudiant(etudiantId);
            double moyenne = calculateMoyenne(notes);
            moyenneLabel.setText(String.format("%.2f", moyenne));

            int pourcentagePresence = presenceDAO.calculatePresencePercentage(etudiantId);
            absencesLabel.setText(pourcentagePresence + "%");

            // For now, Demandes de Stage uses count of RDVs as a placeholder unless there's a DemandeStageDAO
            int rdvCount = rdvDAO.countByEtudiant(etudiantId);
            stagesLabel.setText(String.valueOf(rdvCount));

            modulesLabel.setText(String.valueOf(moduleDAO.count()));

        } catch (SQLException e) {
            e.printStackTrace();
            moyenneLabel.setText("--");
            absencesLabel.setText("--");
            stagesLabel.setText("0");
            modulesLabel.setText("0");
        }
    }

    private double calculateMoyenne(List<Note> notes) {
        if (notes.isEmpty()) return 0;
        double total = 0;
        double coefTotal = 0;
        for (Note n : notes) {
            total += n.getValeur() * n.getCoefficient();
            coefTotal += n.getCoefficient();
        }
        return coefTotal > 0 ? total / coefTotal : 0;
    }

    @FXML private void goModules() { navigate("/fxml/student/modules.fxml", "Mes Modules"); }
    @FXML private void goNotes() { navigate("/fxml/student/notes.fxml", "Mes Notes"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml", "Mes Présences"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml", "Emploi du temps"); }
    @FXML private void goRdv() { navigate("/fxml/student/rdv.fxml", "Mes RDV"); }
    @FXML private void goStages() { navigate("/fxml/student/stages.fxml", "Stages"); }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) modulesLabel.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, title);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) modulesLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}