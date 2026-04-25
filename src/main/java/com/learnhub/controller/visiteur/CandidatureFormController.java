package com.learnhub.controller.visiteur;

import com.learnhub.dao.CandidatureDAO;
import com.learnhub.models.Candidature;
import com.learnhub.models.Filiere;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class CandidatureFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;

    @FXML private Label nomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label telephoneErrorLabel;

    private Filiere filiereCourante;
    private final CandidatureDAO candidatureDAO = new CandidatureDAO();

    public void setFiliere(Filiere filiere) {
        this.filiereCourante = filiere;
        if (filiere != null) {
            subtitleLabel.setText("Filière : " + filiere.getNom());
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateInput()) {
            return;
        }

        Candidature candidature = new Candidature();
        candidature.setNom(nomField.getText().trim());
        candidature.setEmail(emailField.getText().trim());
        candidature.setTelephone(telephoneField.getText().trim());
        candidature.setFiliereId(filiereCourante.getId());

        try {
            candidatureDAO.insert(candidature);
            
            // Envoyer l'email de confirmation
            com.learnhub.service.EmailService.sendConfirmationEmail(candidature.getEmail(), candidature.getNom());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Candidature envoyée !");
            alert.setContentText("Votre candidature a bien été enregistrée. Un email de confirmation a été envoyé à : " + candidature.getEmail());
            alert.showAndWait();
            closeDialog();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur système");
            alert.setHeaderText("Impossible d'enregistrer la candidature");
            alert.setContentText("Détail: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean validateInput() {
        clearErrors();
        boolean isValid = true;

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.length() <= 3) {
            showError(nomField, nomErrorLabel, "Le nom doit comporter plus de 3 caractères.");
            isValid = false;
        }

        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(emailField, emailErrorLabel, "L'email n'est pas valide.");
            isValid = false;
        }

        String phone = telephoneField.getText() == null ? "" : telephoneField.getText().trim();
        if (phone.isEmpty() || !Pattern.matches("^(\\+\\d{1,3}\\s?)?\\d{2,10}(\\s?\\d{2,4})*$", phone)) {
            showError(telephoneField, telephoneErrorLabel, "Le téléphone n'est pas valide.");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Control field, Label errorLabel, String message) {
        field.getStyleClass().add("form-control-error");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearErrors() {
        Control[] fields = {nomField, emailField, telephoneField};
        Label[] labels = {nomErrorLabel, emailErrorLabel, telephoneErrorLabel};

        for (Control f : fields) f.getStyleClass().remove("form-control-error");
        for (Label l : labels) {
            l.setVisible(false);
            l.setManaged(false);
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
