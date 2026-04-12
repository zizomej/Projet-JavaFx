package com.learnhub.controller.admin;

import com.learnhub.dao.CreneauDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Creneau;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
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

public class CreneauxAdminController {

    @FXML private TableView<Creneau> table;
    @FXML private TableColumn<Creneau, Integer> colId;
    @FXML private TableColumn<Creneau, String>  colDate;
    @FXML private TableColumn<Creneau, String>  colDebut;
    @FXML private TableColumn<Creneau, String>  colFin;
    @FXML private TableColumn<Creneau, String>  colMedecin;
    @FXML private TableColumn<Creneau, String>  colDisponible;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final CreneauDAO dao = new CreneauDAO();
    private final ObservableList<Creneau> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinNom"));
        colDisponible.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isDisponible() ? "✅ Oui" : "❌ Non"));
        table.setItems(data);
        loadData();
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, v) -> filter(v));
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            if (statusLabel != null) statusLabel.setText(data.size() + " créneau(x)");
        } catch (SQLException e) {
            if (statusLabel != null) statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void filter(String s) {
        try {
            List<Creneau> all = dao.findAll();
            if (s != null && !s.isBlank()) {
                String q = s.toLowerCase();
                all = all.stream().filter(c ->
                    (c.getMedecinNom() != null && c.getMedecinNom().toLowerCase().contains(q)) ||
                    (c.getDate() != null && c.getDate().contains(q))
                ).toList();
            }
            data.setAll(all);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleAdd()     { showForm(null); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML
    private void handleEdit() {
        Creneau sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez un créneau."); return; }
        showForm(sel);
    }

    @FXML
    private void handleDelete() {
        Creneau sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez un créneau."); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer ce créneau ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try { dao.delete(sel.getId()); loadData(); }
            catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        }
    }

    private void showForm(Creneau c) {
        boolean edit = c != null;
        Dialog<Creneau> dlg = new Dialog<>();
        dlg.setTitle(edit ? "Modifier Créneau" : "Nouveau Créneau");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(24));

        ComboBox<Utilisateur> medBox = new ComboBox<>();
        try { medBox.getItems().addAll(new UtilisateurDAO().findByRole("ROLE_MEDECIN")); } catch (Exception ignored) {}
        if (edit && c.getMedecinId() > 0)
            medBox.getItems().stream().filter(u -> u.getId() == c.getMedecinId()).findFirst().ifPresent(medBox::setValue);

        TextField fDate  = new TextField(edit ? safe(c.getDate()) : "");
        TextField fDebut = new TextField(edit ? safe(c.getHeureDebut()) : "");
        TextField fFin   = new TextField(edit ? safe(c.getHeureFin()) : "");
        CheckBox  chkDispo = new CheckBox("Disponible");
        chkDispo.setSelected(edit ? c.isDisponible() : true);

        fDate.setPromptText("YYYY-MM-DD");
        fDebut.setPromptText("HH:mm");
        fFin.setPromptText("HH:mm");
        medBox.setMinWidth(200);

        g.addRow(0, new Label("Médecin *"), medBox);
        g.addRow(1, new Label("Date *"),    fDate);
        g.addRow(2, new Label("Début *"),   fDebut);
        g.addRow(3, new Label("Fin"),       fFin);
        g.addRow(4, new Label(""),          chkDispo);

        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Creneau nc = edit ? c : new Creneau();
            if (medBox.getValue() != null) nc.setMedecinId(medBox.getValue().getId());
            nc.setDate(fDate.getText().trim());
            nc.setHeureDebut(fDebut.getText().trim());
            nc.setHeureFin(fFin.getText().trim());
            nc.setDisponible(chkDispo.isSelected());
            return nc;
        });

        dlg.showAndWait().ifPresent(nc -> {
            try {
                if (edit) dao.update(nc); else dao.insert(nc);
                loadData();
            } catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        });
    }

    private String safe(String s) { return s != null ? s : ""; }
    private void show(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    @FXML private void goDashboard()     { nav("/fxml/admin/dashboard.fxml",      "Tableau de bord"); }
    @FXML private void goUtilisateurs()  { nav("/fxml/admin/utilisateurs.fxml",   "Utilisateurs"); }
    @FXML private void goModules()       { nav("/fxml/admin/modules.fxml",        "Modules"); }
    @FXML private void goSeances()       { nav("/fxml/admin/seances.fxml",        "Séances"); }
    @FXML private void goNotes()         { nav("/fxml/admin/notes.fxml",          "Notes"); }
    @FXML private void goPresences()     { nav("/fxml/admin/presences.fxml",      "Présences"); }
    @FXML private void goFilieres()      { nav("/fxml/admin/filieres.fxml",       "Filières"); }
    @FXML private void goEvenements()    { nav("/fxml/admin/evenements.fxml",     "Événements"); }
    @FXML private void goRdv()           { nav("/fxml/admin/rdv.fxml",            "RDV Médicaux"); }
    @FXML private void goCreneaux()      { loadData(); }
    @FXML private void goPartenaires()   { nav("/fxml/admin/partenaires.fxml",    "Partenaires"); }
    @FXML private void goOffresStage()   { nav("/fxml/admin/offres_stage.fxml",   "Offres de Stage"); }
    @FXML private void goDemandesStage() { nav("/fxml/admin/demandes_stage.fxml", "Demandes de Stage"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }
}
