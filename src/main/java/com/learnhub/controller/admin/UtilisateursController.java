package com.learnhub.controller.admin;

import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UtilisateursController {

    @FXML private TableView<Utilisateur> table;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colTelephone;
    @FXML private TableColumn<Utilisateur, Boolean> colActif;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private Label statusLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private ObservableList<Utilisateur> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleLabel"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));

        roleFilter.getItems().addAll("Tous", "ROLE_ADMIN", "ROLE_PROFESSEUR", "ROLE_ETUDIANT", "ROLE_PARENT", "ROLE_MEDECIN");
        roleFilter.setValue("Tous");

        table.setItems(data);
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterData(val));
        roleFilter.valueProperty().addListener((obs, old, val) -> filterData(searchField.getText()));
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            statusLabel.setText(data.size() + " utilisateur(s)");
        } catch (SQLException e) {
            showError("Erreur chargement: " + e.getMessage());
        }
    }

    private void filterData(String search) {
        try {
            String role = roleFilter.getValue();
            List<Utilisateur> all = "Tous".equals(role) ? dao.findAll() : dao.findByRole(role);
            if (search != null && !search.isEmpty()) {
                String s = search.toLowerCase();
                all = all.stream().filter(u ->
                    u.getNom().toLowerCase().contains(s) ||
                    u.getPrenom().toLowerCase().contains(s) ||
                    u.getEmail().toLowerCase().contains(s)
                ).toList();
            }
            data.setAll(all);
            statusLabel.setText(data.size() + " utilisateur(s)");
        } catch (SQLException e) {
            showError("Erreur filtre: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        showForm(null);
    }

    @FXML
    private void handleEdit() {
        Utilisateur selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionnez un utilisateur."); return; }
        showForm(selected);
    }

    @FXML
    private void handleDelete() {
        Utilisateur selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Sélectionnez un utilisateur."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selected.getNomComplet() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                dao.delete(selected.getId());
                loadData();
                statusLabel.setText("Utilisateur supprimé.");
            } catch (SQLException e) {
                showError("Erreur suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() { loadData(); }

    private void showForm(Utilisateur user) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Nouvel utilisateur" : "Modifier utilisateur");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nomF = new TextField(user != null ? user.getNom() : "");
        TextField prenomF = new TextField(user != null ? user.getPrenom() : "");
        TextField emailF = new TextField(user != null ? user.getEmail() : "");
        PasswordField passF = new PasswordField();
        TextField telF = new TextField(user != null && user.getTelephone() != null ? user.getTelephone() : "");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ROLE_ADMIN","ROLE_PROFESSEUR","ROLE_ETUDIANT","ROLE_PARENT","ROLE_MEDECIN");
        roleBox.setValue(user != null ? user.getRoles() : "ROLE_ETUDIANT");
        CheckBox actifBox = new CheckBox("Actif");
        actifBox.setSelected(user == null || user.isActif());

        grid.add(new Label("Nom *"), 0, 0);       grid.add(nomF, 1, 0);
        grid.add(new Label("Prénom *"), 0, 1);    grid.add(prenomF, 1, 1);
        grid.add(new Label("Email *"), 0, 2);     grid.add(emailF, 1, 2);
        grid.add(new Label("Mot de passe"), 0, 3); grid.add(passF, 1, 3);
        grid.add(new Label("Rôle *"), 0, 4);      grid.add(roleBox, 1, 4);
        grid.add(new Label("Téléphone"), 0, 5);   grid.add(telF, 1, 5);
        grid.add(new Label("Statut"), 0, 6);      grid.add(actifBox, 1, 6);

        if (user == null) {
            grid.add(new Label("* Requis"), 0, 7, 2, 1);
        } else {
            grid.add(new Label("(Laisser vide = inchangé)"), 0, 3);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Utilisateur u = user != null ? user : new Utilisateur();
                u.setNom(nomF.getText().trim());
                u.setPrenom(prenomF.getText().trim());
                u.setEmail(emailF.getText().trim());
                u.setRoles(roleBox.getValue());
                u.setTelephone(telF.getText().trim());
                u.setActif(actifBox.isSelected());
                if (!passF.getText().isEmpty()) {
                    try {
                        u.setPassword(at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, passF.getText().toCharArray()));
                    } catch (Exception e) { u.setPassword(passF.getText()); }
                }
                return u;
            }
            return null;
        });

        Optional<Utilisateur> result = dialog.showAndWait();
        result.ifPresent(u -> {
            try {
                if (u.getNom().isEmpty() || u.getPrenom().isEmpty() || u.getEmail().isEmpty()) {
                    showError("Nom, prénom et email sont obligatoires."); return;
                }
                if (user == null) dao.insert(u);
                else dao.update(u);
                loadData();
                statusLabel.setText(user == null ? "Utilisateur ajouté." : "Utilisateur modifié.");
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        });
    }

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
    @FXML private void goPartenaires()    { nav("/fxml/admin/partenaires/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage()    { nav("/fxml/admin/offrestage/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage()  { nav("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage"); }
    @FXML private void goMailing()       { nav("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
    }
}
