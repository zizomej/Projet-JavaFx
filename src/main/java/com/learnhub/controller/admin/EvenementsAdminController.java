package com.learnhub.controller.admin;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
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

public class EvenementsAdminController {

    @FXML private TableView<Evenement> table;
    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String>  colTitre;
    @FXML private TableColumn<Evenement, String>  colType;
    @FXML private TableColumn<Evenement, String>  colDebut;
    @FXML private TableColumn<Evenement, String>  colFin;
    @FXML private TableColumn<Evenement, String>  colLieu;
    @FXML private TableColumn<Evenement, String>  colStatut;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final EvenementDAO dao = new EvenementDAO();
    private final ObservableList<Evenement> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieuNom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        table.setItems(data);
        loadData();
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, v) -> filter(v));
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            if (statusLabel != null) statusLabel.setText(data.size() + " événement(s)");
        } catch (SQLException e) {
            if (statusLabel != null) statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void filter(String s) {
        try {
            List<Evenement> all = dao.findAll();
            if (s != null && !s.isBlank()) {
                String q = s.toLowerCase();
                all = all.stream().filter(e ->
                    (e.getTitre() != null && e.getTitre().toLowerCase().contains(q)) ||
                    (e.getTypeEvenement() != null && e.getTypeEvenement().toLowerCase().contains(q))
                ).toList();
            }
            data.setAll(all);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleAdd()     { showForm(null); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML private void handleEdit() {
        Evenement sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez un événement."); return; }
        showForm(sel);
    }

    @FXML private void handleDelete() {
        Evenement sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { show("Sélectionnez un événement."); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer \"" + sel.getTitre() + "\" ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            try { dao.delete(sel.getId()); loadData(); }
            catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        }
    }

    private void showForm(Evenement ev) {
        boolean edit = ev != null;
        Dialog<Evenement> dlg = new Dialog<>();
        dlg.setTitle(edit ? "Modifier Événement" : "Nouvel Événement");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(24));

        TextField fTitre = new TextField(edit && ev.getTitre() != null ? ev.getTitre() : "");
        TextArea  fDesc  = new TextArea(edit && ev.getDescription() != null ? ev.getDescription() : "");
        fDesc.setPrefRowCount(3);
        ComboBox<String> cType = new ComboBox<>();
        cType.getItems().addAll("Conférence", "Workshop", "Cérémonie", "Sport", "Culturel", "Autre");
        cType.setValue(edit && ev.getTypeEvenement() != null ? ev.getTypeEvenement() : "Conférence");
        TextField fDebut = new TextField(edit && ev.getDateDebut() != null ? ev.getDateDebut() : "");
        TextField fFin   = new TextField(edit && ev.getDateFin()   != null ? ev.getDateFin()   : "");
        TextField fHDeb  = new TextField(edit && ev.getHeureDebut() != null ? ev.getHeureDebut() : "");
        TextField fHFin  = new TextField(edit && ev.getHeureFin()   != null ? ev.getHeureFin()   : "");
        ComboBox<String> cStatut = new ComboBox<>();
        cStatut.getItems().addAll("PLANIFIE", "EN_COURS", "TERMINE", "ANNULE");
        cStatut.setValue(edit && ev.getStatut() != null ? ev.getStatut() : "PLANIFIE");

        fDebut.setPromptText("YYYY-MM-DD"); fFin.setPromptText("YYYY-MM-DD");
        fHDeb.setPromptText("HH:mm"); fHFin.setPromptText("HH:mm");
        fTitre.setMinWidth(240);

        g.addRow(0, new Label("Titre *"), fTitre);
        g.addRow(1, new Label("Description"), fDesc);
        g.addRow(2, new Label("Type"), cType);
        g.addRow(3, new Label("Date début *"), fDebut);
        g.addRow(4, new Label("Date fin"), fFin);
        g.addRow(5, new Label("Heure début"), fHDeb);
        g.addRow(6, new Label("Heure fin"), fHFin);
        g.addRow(7, new Label("Statut"), cStatut);

        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Evenement ne = edit ? ev : new Evenement();
            ne.setTitre(fTitre.getText().trim());
            ne.setDescription(fDesc.getText().trim());
            ne.setTypeEvenement(cType.getValue());
            ne.setDateDebut(fDebut.getText().trim());
            ne.setDateFin(fFin.getText().trim());
            ne.setHeureDebut(fHDeb.getText().trim());
            ne.setHeureFin(fHFin.getText().trim());
            ne.setStatut(cStatut.getValue());
            return ne;
        });

        dlg.showAndWait().ifPresent(ne -> {
            if (ne.getTitre() == null || ne.getTitre().isBlank()) { show("Le titre est obligatoire."); return; }
            try {
                if (edit) dao.update(ne); else dao.insert(ne);
                loadData();
            } catch (SQLException e) { show("Erreur: " + e.getMessage()); }
        });
    }

    private void show(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    @FXML private void goBack()           { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goDashboard()      { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs()   { nav("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules()        { nav("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances()        { nav("/fxml/admin/seances.fxml", "Séances"); }
    @FXML private void goNotes()          { nav("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences()      { nav("/fxml/admin/presences.fxml", "Présences"); }
    @FXML private void goFilieres()       { nav("/fxml/admin/filieres.fxml", "Filières"); }
    @FXML private void goEvenements()     { loadData(); }
    @FXML private void goRdv()            { nav("/fxml/admin/rdv.fxml", "RDV Médicaux"); }
    @FXML private void goCreneaux()       { nav("/fxml/admin/creneaux.fxml", "Créneaux"); }
    @FXML private void goPartenaires()    { nav("/fxml/admin/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage()    { nav("/fxml/admin/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage()  { nav("/fxml/admin/demandes_stage.fxml", "Demandes de Stage"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }
}
