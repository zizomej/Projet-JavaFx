package com.learnhub.controller.professor;

import com.learnhub.dao.NoteDAO;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.SessionManager;
import com.learnhub.util.DialogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;
import java.util.Locale;

public class NoteDetailsController {

    @FXML private Label lblScore;
    @FXML private Label lblMention;
    @FXML private Label lblEtudiant;
    @FXML private Label lblModule;
    @FXML private Label lblType;
    @FXML private Label lblCoeff;
    @FXML private Label lblPonderee;
    @FXML private Label lblEnseignant;
    @FXML private Label lblDate;

    private Note currentNote;
    private Runnable onSuccess;
    private final NoteDAO noteDAO = new NoteDAO();

    public void setNote(Note note) {
        this.currentNote = note;
        if (note != null) {
            lblScore.setText(String.format(Locale.US, "%.2f / 20", note.getValeur()));
            
            if (note.getValeur() >= 16) {
                lblMention.setText("★ EXCELLENT");
                lblMention.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px;");
            } else if (note.getValeur() >= 14) {
                lblMention.setText("★ TRES BIEN");
                lblMention.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px;");
            } else if (note.getValeur() >= 12) {
                lblMention.setText("★ BIEN");
                lblMention.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px;");
            } else if (note.getValeur() >= 10) {
                lblMention.setText("★ PASSABLE");
                lblMention.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px;");
            } else {
                lblMention.setText("📉 INSUFFISANT");
                lblMention.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 12px;");
            }

            lblEtudiant.setText(note.getEtudiantNom() != null ? note.getEtudiantNom() : String.valueOf(note.getEtudiantId()));
            lblModule.setText(note.getModuleIntitule() != null ? note.getModuleIntitule() : String.valueOf(note.getModuleId()));
            lblType.setText(note.getTypeNote());
            lblCoeff.setText(String.valueOf(note.getCoefficient()));
            
            double ponderee = note.getValeur() * note.getCoefficient();
            lblPonderee.setText(String.format(Locale.US, "%.2f points", ponderee));
            
            lblDate.setText(note.getDateSaisie() != null ? note.getDateSaisie() : "N/A");

            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getId() == note.getEnseignantId()) {
                lblEnseignant.setText(currentUser.getNomComplet());
            } else {
                lblEnseignant.setText("ID: " + note.getEnseignantId());
            }
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void handleListe() {
        Stage stage = (Stage) lblScore.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDelete() {
        boolean confirmed = DialogUtil.showDeleteConfirmation("cette note", currentNote != null ? currentNote.getTypeNote() + " de " + currentNote.getEtudiantNom() : "Note inconnue");
        if (confirmed) {
            try {
                noteDAO.delete(currentNote.getId());
                if (onSuccess != null) onSuccess.run();
                handleListe();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.").show();
            }
        }
    }

    @FXML
    private void handleEdit() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/professor/note_form.fxml"));
            javafx.scene.Parent root = loader.load();
            com.learnhub.controller.professor.NoteFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new javafx.scene.Scene(root));

            controller.setNote(currentNote);
            controller.setOnSuccess(() -> {
                if (onSuccess != null) onSuccess.run();
                handleListe(); // close the details window too so user goes back to main list
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
