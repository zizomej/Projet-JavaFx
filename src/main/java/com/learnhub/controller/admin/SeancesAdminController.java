package com.learnhub.controller.admin;

import com.learnhub.dao.CoursDAO;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Cours;
import com.learnhub.models.Seance;
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

public class SeancesAdminController {

    @FXML private TableView<Seance> table;
    @FXML private TableColumn<Seance, Integer> colId;
    @FXML private TableColumn<Seance, String>  colModule;
    @FXML private TableColumn<Seance, String>  colDate;
    @FXML private TableColumn<Seance, String>  colDebut;
    @FXML private TableColumn<Seance, String>  colFin;
    @FXML private TableColumn<Seance, String>  colSalle;
    @FXML private TableColumn<Seance, String>  colType;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final SeanceDAO dao = new SeanceDAO();
    private final ObservableList<Seance> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        table.setItems(data);
        loadData();
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, v) -> filter(v));
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            if (statusLabel != null) statusLabel.setText(data.size() + " séance(s)");
        } catch (SQLException e) {
            if (statusLabel != null) statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void filter(String s) {
        try {
            List<Seance> all = dao.findAll();
            if (s != null && !s.isBlank()) {
                String q = s.toLowerCase();
                all = all.stream().filter(se ->
                    (se.getModuleTitre() != null && se.getModuleTitre().toLowerCase().contains(q)) ||
                    (se.getSalle() != null && se.getSalle().toLowerCase().contains(q))
                ).toList();
            }
            data.setAll(all);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleAdd()     { showForm(null); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML private void handleEdit() {
        Seance sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez une séance."); return; }
        showForm(sel);
    }

    @FXML private void handleDelete() {
        Seance sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez une séance."); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer cette séance ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try { dao.delete(sel.getId()); loadData(); }
            catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        }
    }

    private void showForm(Seance s) {
        boolean edit = s != null;
        Dialog<Seance> dlg = new Dialog<>();
        dlg.setTitle(edit ? "Modifier Séance" : "Nouvelle Séance");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(24));

        ComboBox<Cours> cModule = new ComboBox<>();
        try { cModule.getItems().addAll(new CoursDAO().findAll()); } catch (Exception ignored) {}
        if (edit && s.getModuleId() > 0)
            cModule.getItems().stream().filter(c -> c.getId() == s.getModuleId()).findFirst().ifPresent(cModule::setValue);

        TextField fDate  = new TextField(edit ? (s.getDate() != null ? s.getDate() : "") : "");
        TextField fDebut = new TextField(edit ? (s.getHeureDebut() != null ? s.getHeureDebut() : "") : "");
        TextField fFin   = new TextField(edit ? (s.getHeureFin() != null ? s.getHeureFin() : "") : "");
        TextField fSalle = new TextField(edit ? (s.getSalle() != null ? s.getSalle() : "") : "");
        ComboBox<String> cType = new ComboBox<>();
        cType.getItems().addAll("CM", "TD", "TP");
        cType.setValue(edit && s.getType() != null ? s.getType() : "CM");

        fDate.setPromptText("YYYY-MM-DD");
        fDebut.setPromptText("HH:mm");
        fFin.setPromptText("HH:mm");
        cModule.setMinWidth(200);

        g.addRow(0, new Label("Module *"), cModule);
        g.addRow(1, new Label("Date *"), fDate);
        g.addRow(2, new Label("Heure début *"), fDebut);
        g.addRow(3, new Label("Heure fin"), fFin);
        g.addRow(4, new Label("Salle"), fSalle);
        g.addRow(5, new Label("Type"), cType);

        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Seance ns = edit ? s : new Seance();
            if (cModule.getValue() != null) ns.setModuleId(cModule.getValue().getId());
            ns.setDate(fDate.getText().trim());
            ns.setHeureDebut(fDebut.getText().trim());
            ns.setHeureFin(fFin.getText().trim());
            ns.setSalle(fSalle.getText().trim());
            ns.setType(cType.getValue());
            return ns;
        });

        dlg.showAndWait().ifPresent(ns -> {
            try {
                if (edit) dao.update(ns); else dao.insert(ns);
                loadData();
            } catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        });
    }

    private void show(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    @FXML private void goBack()           { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goDashboard()      { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs()   { nav("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules()        { nav("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances()        { loadData(); }
    @FXML private void goNotes()          { nav("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences()      { nav("/fxml/admin/presences.fxml", "Présences"); }
    @FXML private void goFilieres()       { nav("/fxml/admin/filieres.fxml", "Filières"); }
    @FXML private void goEvenements()     { nav("/fxml/admin/evenements.fxml", "Événements"); }
    @FXML private void goRdv()            { nav("/fxml/admin/rdv.fxml", "RDV Médicaux"); }
    @FXML private void goCreneaux()       { nav("/fxml/admin/creneaux.fxml", "Créneaux"); }
    @FXML private void goPartenaires()    { nav("/fxml/admin/partenaires/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage()    { nav("/fxml/admin/offrestage/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage()  { nav("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage"); }
    @FXML private void goMailing()       { nav("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }
}
