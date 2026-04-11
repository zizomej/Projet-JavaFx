package com.learnhub.controller.professor;

import com.learnhub.controller.admin.EvenementFormController;
import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ProfessorEvenementsController {

    @FXML private TableView<Evenement> evenementsTable;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colType;
    @FXML private TableColumn<Evenement, String> colDate;
    @FXML private TableColumn<Evenement, String> colStatut;
    
    @FXML private TextField searchField;
    @FXML private Label welcomeLabel;

    private final EvenementDAO evenementDAO = new EvenementDAO();
    private final ObservableList<Evenement> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText(user.getNomComplet());
        }
        setupTable();
        loadData();
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void loadData() {
        try {
            masterData.setAll(evenementDAO.findAll());
            evenementsTable.setItems(masterData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Evenement selected = evenementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner un événement à modifier.");
            return;
        }
        showForm(selected);
    }

    private void showForm(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/evenement_form.fxml"));
            Parent root = loader.load();
            EvenementFormController controller = loader.getController();
            controller.setEvenement(event);

            Stage stage = (Stage) evenementsTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Mes Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Mes Séances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Gestion des Notes"); }
    @FXML private void goPresences() { navigate("/fxml/professor/presences.fxml", "Présences"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) evenementsTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) evenementsTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
