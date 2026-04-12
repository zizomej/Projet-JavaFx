package com.learnhub.controller.visiteur;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.CoursDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class HomeController {

    @FXML private Label statEtudiants;
    @FXML private Label statModules;
    @FXML private Label statProfesseurs;
    @FXML private Label statFilieres;
    @FXML private ListView<String> eventsList;

    @FXML
    public void initialize() {
        try {
            UtilisateurDAO dao = new UtilisateurDAO();
            statEtudiants.setText(dao.countByRole("ROLE_ETUDIANT") + "+");
            statModules.setText(String.valueOf(new CoursDAO().count()));
            statProfesseurs.setText(dao.countByRole("ROLE_PROFESSEUR") + "+");
            statFilieres.setText(String.valueOf(new FiliereDAO().findAll().size()));

            List<Evenement> events = new EvenementDAO().findUpcoming();
            for (Evenement e : events) {
                eventsList.getItems().add("🎉  " + e.getTitre() + "  —  " + e.getDate() + "  |  " + e.getLieu());
            }
            if (events.isEmpty()) eventsList.getItems().add("Aucun événement à venir");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goHome()     { /* already here */ }
    @FXML private void goPrograms() { navigate("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goEvents()   { navigate("/fxml/visiteur/events.fxml", "Événements"); }
    @FXML private void goPartners() { navigate("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goLogin()    { navigate("/fxml/auth/login.fxml", "Connexion"); }
    @FXML private void goRegister() { navigate("/fxml/auth/register.fxml", "Inscription"); }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) eventsList.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
