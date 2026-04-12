package com.learnhub.controller.admin;

import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Presence;
import com.learnhub.models.Seance;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PresencesAdminController {

    @FXML private TableView<Presence> table;
    @FXML private TableColumn<Presence, Integer> colId;
    @FXML private TableColumn<Presence, String> colEtudiant;
    @FXML private TableColumn<Presence, String> colSeance;
    @FXML private TableColumn<Presence, String> colStatut;
    @FXML private TableColumn<Presence, String> colDate;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final PresenceDAO dao = new PresenceDAO();
    private final ObservableList<Presence> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("etudiantNom"));
        colSeance.setCellValueFactory(new PropertyValueFactory<>("seanceInfo"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        table.setItems(data);
        loadData();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> filter(newValue));
        }
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            if (statusLabel != null) {
                statusLabel.setText(data.size() + " presence(s)");
            }
        } catch (SQLException e) {
            if (statusLabel != null) {
                statusLabel.setText("Erreur: " + e.getMessage());
            }
        }
    }

    private void filter(String query) {
        try {
            List<Presence> all = dao.findAll();
            if (query != null && !query.isBlank()) {
                String normalized = query.toLowerCase();
                all = all.stream().filter(presence ->
                        (presence.getEtudiantNom() != null && presence.getEtudiantNom().toLowerCase().contains(normalized))
                                || (presence.getSeanceInfo() != null && presence.getSeanceInfo().toLowerCase().contains(normalized))
                ).toList();
            }
            data.setAll(all);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleAdd() { showForm(null); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML
    private void handleEdit() {
        Presence selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            show("Selectionnez une presence.");
            return;
        }
        showForm(selected);
    }

    @FXML
    private void handleDelete() {
        Presence selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            show("Selectionnez une presence.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette presence ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                dao.delete(selected.getId());
                loadData();
            } catch (SQLException e) {
                show("Erreur: " + e.getMessage());
            }
        }
    }

    private void showForm(Presence presence) {
        boolean edit = presence != null;

        Dialog<Presence> dialog = new Dialog<>();
        dialog.setTitle(edit ? "Modifier une presence" : "Ajouter une presence");
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        ComboBox<Utilisateur> etudiantBox = new ComboBox<>();
        ComboBox<Seance> seanceBox = new ComboBox<>();
        try {
            etudiantBox.getItems().addAll(new UtilisateurDAO().findByRole("ROLE_ETUDIANT"));
            seanceBox.getItems().addAll(new SeanceDAO().findAll());
        } catch (SQLException e) {
            show("Erreur: " + e.getMessage());
            return;
        }

        if (edit) {
            etudiantBox.getItems().stream()
                    .filter(user -> user.getId() == presence.getEtudiantId())
                    .findFirst()
                    .ifPresent(etudiantBox::setValue);
            seanceBox.getItems().stream()
                    .filter(seance -> seance.getId() == presence.getSeanceId())
                    .findFirst()
                    .ifPresent(seanceBox::setValue);
        }

        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("PRESENT", "ABSENT", "RETARD", "JUSTIFIE");
        statutBox.setValue(edit && presence.getStatut() != null ? presence.getStatut() : "PRESENT");

        Label dateInfo = new Label(edit ? "Date de la seance: " + safe(presence.getDate()) : "La date est recuperee automatiquement depuis la seance.");

        etudiantBox.setPrefWidth(320);
        seanceBox.setPrefWidth(320);
        statutBox.setPrefWidth(180);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Etudiant *"), etudiantBox);
        grid.addRow(1, new Label("Seance *"), seanceBox);
        grid.addRow(2, new Label("Statut *"), statutBox);
        grid.addRow(3, new Label("Information"), dateInfo);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }

            Presence result = edit ? presence : new Presence();
            if (etudiantBox.getValue() != null) {
                result.setEtudiantId(etudiantBox.getValue().getId());
                result.setEtudiantNom(etudiantBox.getValue().getNomComplet());
            }
            if (seanceBox.getValue() != null) {
                result.setSeanceId(seanceBox.getValue().getId());
                result.setSeanceInfo(seanceBox.getValue().toString());
                result.setDate(seanceBox.getValue().getDate());
            }
            result.setStatut(statutBox.getValue());
            return result;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getEtudiantId() <= 0) {
                show("Selectionnez un etudiant.");
                return;
            }
            if (result.getSeanceId() <= 0) {
                show("Selectionnez une seance.");
                return;
            }

            try {
                if (edit) {
                    dao.update(result);
                } else {
                    dao.insert(result);
                }
                loadData();
            } catch (SQLException e) {
                show("Erreur: " + e.getMessage());
            }
        });
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private void show(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    @FXML private void goBack() { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goDashboard() { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs() { nav("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules() { nav("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances() { nav("/fxml/admin/seances.fxml", "Seances"); }
    @FXML private void goNotes() { nav("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences() { loadData(); }
    @FXML private void goFilieres() { nav("/fxml/admin/filieres.fxml", "Filieres"); }
    @FXML private void goEvenements() { nav("/fxml/admin/evenements.fxml", "Evenements"); }
    @FXML private void goRdv() { nav("/fxml/admin/rdv.fxml", "RDV medicaux"); }
    @FXML private void goCreneaux() { nav("/fxml/admin/creneaux.fxml", "Creneaux"); }
    @FXML private void goPartenaires() { nav("/fxml/admin/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage() { nav("/fxml/admin/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage() { nav("/fxml/admin/demandes_stage.fxml", "Demandes de Stage"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }
}
