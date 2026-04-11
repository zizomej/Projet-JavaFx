package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ProfessorDashboardController {

    @FXML private Label welcomeTitleLabel;
    @FXML private Label navProfName;
    @FXML private Label modulesLabel;
    @FXML private Label studentsLabel;
    @FXML private Label notesLabel;
    @FXML private Label presencesLabel;
    @FXML private VBox activitiesList;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final NoteDAO noteDAO = new NoteDAO();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        
        welcomeTitleLabel.setText("Bonjour, " + user.getNomComplet());
        navProfName.setText(user.getNomComplet());

        loadStats(user.getId());
        loadActivities();
    }

    private void loadStats(int profId) {
        try {
            List<Module> modules = moduleDAO.findByProfesseur(profId);
            modulesLabel.setText(String.valueOf(modules.size()));
            
            // Dummy for students & presences since we don't have those DAOs linked strictly yet
            studentsLabel.setText("3"); 
            presencesLabel.setText("4");

            int nbNotes = noteDAO.findByProfesseur(profId).size();
            notesLabel.setText(String.valueOf(nbNotes));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadActivities() {
        activitiesList.getChildren().clear();
        Label l1 = new Label("• Ressource \"Cours Intro\" (PDF) ajoutée");
        Label l2 = new Label("• Note de Ahmed Zneidi modifiée (14.50)");
        l1.setStyle("-fx-text-fill: #374151;");
        l2.setStyle("-fx-text-fill: #374151;");
        activitiesList.getChildren().addAll(l1, l2);
    }

    // Navigation
    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Mes Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Mes Séances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Gestion des Notes"); }
    @FXML private void goPresences() { navigate("/fxml/professor/presences.fxml", "Présences"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeTitleLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) welcomeTitleLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}