package com.learnhub.controller.visiteur;

import com.learnhub.dao.CandidatureDAO;
import com.learnhub.models.Candidature;
import com.learnhub.models.Filiere;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Candidature envoyée !");
            alert.setContentText("Votre candidature a bien été enregistrée pour la filière : " + filiereCourante.getNom());
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
        StringBuilder errors = new StringBuilder();

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.length() <= 3) {
            errors.append("- Le nom doit comporter plus de 3 caractères.\n");
        }

        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            errors.append("- L'email n'est pas valide (doit contenir @ et .).\n");
        }

        String phone = telephoneField.getText() == null ? "" : telephoneField.getText().trim();
        // Allow formatting: +216 71856935 or 71 856 935 or 71856935
        if (phone.isEmpty() || !Pattern.matches("^(\\+\\d{1,3}\\s?)?\\d{2,10}(\\s?\\d{2,4})*$", phone)) {
            errors.append("- Le téléphone n'est pas valide.\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Saisie");
            alert.setHeaderText("Veuillez corriger les champs suivants :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
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
