package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Seance;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProfessorPresencesController {

    @FXML private ComboBox<Module> moduleSelector;
    @FXML private ComboBox<Seance> seanceSelector;
    @FXML private Label welcomeLabel;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText(user.getNomComplet());
            loadModules(user.getId());
        }

        moduleSelector.setOnAction(e -> {
            Module selected = moduleSelector.getValue();
            if (selected != null) {
                loadSeances(selected.getId());
            }
        });
    }

    private void loadModules(int profId) {
        try {
            moduleSelector.getItems().setAll(moduleDAO.findByProfesseur(profId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSeances(int moduleId) {
        try {
            List<Seance> seances = seanceDAO.findByModule(moduleId);
            seanceSelector.getItems().setAll(seances);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadPresences() {
        Seance selected = seanceSelector.getValue();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner une séance.");
            return;
        }
        openPresenceForm(selected);
    }

    private void openPresenceForm(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professor/presence_form.fxml"));
            Parent root = loader.load();
            PresenceFormController controller = loader.getController();
            controller.setSeance(seance);

            Stage stage = (Stage) moduleSelector.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Mes Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Mes Séances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Gestion des Notes"); }
    @FXML private void goPresences() { navigate("/fxml/professor/presences.fxml", "Présences"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) moduleSelector.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) moduleSelector.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
