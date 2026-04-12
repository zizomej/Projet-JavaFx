package com.learnhub.controller.student;

import com.learnhub.dao.PresenceDAO;
import com.learnhub.models.Presence;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class StudentPresencesController {

    @FXML private TextField searchField;
    @FXML private TableView<Presence> table;
    @FXML private Label userLabel;
    @FXML private Label presentLabel;
    @FXML private Label absentLabel;

    @FXML private TableColumn<Presence, String> colSeance;
    @FXML private TableColumn<Presence, String> colStatut;
    @FXML private TableColumn<Presence, String> colDate;
    @FXML private TableColumn<Presence, String> colCommentaire;

    private ObservableList<Presence> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        if (userLabel != null) userLabel.setText(user.getNomComplet());

        colSeance.setCellValueFactory(new PropertyValueFactory<>("seanceInfo"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCommentaire.setCellValueFactory(cell -> new ReadOnlyStringWrapper(buildPresenceDetail(cell.getValue())));

        loadData(user.getId());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
    }

    private void loadData(int userId) {
        try {
            List<Presence> list = new PresenceDAO().findByEtudiant(userId);
            allData = FXCollections.observableArrayList(list);
            table.setItems(allData);

            long presents = list.stream().filter(p -> "PRESENT".equalsIgnoreCase(p.getStatut())).count();
            long absents  = list.stream().filter(p -> "ABSENT".equalsIgnoreCase(p.getStatut())).count();

            if (presentLabel != null) presentLabel.setText(String.valueOf(presents));
            if (absentLabel  != null) absentLabel.setText(String.valueOf(absents));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            table.setItems(allData);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<Presence> filtered = allData.filtered(p ->
            (p.getSeanceInfo() != null && p.getSeanceInfo().toLowerCase().contains(lower)) ||
            (p.getStatut()     != null && p.getStatut().toLowerCase().contains(lower))
        );
        table.setItems(filtered);
    }

    private String buildPresenceDetail(Presence presence) {
        if (presence == null || presence.getStatut() == null) {
            return "-";
        }
        return switch (presence.getStatut().toUpperCase()) {
            case "PRESENT" -> "Presence validee";
            case "ABSENT" -> "Absence a regulariser";
            case "RETARD" -> "Arrivee en retard";
            case "JUSTIFIE" -> "Absence justifiee";
            default -> presence.getStatut();
        };
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
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
