package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.repository.UtilisateurRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class UserManagementController {

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colStatut;

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCin;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboRole;
    @FXML private ComboBox<String> comboStatut;

    private final UtilisateurRepository userRepo = new UtilisateurRepository();
    private final ObservableList<Utilisateur> userList = FXCollections.observableArrayList();
    private Utilisateur selectedUser = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        comboRole.setItems(FXCollections.observableArrayList("admin", "medecin", "etudiant", "professeur"));
        comboStatut.setItems(FXCollections.observableArrayList("actif", "inactif", "suspendu"));

        loadUsers();

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                txtNom.setText(newVal.getNom());
                txtPrenom.setText(newVal.getPrenom());
                txtEmail.setText(newVal.getEmail());
                txtCin.setText(newVal.getCin());
                txtTelephone.setText(newVal.getTelephone());
                comboRole.setValue(newVal.getRole());
                comboStatut.setValue(newVal.getStatut());
            }
        });

        FilteredList<Utilisateur> filteredData = new FilteredList<>(userList, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getNom().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getPrenom().toLowerCase().contains(lowerCaseFilter)) return true;
                return user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<Utilisateur> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableUsers.comparatorProperty());
        tableUsers.setItems(sortedData);
    }

    @FXML private void handleRefresh() { loadUsers(); }

    private void loadUsers() {
        try {
            userList.setAll(userRepo.findAll());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() || txtEmail.getText().isEmpty() || comboRole.getValue() == null) {
            showAlert("Info", "Veuillez remplir les champs obligatoires (*)");
            return;
        }

        try {
            if (selectedUser == null) {
                Utilisateur newUser = new Utilisateur();
                fillUserFromForm(newUser);
                newUser.setMotDePasse("password123"); // Default password
                userRepo.save(newUser);
            } else {
                fillUserFromForm(selectedUser);
                userRepo.update(selectedUser);
            }
            loadUsers();
            handleClear();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try {
                userRepo.delete(selectedUser.getId());
                loadUsers();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        selectedUser = null;
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtCin.clear();
        txtTelephone.clear();
        comboRole.setValue(null);
        comboStatut.setValue(null);
        tableUsers.getSelectionModel().clearSelection();
    }

    private void fillUserFromForm(Utilisateur user) {
        user.setNom(txtNom.getText());
        user.setPrenom(txtPrenom.getText());
        user.setEmail(txtEmail.getText());
        user.setCin(txtCin.getText());
        user.setTelephone(txtTelephone.getText());
        user.setRole(comboRole.getValue());
        user.setStatut(comboStatut.getValue());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
