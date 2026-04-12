package com.learnhub.controller.student;

import com.learnhub.dao.RdvDAO;
import com.learnhub.models.Rdv;
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

public class StudentRdvController {

    /** Root node injected so navigation always has a scene reference,
     *  even when the page body contains no table. */
    @FXML private BorderPane root;

    // Optional fields present on richer layouts – null-safe throughout
    @FXML private TextField  searchField;
    @FXML private TableView<Rdv> table;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;

    @FXML private TableColumn<Rdv, String>  colMotif;
    @FXML private TableColumn<Rdv, String>  colDate;
    @FXML private TableColumn<Rdv, String>  colStatut;
    @FXML private TableColumn<Rdv, String>  colDescription;

    private ObservableList<Rdv> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        if (userLabel != null) userLabel.setText(user.getNomComplet());

        if (table != null) {
            if (colMotif       != null) colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
            if (colDate        != null) colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
            if (colStatut      != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

            loadData(user.getId());

            if (searchField != null) {
                searchField.textProperty().addListener((obs, ov, nv) -> filterTable(nv));
            }
        }
    }

    private void loadData(int userId) {
        try {
            List<Rdv> list = new RdvDAO().findByPatient(userId);
            allData = FXCollections.observableArrayList(list);
            table.setItems(allData);
            if (statusLabel != null) statusLabel.setText(list.size() + " RDV trouvé(s)");
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
        ObservableList<Rdv> filtered = allData.filtered(r ->
            (r.getMotif()  != null && r.getMotif().toLowerCase().contains(lower)) ||
            (r.getStatut() != null && r.getStatut().toLowerCase().contains(lower))
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
