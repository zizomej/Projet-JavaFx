package com.learnhub.controller.admin;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.Filiere;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FilieresAdminController {

    @FXML private TableView<Filiere> table;
    @FXML private TableColumn<Filiere, Integer> colId;
    @FXML private TableColumn<Filiere, String>  colCode;
    @FXML private TableColumn<Filiere, String>  colNom;
    @FXML private TableColumn<Filiere, String>  colNiveau;
    @FXML private TableColumn<Filiere, Integer> colDuree;
    @FXML private TableColumn<Filiere, Integer> colCapacite;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final FiliereDAO dao = new FiliereDAO();
    private final ObservableList<Filiere> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeAnnees"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capaciteMax"));
        table.setItems(data);
        loadData();
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, v) -> filter(v));
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            if (statusLabel != null) statusLabel.setText(data.size() + " filière(s)");
        } catch (SQLException e) {
            if (statusLabel != null) statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void filter(String s) {
        try {
            List<Filiere> all = dao.findAll();
            if (s != null && !s.isBlank()) {
                String q = s.toLowerCase();
                all = all.stream().filter(f ->
                    (f.getNom() != null && f.getNom().toLowerCase().contains(q)) ||
                    (f.getCode() != null && f.getCode().toLowerCase().contains(q))
                ).toList();
            }
            data.setAll(all);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleAdd()     { showForm(null); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML private void handleEdit() {
        Filiere sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez une filière."); return; }
        showForm(sel);
    }

    @FXML private void handleDelete() {
        Filiere sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez une filière."); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer \"" + sel.getNom() + "\" ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try { dao.delete(sel.getId()); loadData(); }
            catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        }
    }

    private void showForm(Filiere f) {
        boolean edit = f != null;
        Dialog<Filiere> dlg = new Dialog<>();
        dlg.setTitle(edit ? "Modifier Filière" : "Nouvelle Filière");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(24));

        TextField fCode     = new TextField(edit && f.getCode()   != null ? f.getCode()   : "");
        TextField fNom      = new TextField(edit && f.getNom()    != null ? f.getNom()    : "");
        TextField fNiveau   = new TextField(edit && f.getNiveau() != null ? f.getNiveau() : "");
        TextField fDuree    = new TextField(edit ? String.valueOf(f.getDureeAnnees()) : "3");
        TextField fCapacite = new TextField(edit ? String.valueOf(f.getCapaciteMax()) : "30");

        fNom.setMinWidth(260);
        fCode.setPromptText("ex: INFO-L3");
        fNiveau.setPromptText("ex: Licence");

        g.addRow(0, new Label("Code *"), fCode);
        g.addRow(1, new Label("Nom *"), fNom);
        g.addRow(2, new Label("Niveau"), fNiveau);
        g.addRow(3, new Label("Durée (ans)"), fDuree);
        g.addRow(4, new Label("Capacité max"), fCapacite);

        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Filiere nf = edit ? f : new Filiere();
            nf.setCode(fCode.getText().trim());
            nf.setNom(fNom.getText().trim());
            nf.setNiveau(fNiveau.getText().trim());
            try { nf.setDureeAnnees(Integer.parseInt(fDuree.getText().trim())); } catch (NumberFormatException ignored) {}
            try { nf.setCapaciteMax(Integer.parseInt(fCapacite.getText().trim())); } catch (NumberFormatException ignored) {}
            return nf;
        });

        dlg.showAndWait().ifPresent(nf -> {
            if (nf.getNom() == null || nf.getNom().isBlank()) { show("Le nom est obligatoire."); return; }
            try {
                if (edit) dao.update(nf); else dao.insert(nf);
                loadData();
            } catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        });
    }

    private void show(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    @FXML private void goBack()          { nav("/fxml/admin/dashboard.fxml",      "Tableau de bord"); }
    @FXML private void goDashboard()     { nav("/fxml/admin/dashboard.fxml",      "Tableau de bord"); }
    @FXML private void goUtilisateurs()  { nav("/fxml/admin/utilisateurs.fxml",   "Utilisateurs"); }
    @FXML private void goModules()       { nav("/fxml/admin/modules.fxml",        "Modules"); }
    @FXML private void goSeances()       { nav("/fxml/admin/seances.fxml",        "Séances"); }
    @FXML private void goNotes()         { nav("/fxml/admin/notes.fxml",          "Notes"); }
    @FXML private void goPresences()     { nav("/fxml/admin/presences.fxml",      "Présences"); }
    @FXML private void goFilieres()      { loadData(); }
    @FXML private void goEvenements()    { nav("/fxml/admin/evenements.fxml",     "Événements"); }
    @FXML private void goRdv()           { nav("/fxml/admin/rdv.fxml",            "RDV Médicaux"); }
    @FXML private void goCreneaux()      { nav("/fxml/admin/creneaux.fxml",       "Créneaux"); }
    @FXML private void goPartenaires()   { nav("/fxml/admin/partenaires/partenaires.fxml",    "Partenaires"); }
    @FXML private void goOffresStage()   { nav("/fxml/admin/offrestage/offres_stage.fxml",   "Offres de Stage"); }
    @FXML private void goDemandesStage() { nav("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage"); }
    @FXML private void goMailing()       { nav("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }
}
