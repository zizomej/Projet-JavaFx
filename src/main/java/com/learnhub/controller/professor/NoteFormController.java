package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteFormController {

    @FXML private ComboBox<String> cbModule;
    @FXML private ComboBox<String> cbTypeNote;
    @FXML private TextField tfCoefficient;
    @FXML private VBox studentsListContainer;

    private Runnable onSuccess;
    private final NoteDAO noteDAO = new NoteDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private List<Module> moduleList = new ArrayList<>();
    private List<Utilisateur> etudiantList = new ArrayList<>();
    
    private Note currentNote; // Appelé si on édite une seule note (depuis les détails)
    private List<StudentRow> studentRows = new ArrayList<>();

    private static class StudentRow {
        Utilisateur etudiant;
        TextField tfNote;
        Note existingNote; // null pour les nouvelles notes
    }

    @FXML
    public void initialize() {
        cbTypeNote.getItems().addAll("DS1", "DS2", "DS3", "TP1", "TP2", "TP3", "PROJET", "EXAMEN", "CC");

        tfCoefficient.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*([\\.,]\\d*)?")) {
                tfCoefficient.setText(oldValue);
            }
        });

        tfCoefficient.textProperty().addListener((obs, old, newVal) -> clearError(tfCoefficient));
        cbModule.valueProperty().addListener((obs, old, newVal) -> clearError(cbModule));
        cbTypeNote.valueProperty().addListener((obs, old, newVal) -> clearError(cbTypeNote));

        try {
            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            int profId = currentUser != null ? currentUser.getId() : 0;
            
            moduleList = moduleDAO.findByProfesseur(profId);
            for (Module m : moduleList) {
                cbModule.getItems().add(m.getCode() + " - " + m.getIntitule());
            }

            etudiantList = utilisateurDAO.findByRole("etudiant");
            if (etudiantList.isEmpty()) etudiantList = utilisateurDAO.findByRole("ETUDIANT");
            if (etudiantList.isEmpty()) etudiantList = utilisateurDAO.findByRole("ROLE_ETUDIANT");
            
            // Build default student list for bulk grading
            renderStudentRows(etudiantList, null);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderStudentRows(List<Utilisateur> students, Note specificNote) {
        studentsListContainer.getChildren().clear();
        studentRows.clear();

        for (Utilisateur etu : students) {
            StudentRow row = new StudentRow();
            row.etudiant = etu;
            row.existingNote = specificNote != null && specificNote.getEtudiantId() == etu.getId() ? specificNote : null;

            HBox hbox = new HBox(15);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setStyle("-fx-padding: 8 12; -fx-background-color: #f8fafc; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

            Label nameLbl = new Label(etu.getNom() + " " + etu.getPrenom());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            TextField tfNote = new TextField();
            tfNote.setPromptText("/ 20");
            tfNote.setPrefWidth(80);
            tfNote.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 5 8; -fx-alignment: center;");
            
            // Controle des caractères
            tfNote.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && !newValue.matches("\\d*([\\.,]\\d*)?")) {
                    tfNote.setText(oldValue);
                }
                clearError(tfNote);
            });

            if (row.existingNote != null) {
                tfNote.setText(String.valueOf(row.existingNote.getValeur()));
            }

            row.tfNote = tfNote;
            studentRows.add(row);

            hbox.getChildren().addAll(nameLbl, spacer, tfNote);
            studentsListContainer.getChildren().add(hbox);
        }
        
        if (students.isEmpty()) {
            studentsListContainer.getChildren().add(new Label("Aucun étudiant trouvé dans le système."));
        }
    }

    public void setNote(Note note) {
        this.currentNote = note;
        if (note != null) {
            for(int i=0; i<moduleList.size(); i++) {
                if(moduleList.get(i).getId() == note.getModuleId()) { cbModule.getSelectionModel().select(i); break; }
            }
            cbTypeNote.getSelectionModel().select(note.getTypeNote());
            tfCoefficient.setText(String.valueOf(note.getCoefficient()));
            
            // Si on édite une note unique, on filtre la vue (Bulk mode -> Single mode)
            List<Utilisateur> singletonList = new ArrayList<>();
            for (Utilisateur etu : etudiantList) {
                if (etu.getId() == note.getEtudiantId()) {
                    singletonList.add(etu);
                    break;
                }
            }
            renderStudentRows(singletonList, note);
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    private void showError(javafx.scene.Node node, String message) {
        if (!node.getStyle().contains("#ef4444")) {
            node.setStyle(node.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1px;");
        }
        Tooltip t = new Tooltip(message);
        t.setStyle("-fx-background-color: #ef4444;");
        Tooltip.install(node, t);
    }

    private void clearError(javafx.scene.Node node) {
        if (node.getStyle().contains("#ef4444")) {
            node.setStyle(node.getStyle().replace("; -fx-border-color: #ef4444; -fx-border-width: 1px;", ""));
        }
        Tooltip.uninstall(node, null);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tfCoefficient.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave() {
        boolean globalError = false;

        if (cbModule.getValue() == null) { showError(cbModule, "Sélectionnez un module."); globalError = true; }
        if (cbTypeNote.getValue() == null) { showError(cbTypeNote, "Type de note obligatoire."); globalError = true; }
        if (tfCoefficient.getText() == null || tfCoefficient.getText().trim().isEmpty()) { showError(tfCoefficient, "Coefficient obligatoire."); globalError = true; }
        
        double coefficient = 1.0;
        int moduleId = 0;
        
        try {
            if (!globalError) {
                coefficient = Double.parseDouble(tfCoefficient.getText().replace(",", "."));
                if (coefficient < 0.5 || coefficient > 10) {
                    showError(tfCoefficient, "Le coefficient doit être compris entre 0.5 et 10.");
                    globalError = true;
                }
                int moduleIndex = cbModule.getSelectionModel().getSelectedIndex();
                moduleId = moduleList.get(moduleIndex).getId();
            }
        } catch (NumberFormatException e) {
            showError(tfCoefficient, "Format de coefficient invalide.");
            globalError = true;
        }

        if (globalError) return;

        boolean hasRowErrors = false;
        int savedCount = 0;
        Utilisateur cur = SessionManager.getInstance().getCurrentUser();
        int profId = cur != null ? cur.getId() : 0;

        for (StudentRow row : studentRows) {
            String valStr = row.tfNote.getText();
            if (valStr == null || valStr.trim().isEmpty()) {
                continue; // L'étudiant n'est pas noté cette fois (absent ou autre)
            }

            try {
                double valeur = Double.parseDouble(valStr.replace(",", "."));
                if (valeur < 0 || valeur > 20) {
                    showError(row.tfNote, "La note doit être comprise entre 0 et 20.");
                    hasRowErrors = true;
                    continue;
                }

                Note note = row.existingNote == null ? new Note() : row.existingNote;
                note.setModuleId(moduleId);
                note.setEtudiantId(row.etudiant.getId());
                note.setTypeNote(cbTypeNote.getValue());
                note.setValeur(valeur);
                note.setCoefficient(coefficient);
                note.setEnseignantId(profId);

                if (note.getId() == 0) {
                    note.setDateSaisie(java.time.LocalDate.now().toString());
                    noteDAO.add(note);
                } else {
                    noteDAO.update(note);
                }
                savedCount++;

            } catch (Exception e) {
                showError(row.tfNote, "Note invalide.");
                hasRowErrors = true;
            }
        }

        if (hasRowErrors) {
            new Alert(Alert.AlertType.WARNING, "Certaines notes sont invalides (encadrées en rouge). Veuillez les corriger.").show();
            return;
        }

        if (savedCount == 0 && studentRows.size() > 0 && currentNote == null) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune note n'a été saisie. Tout a été ignoré.").showAndWait();
            handleClose();
            return;
        }

        if (onSuccess != null) {
            onSuccess.run();
        }
        handleClose();
    }
}
