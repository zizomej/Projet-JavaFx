package com.learnhub.controller.admin;

import com.learnhub.dao.CoursDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Cours;
import com.learnhub.models.Note;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class NotesAdminController {

    @FXML private TableView<Note> table;
    @FXML private TableColumn<Note, Integer> colId;
    @FXML private TableColumn<Note, String> colEtudiant;
    @FXML private TableColumn<Note, String> colModule;
    @FXML private TableColumn<Note, Double> colValeur;
    @FXML private TableColumn<Note, String> colType;
    @FXML private TableColumn<Note, String> colDate;
    @FXML private TableColumn<Note, String> colMention;
    @FXML private Label statusLabel;

    private final NoteDAO dao = new NoteDAO();
    private final ObservableList<Note> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("etudiantNom"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colValeur.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        colMention.setCellValueFactory(new PropertyValueFactory<>("mention"));
        table.setItems(data);
        loadData();
    }

    private void loadData() {
        try {
            data.setAll(dao.findAll());
            statusLabel.setText(data.size() + " note(s)");
        } catch (SQLException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML private void handleAdd() { showForm(null); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML
    private void handleEdit() {
        Note note = table.getSelectionModel().getSelectedItem();
        if (note == null) {
            statusLabel.setText("Selectionnez une note.");
            return;
        }
        showForm(note);
    }

    @FXML
    private void handleDelete() {
        Note note = table.getSelectionModel().getSelectedItem();
        if (note == null) {
            statusLabel.setText("Selectionnez une note.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette note ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                dao.delete(note.getId());
                loadData();
                statusLabel.setText("Note supprimee.");
            } catch (SQLException e) {
                statusLabel.setText("Erreur: " + e.getMessage());
            }
        }
    }

    private void showForm(Note note) {
        boolean edit = note != null;

        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle(edit ? "Modifier une note" : "Ajouter une note");
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        ComboBox<Utilisateur> etudiantBox = new ComboBox<>();
        ComboBox<Cours> moduleBox = new ComboBox<>();
        try {
            List<Utilisateur> etudiants = new UtilisateurDAO().findByRole("ROLE_ETUDIANT");
            etudiantBox.getItems().addAll(etudiants);
            List<Cours> modules = new CoursDAO().findAll();
            moduleBox.getItems().addAll(modules);

            if (edit) {
                etudiants.stream()
                        .filter(user -> user.getId() == note.getEtudiantId())
                        .findFirst()
                        .ifPresent(etudiantBox::setValue);
                modules.stream()
                        .filter(module -> module.getId() == note.getModuleId())
                        .findFirst()
                        .ifPresent(moduleBox::setValue);
            }
        } catch (SQLException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            return;
        }

        TextField valeurField = new TextField(edit ? String.valueOf(note.getValeur()) : "");
        valeurField.setPromptText("Ex: 15.5");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("CC", "DS1", "DS2", "TP1", "TP2", "PROJET", "EXAMEN", "RATTRAPAGE");
        typeBox.setValue(edit && note.getType() != null ? note.getType() : "CC");

        Spinner<Double> coefficientSpinner = new Spinner<>();
        coefficientSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                1.0, 20.0, edit && note.getCoefficient() > 0 ? note.getCoefficient() : 1.0, 1.0
        ));
        coefficientSpinner.setEditable(true);

        DatePicker datePicker = new DatePicker(
                edit && note.getDateEvaluation() != null && !note.getDateEvaluation().isBlank()
                        ? LocalDate.parse(note.getDateEvaluation())
                        : LocalDate.now()
        );

        etudiantBox.setPrefWidth(320);
        moduleBox.setPrefWidth(320);
        typeBox.setPrefWidth(180);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Etudiant *"), etudiantBox);
        grid.addRow(1, new Label("Module *"), moduleBox);
        grid.addRow(2, new Label("Valeur /20 *"), valeurField);
        grid.addRow(3, new Label("Type de note"), typeBox);
        grid.addRow(4, new Label("Coefficient"), coefficientSpinner);
        grid.addRow(5, new Label("Date de saisie"), datePicker);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }

            Note result = edit ? note : new Note();
            if (etudiantBox.getValue() != null) {
                result.setEtudiantId(etudiantBox.getValue().getId());
                result.setEtudiantNom(etudiantBox.getValue().getNomComplet());
            }
            if (moduleBox.getValue() != null) {
                result.setModuleId(moduleBox.getValue().getId());
                result.setModuleTitre(moduleBox.getValue().getTitre());
                result.setEnseignantId(moduleBox.getValue().getResponsableId());
            }
            try {
                result.setValeur(Double.parseDouble(valeurField.getText().trim()));
            } catch (NumberFormatException ignored) {
                result.setValeur(0.0);
            }
            result.setType(typeBox.getValue());
            result.setCoefficient(coefficientSpinner.getValue());
            result.setDateEvaluation(datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString());
            return result;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.getEtudiantId() <= 0) {
                statusLabel.setText("Selectionnez un etudiant.");
                return;
            }
            if (result.getModuleId() <= 0) {
                statusLabel.setText("Selectionnez un module.");
                return;
            }
            if (result.getValeur() < 0 || result.getValeur() > 20) {
                statusLabel.setText("La note doit etre comprise entre 0 et 20.");
                return;
            }

            try {
                if (edit) {
                    dao.update(result);
                    statusLabel.setText("Note modifiee.");
                } else {
                    dao.insert(result);
                    statusLabel.setText("Note ajoutee.");
                }
                loadData();
            } catch (SQLException e) {
                statusLabel.setText("Erreur: " + e.getMessage());
            }
        });
    }

    @FXML private void goBack() { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goDashboard() { nav("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs() { nav("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules() { nav("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances() { nav("/fxml/admin/seances.fxml", "Seances"); }
    @FXML private void goNotes() { nav("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences() { nav("/fxml/admin/presences.fxml", "Presences"); }
    @FXML private void goFilieres() { nav("/fxml/admin/filieres.fxml", "Filieres"); }
    @FXML private void goEvenements() { nav("/fxml/admin/evenements.fxml", "Evenements"); }
    @FXML private void goRdv() { nav("/fxml/admin/rdv.fxml", "RDV medicaux"); }
    @FXML private void goCreneaux() { nav("/fxml/admin/creneaux.fxml", "Creneaux"); }
    @FXML private void goPartenaires() { nav("/fxml/admin/partenaires/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage() { nav("/fxml/admin/offrestage/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage() { nav("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage"); }
    @FXML private void goMailing()       { nav("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), fxml, title);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) table.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }
}
