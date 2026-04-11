package com.learnhub.controller.visiteur;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;

public class HomeController {

    @FXML private Text statEtudiants;
    @FXML private Text statProfesseurs;
    @FXML private Text statModules;
    @FXML private Text statPartenaires;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final PartenaireDAO partenaireDAO = new PartenaireDAO();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            int totalEtudiants = utilisateurDAO.countByRole("etudiant");
            int totalProfesseurs = utilisateurDAO.countByRole("professeur");
            int totalModules = moduleDAO.count();
            int totalPartenaires = partenaireDAO.count();

            if (statEtudiants != null) statEtudiants.setText(String.valueOf(totalEtudiants));
            if (statProfesseurs != null) statProfesseurs.setText(String.valueOf(totalProfesseurs));
            if (statModules != null) statModules.setText(String.valueOf(totalModules));
            if (statPartenaires != null) statPartenaires.setText(String.valueOf(totalPartenaires));

        } catch (SQLException e) {
            e.printStackTrace();
            if (statEtudiants != null) statEtudiants.setText("--");
            if (statProfesseurs != null) statProfesseurs.setText("--");
            if (statModules != null) statModules.setText("--");
            if (statPartenaires != null) statPartenaires.setText("--");
        }
    }

    @FXML private void goHome() { navigateTo("/fxml/visiteur/home.fxml", "Accueil"); }
    @FXML private void goPrograms() { navigateTo("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goEvents() { navigateTo("/fxml/visiteur/events.fxml", "Événements"); }
    @FXML private void goPartners() { navigateTo("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goLogin() { navigateTo("/fxml/auth/login.fxml", "Connexion"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) javafx.stage.Window.getWindows().get(0).getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxmlPath, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
