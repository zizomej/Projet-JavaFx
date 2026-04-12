package com.learnhub.controller.professor;

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

public class ProfessorNotesController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<Cours> filtreModule;
    @FXML private TableView<Note> table;
    @FXML private TableColumn<Note, Integer> colId;
    @FXML private TableColumn<Note, String> colEtudiant;
    @FXML private TableColumn<Note, String> colModule;
    @FXML private TableColumn<Note, Double> colValeur;
    @FXML private TableColumn<Note, String> colType;
    @FXML private TableColumn<Note, String> colDate;
    @FXML private TableColumn<Note, String> colMention;

    private ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private ObservableList<Cours> professorModules = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> students = FXCollections.observableArrayList();
    private final NoteDAO noteDAO = new NoteDAO();
    private final CoursDAO coursDAO = new CoursDAO();
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
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colValeur.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        colMention.setCellValueFactory(new PropertyValueFactory<>("mention"));

        loadModules(currentProfessorId);
        loadStudents();
        loadData();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterTable());
        filtreModule.valueProperty().addListener((obs, oldValue, newValue) -> filterTable());
    }

    private void loadModules(int profId) {
        try {
            List<Cours> modules = coursDAO.findByProfesseur(profId);
            professorModules.setAll(modules);
            filtreModule.setItems(FXCollections.observableArrayList(modules));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStudents() {
        try {
            students.setAll(new UtilisateurDAO().findByRole("ROLE_ETUDIANT"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            List<Note> list = noteDAO.findByEnseignant(currentProfessorId);
            allNotes = FXCollections.observableArrayList(list);
            table.setItems(allNotes);
            statusLabel.setText(list.size() + " note(s)");
        } catch (SQLException e) {
            statusLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        Cours selectedModule = filtreModule.getValue();

        ObservableList<Note> filtered = allNotes.filtered(note -> {
            boolean matchSearch = search.isEmpty()
                    || (note.getEtudiantNom() != null && note.getEtudiantNom().toLowerCase().contains(search));
            boolean matchModule = selectedModule == null || note.getModuleId() == selectedModule.getId();
            return matchSearch && matchModule;
        });
        table.setItems(filtered);
    }

    @FXML
    private void handleAjouter() {
        Dialog<Note> dialog = buildNoteDialog(null);
        Optional<Note> result = dialog.showAndWait();
        result.ifPresent(note -> {
            try {
                noteDAO.insert(note);
                loadData();
            } catch (SQLException e) {
                showError("Erreur lors de l'ajout: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleModifier() {
        Note selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une note.");
            return;
        }
        Dialog<Note> dialog = buildNoteDialog(selected);
        Optional<Note> result = dialog.showAndWait();
        result.ifPresent(note -> {
            try {
                note.setId(selected.getId());
                noteDAO.update(note);
                loadData();
            } catch (SQLException e) {
                showError("Erreur lors de la modification: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSupprimer() {
        Note selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une note.");
            return;
        }
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Supprimer la note de " + selected.getEtudiantNom() + " ?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                try {
                    noteDAO.delete(selected.getId());
                    loadData();
                } catch (SQLException e) {
                    showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private Dialog<Note> buildNoteDialog(Note existing) {
        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter une note" : "Modifier la note");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("admin-dialog-pane");

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        ComboBox<Utilisateur> studentBox = new ComboBox<>(students);
        ComboBox<Cours> moduleBox = new ComboBox<>(professorModules);
        TextField valueField = new TextField(existing != null ? String.valueOf(existing.getValeur()) : "");
        valueField.setPromptText("Ex: 14.5");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(
                "CC", "DS1", "DS2", "TP1", "TP2", "PROJET", "EXAMEN", "RATTRAPAGE"
        ));
        typeBox.setValue(existing != null && existing.getType() != null ? existing.getType() : "CC");

        Spinner<Double> coefficientSpinner = new Spinner<>();
        coefficientSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                1.0, 20.0, existing != null && existing.getCoefficient() > 0 ? existing.getCoefficient() : 1.0, 1.0
        ));
        coefficientSpinner.setEditable(true);

        DatePicker datePicker = new DatePicker(
                existing != null && existing.getDateEvaluation() != null && !existing.getDateEvaluation().isBlank()
                        ? LocalDate.parse(existing.getDateEvaluation())
                        : LocalDate.now()
        );

        if (existing != null) {
            students.stream()
                    .filter(student -> student.getId() == existing.getEtudiantId())
                    .findFirst()
                    .ifPresent(studentBox::setValue);
            professorModules.stream()
                    .filter(module -> module.getId() == existing.getModuleId())
                    .findFirst()
                    .ifPresent(moduleBox::setValue);
        }

        studentBox.setPrefWidth(320);
        moduleBox.setPrefWidth(320);
        typeBox.setPrefWidth(180);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Etudiant *"), studentBox);
        grid.addRow(1, new Label("Module *"), moduleBox);
        grid.addRow(2, new Label("Valeur /20 *"), valueField);
        grid.addRow(3, new Label("Type"), typeBox);
        grid.addRow(4, new Label("Coefficient"), coefficientSpinner);
        grid.addRow(5, new Label("Date"), datePicker);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButton) {
                return null;
            }
            try {
                Note note = new Note();
                if (studentBox.getValue() == null || moduleBox.getValue() == null) {
                    showError("Veuillez selectionner un etudiant et un module.");
                    return null;
                }
                note.setEtudiantId(studentBox.getValue().getId());
                note.setEtudiantNom(studentBox.getValue().getNomComplet());
                note.setModuleId(moduleBox.getValue().getId());
                note.setModuleTitre(moduleBox.getValue().getTitre());
                note.setEnseignantId(currentProfessorId);
                note.setValeur(Double.parseDouble(valueField.getText().trim()));
                note.setType(typeBox.getValue());
                note.setCoefficient(coefficientSpinner.getValue());
                note.setDateEvaluation(datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString());
                return note;
            } catch (NumberFormatException e) {
                showError("La valeur de note est invalide.");
                return null;
            }
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
