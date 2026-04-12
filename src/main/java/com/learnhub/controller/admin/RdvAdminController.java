package com.learnhub.controller.admin;

import com.learnhub.dao.RdvDAO;
import com.learnhub.models.Rdv;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class RdvAdminController {

    @FXML private TableView<Rdv> table;
    @FXML private TableColumn<Rdv, Integer> colId;
    @FXML private TableColumn<Rdv, String> colPatient;
    @FXML private TableColumn<Rdv, String> colMedecin;
    @FXML private TableColumn<Rdv, String> colDateHeure;
    @FXML private TableColumn<Rdv, String> colMotif;
    @FXML private TableColumn<Rdv, String> colStatut;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> statutFilter;

    private final RdvDAO dao = new RdvDAO();
    private final ObservableList<Rdv> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinNom"));
        colDateHeure.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Color rows by status
        table.setRowFactory(tv -> new TableRow<Rdv>() {
            @Override
            protected void updateItem(Rdv item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                switch (item.getStatut() != null ? item.getStatut() : "") {
                    case "CONFIRME"   -> setStyle("-fx-background-color: #d4edda;");
                    case "ANNULE"     -> setStyle("-fx-background-color: #f8d7da;");
                    case "EN_ATTENTE" -> setStyle("-fx-background-color: #fff3cd;");
                    default           -> setStyle("");
                }
            }
        });

        statutFilter.getItems().addAll("Tous", "EN_ATTENTE", "CONFIRME", "ANNULE", "TERMINE");
        statutFilter.setValue("Tous");
        statutFilter.valueProperty().addListener((obs, o, v) -> filterByStatut(v));

        table.setItems(data);
        loadData();
    }

    private void loadData() {
        try { data.setAll(dao.findAll()); statusLabel.setText(data.size() + " RDV"); }
        catch (SQLException e) { statusLabel.setText("Erreur: " + e.getMessage()); }
    }

    private void filterByStatut(String statut) {
        try {
            var all = dao.findAll();
            if (!"Tous".equals(statut)) all = all.stream().filter(r -> statut.equals(r.getStatut())).toList();
            data.setAll(all);
        } catch (SQLException e) { statusLabel.setText("Erreur: " + e.getMessage()); }
    }

    @FXML
    private void handleConfirmer() { updateStatut("CONFIRME"); }
    @FXML
    private void handleAnnuler() { updateStatut("ANNULE"); }
    @FXML
    private void handleTerminer() { updateStatut("TERMINE"); }

    private void updateStatut(String statut) {
        Rdv selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Sélectionnez un RDV."); return; }
        try {
            dao.updateStatut(selected.getId(), statut);
            loadData();
            statusLabel.setText("Statut mis à jour : " + statut);
        } catch (SQLException e) { statusLabel.setText("Erreur: " + e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        Rdv selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce RDV ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            try { dao.delete(selected.getId()); loadData(); }
            catch (SQLException e) { statusLabel.setText("Erreur: " + e.getMessage()); }
        }
    }

    @FXML private void handleRefresh() { loadData(); }
    @FXML private void goBack()           { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goDashboard()      { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs()   { nav("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules()        { nav("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances()        { nav("/fxml/admin/seances.fxml", "Séances"); }
    @FXML private void goNotes()          { nav("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences()      { nav("/fxml/admin/presences.fxml", "Présences"); }
    @FXML private void goFilieres()       { nav("/fxml/admin/filieres.fxml", "Filières"); }
    @FXML private void goEvenements()     { nav("/fxml/admin/evenements.fxml", "Événements"); }
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
