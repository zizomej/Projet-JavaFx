package com.learnhub.controller.professor;

import com.learnhub.dao.CoursDAO;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Cours;
import com.learnhub.models.Seance;
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
import java.util.List;

public class ProfessorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label modulesLabel;
    @FXML private Label seancesLabel;
    @FXML private TableView<Cours>  modulesTable;
    @FXML private TableColumn<Cours, String>  colTitre;
    @FXML private TableColumn<Cours, String>  colNiveau;
    @FXML private TableView<Seance> seancesTable;
    @FXML private TableColumn<Seance, String> colSeanceModule;
    @FXML private TableColumn<Seance, String> colSeanceDate;
    @FXML private TableColumn<Seance, String> colSeanceSalle;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        welcomeLabel.setText("Bienvenue, " + user.getNomComplet());

        if (modulesTable != null) {
            colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
            colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        }
        if (seancesTable != null) {
            colSeanceModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
            colSeanceDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            colSeanceSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        }
        loadData(user.getId());
    }

    private void loadData(int profId) {
        try {
            List<Cours> modules = new CoursDAO().findByProfesseur(profId);
            modulesLabel.setText(String.valueOf(modules.size()));
            if (modulesTable != null) {
                modulesTable.setItems(FXCollections.observableArrayList(modules));
            }

            List<Seance> seances = new SeanceDAO().findByProfesseur(profId);
            seancesLabel.setText(String.valueOf(seances.size()));
            if (seancesTable != null) {
                seancesTable.setItems(FXCollections.observableArrayList(seances));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goModules()    { navigate("/fxml/professor/modules.fxml",   "Modules"); }
    @FXML private void goSeances()    { navigate("/fxml/professor/seances.fxml",   "Séances"); }
    @FXML private void goNotes()      { navigate("/fxml/professor/notes.fxml",     "Notes"); }
    @FXML private void goPresences()  { navigate("/fxml/professor/presences.fxml", "Présences"); }
    @FXML private void goEvenements() { navigate("/fxml/professor/evenements.fxml","Événements"); }

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

    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Espace Professeur"); }
}
