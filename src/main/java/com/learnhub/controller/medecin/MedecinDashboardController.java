package com.learnhub.controller.medecin;

import com.learnhub.dao.CreneauDAO;
import com.learnhub.dao.RdvDAO;
import com.learnhub.models.Creneau;
import com.learnhub.models.Rdv;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MedecinDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label rdvTotalLabel;
    @FXML private Label rdvAttenteLabel;
    @FXML private Label creneauxLabel;
    @FXML private TableView<Rdv> rdvTable;
    @FXML private TableColumn<Rdv, String> colPatient;
    @FXML private TableColumn<Rdv, String> colDate;
    @FXML private TableColumn<Rdv, String> colMotif;
    @FXML private TableColumn<Rdv, String> colStatut;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        welcomeLabel.setText("Dr. " + user.getNomComplet());

        if (rdvTable != null) {
            colPatient.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
            colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }
        loadData(user.getId());
    }

    private void loadData(int medecinId) {
        try {
            var rdvList = new RdvDAO().findByMedecin(medecinId);
            rdvTotalLabel.setText(String.valueOf(rdvList.size()));
            long attente = rdvList.stream().filter(r -> "EN_ATTENTE".equals(r.getStatut())).count();
            rdvAttenteLabel.setText(String.valueOf(attente));
            if (rdvTable != null) rdvTable.setItems(FXCollections.observableArrayList(rdvList));

            var creneaux = new CreneauDAO().findByMedecin(medecinId);
            long dispo = creneaux.stream().filter(Creneau::isDisponible).count();
            creneauxLabel.setText(String.valueOf(dispo));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void goRdv()       { navigate("/fxml/medecin/rdv.fxml", "Mes RDV"); }
    @FXML private void goCreneaux()  { navigate("/fxml/medecin/creneaux.fxml", "Mes Créneaux"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
