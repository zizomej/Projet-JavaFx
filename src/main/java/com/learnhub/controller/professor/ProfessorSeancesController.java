package com.learnhub.controller.professor;

import com.learnhub.controller.admin.SeanceFormController;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Seance;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ProfessorSeancesController {

    @FXML private TableView<Seance> seancesTable;
    @FXML private TableColumn<Seance, String> colDate;
    @FXML private TableColumn<Seance, String> colHeure;
    @FXML private TableColumn<Seance, String> colModule;
    @FXML private TableColumn<Seance, String> colSalle;
    @FXML private TableColumn<Seance, String> colType;
    
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ObservableList<Seance> masterData = FXCollections.observableArrayList();
    private FilteredList<Seance> filteredData;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText(user.getNomComplet());
        }
        setupTable();
        setupSearch();
        loadData();
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(seance -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (seance.getModuleTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (seance.getType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (seance.getSalle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        seancesTable.setItems(filteredData);
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
    }

    private void loadData() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        try {
            masterData.setAll(seanceDAO.findByProfesseur(user.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Seance selected = seancesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner une séance à modifier.");
            return;
        }
        showForm(selected);
    }

    private void showForm(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/seance_form.fxml"));
            Parent root = loader.load();
            SeanceFormController controller = loader.getController();
            controller.setSeance(seance);

            Stage stage = (Stage) seancesTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleRefresh() { loadData(); }

    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Mes Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Mes Séances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Gestion des Notes"); }
    @FXML private void goPresences() { navigate("/fxml/professor/presences.fxml", "Présences"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) seancesTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) seancesTable.getScene().getWindow();
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
