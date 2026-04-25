package com.learnhub.controller.admin;

import com.learnhub.dao.UniversiteDAO;
import com.learnhub.models.Universite;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UniversiteFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<String> secteurCombo;
    @FXML
    private TextField villeField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField emailField;

    @FXML private Label nomErrorLabel;
    @FXML private Label secteurErrorLabel;
    @FXML private Label villeErrorLabel;
    @FXML private Label telephoneErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label emailErrorLabel;

    private Universite universiteCourante;
    private final UniversiteDAO universiteDAO = new UniversiteDAO();
    private Runnable onSaveCallback;

    public void setUniversite(Universite universite) {
        this.universiteCourante = universite;
        if (universite != null) {
            titleLabel.setText("Modifier l'université");
            nomField.setText(universite.getNom());
            secteurCombo.setValue(universite.getType());
            villeField.setText(universite.getVille());
            telephoneField.setText(universite.getTelephone());
            adresseField.setText(universite.getAdresse());
            emailField.setText(universite.getEmail());
        } else {
            titleLabel.setText("Ajouter une université");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        secteurCombo.getItems().addAll("Publique", "Privée");
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        boolean isNew = (universiteCourante == null);
        if (isNew) {
            universiteCourante = new Universite();
        }

        universiteCourante.setNom(nomField.getText());
        universiteCourante.setType(secteurCombo.getValue());
        universiteCourante.setVille(villeField.getText());
        universiteCourante.setTelephone(telephoneField.getText());
        universiteCourante.setAdresse(adresseField.getText());
        universiteCourante.setEmail(emailField.getText());

        try {
            if (isNew) {
                universiteDAO.insert(universiteCourante);
            } else {
                universiteDAO.update(universiteCourante);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Université enregistrée avec succès !");
            alert.showAndWait();

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            goBack();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean validateInput() {
        clearErrors();
        boolean isValid = true;

        // Nom Validation
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.length() <= 3) {
            showError(nomField, nomErrorLabel, "Le nom de l'université doit comporter plus de 3 caractères.");
            isValid = false;
        }

        // Type Validation
        if (secteurCombo.getValue() == null) {
            showError(secteurCombo, secteurErrorLabel, "Le type d'établissement est obligatoire.");
            isValid = false;
        }

        // Ville Validation
        String ville = villeField.getText() == null ? "" : villeField.getText().trim();
        if (ville.length() <= 3) {
            showError(villeField, villeErrorLabel, "La ville doit comporter plus de 3 caractères.");
            isValid = false;
        }

        // Téléphone Validation
        String telephone = telephoneField.getText() == null ? "" : telephoneField.getText().trim();
        if (!telephone.matches("^(\\+216\\s?\\d{8})|(\\d{2}\\s?\\d{3}\\s?\\d{3})|\\d{8}$")) {
            showError(telephoneField, telephoneErrorLabel, "Format invalide (+216 71856935 ou 71 856 935).");
            isValid = false;
        }

        // Adresse Validation
        String adresse = adresseField.getText() == null ? "" : adresseField.getText().trim();
        if (adresse.length() <= 4) {
            showError(adresseField, adresseErrorLabel, "L'adresse complète doit comporter plus de 4 caractères.");
            isValid = false;
        }

        // Email Validation
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(emailField, emailErrorLabel, "L'adresse e-mail n'est pas valide.");
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
        Control[] fields = {nomField, secteurCombo, villeField, telephoneField, adresseField, emailField};
        Label[] labels = {nomErrorLabel, secteurErrorLabel, villeErrorLabel, telephoneErrorLabel, adresseErrorLabel, emailErrorLabel};

        for (Control f : fields) f.getStyleClass().remove("form-control-error");
        for (Label l : labels) {
            l.setVisible(false);
            l.setManaged(false);
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/partenaires.fxml", "Gestion des Universités");
    }
}
