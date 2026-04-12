package com.learnhub.controller.student;

import com.learnhub.dao.CoursDAO;
import com.learnhub.models.Cours;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class StudentEmploiController {

    /** Root node – always present, used as fallback scene anchor. */
    @FXML private BorderPane root;

    @FXML private TextField searchField;
    @FXML private TableView<Cours> table;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;

    @FXML private TableColumn<Cours, String>  colCode;
    @FXML private TableColumn<Cours, String>  colIntitule;
    @FXML private TableColumn<Cours, Integer> colSemestre;
    @FXML private TableColumn<Cours, Integer> colCredits;
    @FXML private TableColumn<Cours, String>  colResponsable;

    private ObservableList<Cours> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null && userLabel != null) {
            userLabel.setText(user.getNomComplet());
        }

        if (table != null) {
            if (colCode        != null) colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
            if (colIntitule    != null) colIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));
            if (colSemestre    != null) colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
            if (colCredits     != null) colCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
            if (colResponsable != null) colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsableNom"));

            loadData();

            if (searchField != null) {
                searchField.textProperty().addListener((obs, ov, nv) -> filterTable(nv));
            }
        }
    }

    private void loadData() {
        try {
            List<Cours> list = new CoursDAO().findAll();
            allData = FXCollections.observableArrayList(list);
            table.setItems(allData);
            if (statusLabel != null) statusLabel.setText(list.size() + " module(s) chargé(s)");
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Erreur lors du chargement");
        }
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            table.setItems(allData);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<Cours> filtered = allData.filtered(c ->
            (c.getCode()           != null && c.getCode().toLowerCase().contains(lower)) ||
            (c.getIntitule()       != null && c.getIntitule().toLowerCase().contains(lower)) ||
            (c.getResponsableNom() != null && c.getResponsableNom().toLowerCase().contains(lower))
        );
        table.setItems(filtered);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void goDashboard()     { navigate("/fxml/student/dashboard.fxml",     "Espace Étudiant"); }
    @FXML private void goModules()       { navigate("/fxml/student/modules.fxml",        "Mes Modules"); }
    @FXML private void goNotes()         { navigate("/fxml/student/notes.fxml",          "Mes Notes"); }
    @FXML private void goEmploi()        { navigate("/fxml/student/emploi.fxml",         "Emploi du temps"); }
    @FXML private void goPresences()     { navigate("/fxml/student/presences.fxml",      "Mes Présences"); }
    @FXML private void goRdv()           { navigate("/fxml/student/rdv.fxml",            "Mes RDV"); }
    @FXML private void goEvenements()    { navigate("/fxml/student/evenements.fxml",     "Événements"); }
    @FXML private void goOffresStage()   { navigate("/fxml/student/offres_stage.fxml",   "Offres de Stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/student/demandes_stage.fxml", "Mes Demandes de Stage"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateTo(getStage(), "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        NavigationUtil.navigateTo(getStage(), fxml, title);
    }

    private Stage getStage() {
        if (table != null && table.getScene() != null)
            return (Stage) table.getScene().getWindow();
        return (Stage) root.getScene().getWindow();
    }
}
