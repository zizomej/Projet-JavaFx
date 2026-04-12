package com.learnhub.controller.student;

import com.learnhub.dao.NoteDAO;
import com.learnhub.models.Note;
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
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class StudentNotesController {

    @FXML private TextField searchField;
    @FXML private TableView<Note> table;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Label moyenneLabel;

    @FXML private TableColumn<Note, String>  colModule;
    @FXML private TableColumn<Note, Double>  colNote;
    @FXML private TableColumn<Note, String>  colType;
    @FXML private TableColumn<Note, String>  colMention;
    @FXML private TableColumn<Note, String>  colDate;

    private ObservableList<Note> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        if (userLabel != null) userLabel.setText(user.getNomComplet());

        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMention.setCellValueFactory(new PropertyValueFactory<>("mention"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));

        loadData(user.getId());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
    }

    private void loadData(int userId) {
        try {
            NoteDAO dao = new NoteDAO();
            List<Note> list = dao.findByEtudiant(userId);
            allData = FXCollections.observableArrayList(list);
            table.setItems(allData);

            double moyenne = dao.getMoyenneEtudiant(userId);
            if (moyenneLabel != null) {
                moyenneLabel.setText(String.format("Moyenne: %.2f / 20", moyenne));
            }
            if (statusLabel != null) {
                statusLabel.setText(list.size() + " note(s) trouvée(s)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Erreur lors du chargement des notes");
        }
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            table.setItems(allData);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<Note> filtered = allData.filtered(n ->
            (n.getModuleTitre() != null && n.getModuleTitre().toLowerCase().contains(lower)) ||
            (n.getType()        != null && n.getType().toLowerCase().contains(lower))
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
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
