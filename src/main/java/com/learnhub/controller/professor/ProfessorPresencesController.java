package com.learnhub.controller.professor;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfessorPresencesController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<Presence> table;
    @FXML private TableColumn<Presence, Integer> colId;
    @FXML private TableColumn<Presence, String> colEtudiant;
    @FXML private TableColumn<Presence, String> colSeance;
    @FXML private TableColumn<Presence, String> colStatut;
    @FXML private TableColumn<Presence, String> colDate;

    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ObservableList<Seance> professorSeances = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> students = FXCollections.observableArrayList();
    private int currentProfessorId;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        currentProfessorId = user.getId();
        welcomeLabel.setText(user.getNomComplet());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("etudiantNom"));
        colSeance.setCellValueFactory(new PropertyValueFactory<>("seanceInfo"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadReferenceData();
        loadData();
    }

    private void loadReferenceData() {
        try {
            professorSeances.setAll(seanceDAO.findByProfesseur(currentProfessorId));
            students.setAll(new UtilisateurDAO().findByRole("ROLE_ETUDIANT"));
        } catch (SQLException e) {
            statusLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            Set<Integer> seanceIds = professorSeances.stream().map(Seance::getId).collect(Collectors.toSet());
            List<Presence> list = presenceDAO.findAll().stream()
                    .filter(presence -> seanceIds.contains(presence.getSeanceId()))
                    .toList();
            table.setItems(FXCollections.observableArrayList(list));
            statusLabel.setText(list.size() + " presence(s)");
        } catch (SQLException e) {
            statusLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        if (professorSeances.isEmpty()) {
            showError("Aucune seance n'est associee a votre compte.");
            return;
        }

        Dialog<Presence> dialog = buildPresenceDialog(null);
        Optional<Presence> result = dialog.showAndWait();
        result.ifPresent(presence -> {
            try {
                presenceDAO.insert(presence);
                loadData();
            } catch (SQLException e) {
                showError("Erreur lors de l'ajout: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleModifier() {
        Presence selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une presence.");
            return;
        }
        Dialog<Presence> dialog = buildPresenceDialog(selected);
        Optional<Presence> result = dialog.showAndWait();
        result.ifPresent(presence -> {
            try {
                presence.setId(selected.getId());
                presenceDAO.update(presence);
                loadData();
            } catch (SQLException e) {
                showError("Erreur lors de la modification: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSupprimer() {
        Presence selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une presence.");
            return;
        }
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Supprimer la presence de " + selected.getEtudiantNom() + " ?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                try {
                    presenceDAO.delete(selected.getId());
                    loadData();
                } catch (SQLException e) {
                    showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private Dialog<Presence> buildPresenceDialog(Presence existing) {
        Dialog<Presence> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter une presence" : "Modifier la presence");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<Utilisateur> studentBox = new ComboBox<>(students);
        ComboBox<Seance> seanceBox = new ComboBox<>(professorSeances);
        ComboBox<String> statutBox = new ComboBox<>(FXCollections.observableArrayList("PRESENT", "ABSENT", "RETARD", "JUSTIFIE"));

        if (existing != null) {
            students.stream()
                    .filter(student -> student.getId() == existing.getEtudiantId())
                    .findFirst()
                    .ifPresent(studentBox::setValue);
            professorSeances.stream()
                    .filter(seance -> seance.getId() == existing.getSeanceId())
                    .findFirst()
                    .ifPresent(seanceBox::setValue);
            statutBox.setValue(existing.getStatut());
        } else {
            statutBox.setValue("PRESENT");
        }

        studentBox.setPrefWidth(320);
        seanceBox.setPrefWidth(320);
        statutBox.setPrefWidth(180);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Etudiant *"), studentBox);
        grid.addRow(1, new Label("Seance *"), seanceBox);
        grid.addRow(2, new Label("Statut *"), statutBox);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }
            if (studentBox.getValue() == null || seanceBox.getValue() == null) {
                showError("Veuillez selectionner un etudiant et une seance.");
                return null;
            }

            Presence presence = new Presence();
            presence.setEtudiantId(studentBox.getValue().getId());
            presence.setEtudiantNom(studentBox.getValue().getNomComplet());
            presence.setSeanceId(seanceBox.getValue().getId());
            presence.setSeanceInfo(seanceBox.getValue().toString());
            presence.setDate(seanceBox.getValue().getDate());
            presence.setStatut(statutBox.getValue());
            return presence;
        });
        return dialog;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Espace Professeur"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Seances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Notes"); }
    @FXML private void goPresences() { navigate("/fxml/professor/presences.fxml", "Presences"); }
    @FXML private void goEvenements() { navigate("/fxml/professor/evenements.fxml", "Evenements"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
