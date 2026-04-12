package com.learnhub.controller.professor;

import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Seance;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ProfessorSeancesController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<Seance> table;
    @FXML private TableColumn<Seance, Integer> colId;
    @FXML private TableColumn<Seance, String>  colModule;
    @FXML private TableColumn<Seance, String>  colDate;
    @FXML private TableColumn<Seance, String>  colDebut;
    @FXML private TableColumn<Seance, String>  colFin;
    @FXML private TableColumn<Seance, String>  colSalle;
    @FXML private TableColumn<Seance, String>  colType;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        welcomeLabel.setText(user.getNomComplet());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        loadData(user.getId());
    }

    private void loadData(int profId) {
        try {
            List<Seance> list = new SeanceDAO().findByProfesseur(profId);
            ObservableList<Seance> data = FXCollections.observableArrayList(list);
            table.setItems(data);
            statusLabel.setText(list.size() + " séance(s)");
        } catch (SQLException e) {
            statusLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    @FXML private void goDashboard()   { navigate("/fxml/professor/dashboard.fxml",  "Espace Professeur"); }
    @FXML private void goModules()     { navigate("/fxml/professor/modules.fxml",    "Modules"); }
    @FXML private void goSeances()     { navigate("/fxml/professor/seances.fxml",    "Séances"); }
    @FXML private void goNotes()       { navigate("/fxml/professor/notes.fxml",      "Notes"); }
    @FXML private void goPresences()   { navigate("/fxml/professor/presences.fxml",  "Présences"); }
    @FXML private void goEvenements()  { navigate("/fxml/professor/evenements.fxml", "Événements"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
