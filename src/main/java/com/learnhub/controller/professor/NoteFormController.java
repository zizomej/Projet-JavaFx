package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteFormController {

    @FXML private ComboBox<String> cbModule;
    @FXML private ComboBox<String> cbEtudiant;
    @FXML private ComboBox<String> cbTypeNote;
    @FXML private TextField tfValeur;
    @FXML private TextField tfCoefficient;

    private Runnable onSuccess;
    private final NoteDAO noteDAO = new NoteDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private List<Module> moduleList = new ArrayList<>();
    private List<Utilisateur> etudiantList = new ArrayList<>();
    
    private Note currentNote;

    @FXML
    public void initialize() {
        cbTypeNote.getItems().addAll("DS1", "DS2", "DS3", "TP1", "TP2", "TP3", "PROJET", "EXAMEN", "CC");

        try {
            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            int profId = currentUser != null ? currentUser.getId() : 0;
            
            moduleList = moduleDAO.findByProfesseur(profId);
            for (Module m : moduleList) {
                cbModule.getItems().add(m.getCode() + " - " + m.getIntitule());
            }

            // In our system, typical student roles are "ETUDIANT" or "etudiant"
            etudiantList = utilisateurDAO.findByRole("etudiant");
            if (etudiantList.isEmpty()) {
                etudiantList = utilisateurDAO.findByRole("ETUDIANT");
            }
            if (etudiantList.isEmpty()) {
                etudiantList = utilisateurDAO.findByRole("ROLE_ETUDIANT");
            }

            for (Utilisateur etu : etudiantList) {
                cbEtudiant.getItems().add(etu.getNom() + " " + etu.getPrenom());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setNote(Note note) {
        this.currentNote = note;
        if (note != null) {
            for(int i=0; i<moduleList.size(); i++) {
                if(moduleList.get(i).getId() == note.getModuleId()) { cbModule.getSelectionModel().select(i); break; }
            }
            for(int i=0; i<etudiantList.size(); i++) {
                if(etudiantList.get(i).getId() == note.getEtudiantId()) { cbEtudiant.getSelectionModel().select(i); break; }
            }
            cbTypeNote.getSelectionModel().select(note.getTypeNote());
            tfValeur.setText(String.valueOf(note.getValeur()));
            tfCoefficient.setText(String.valueOf(note.getCoefficient()));
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tfValeur.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave() {
        if (cbModule.getValue() == null || cbEtudiant.getValue() == null ||
            cbTypeNote.getValue() == null || tfValeur.getText().isEmpty() ||
            tfCoefficient.getText().isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.");
            alert.show();
            return;
        }

        try {
            double valeur = Double.parseDouble(tfValeur.getText().replace(",", "."));
            if (valeur < 0 || valeur > 20) {
                new Alert(Alert.AlertType.WARNING, "La note doit être comprise entre 0 et 20.").show();
                return;
            }

            double coefficient = Double.parseDouble(tfCoefficient.getText().replace(",", "."));
            if (coefficient < 0.5 || coefficient > 10) {
                new Alert(Alert.AlertType.WARNING, "Le coefficient doit être compris entre 0.5 et 10.").show();
                return;
            }

            int moduleIndex = cbModule.getSelectionModel().getSelectedIndex();
            int etudiantIndex = cbEtudiant.getSelectionModel().getSelectedIndex();

            Note note = currentNote == null ? new Note() : currentNote;
            note.setModuleId(moduleList.get(moduleIndex).getId());
            note.setEtudiantId(etudiantList.get(etudiantIndex).getId());
            note.setTypeNote(cbTypeNote.getValue());
            note.setValeur(valeur);
            note.setCoefficient(coefficient);

            if (note.getId() == 0) note.setDateSaisie(java.time.LocalDate.now().toString());

            Utilisateur cur = SessionManager.getInstance().getCurrentUser();
            if (cur != null) {
                note.setEnseignantId(cur.getId());
            }

            if (note.getId() == 0) {
                noteDAO.add(note);
            } else {
                noteDAO.update(note);
            }

            if (onSuccess != null) {
                onSuccess.run();
            }
            handleClose();

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Format de note ou coefficient invalide.").show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement: " + e.getMessage()).show();
        }
    }
}
