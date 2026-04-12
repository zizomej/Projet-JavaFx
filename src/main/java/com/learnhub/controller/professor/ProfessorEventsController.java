package com.learnhub.controller.professor;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
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

public class ProfessorEventsController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<Evenement> table;
    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String>  colTitre;
    @FXML private TableColumn<Evenement, String>  colType;
    @FXML private TableColumn<Evenement, String>  colDebut;
    @FXML private TableColumn<Evenement, String>  colLieu;
    @FXML private TableColumn<Evenement, String>  colStatut;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        welcomeLabel.setText(user.getNomComplet());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieuNom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        loadData();
    }

    private void loadData() {
        try {
            List<Evenement> list = new EvenementDAO().findAll();
            ObservableList<Evenement> data = FXCollections.observableArrayList(list);
            table.setItems(data);
            statusLabel.setText(list.size() + " événement(s)");
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
