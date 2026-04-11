package com.learnhub.controller.admin;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UniversiteFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> secteurCombo;
    @FXML private TextField villeField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextField emailField;

    private Partenaire partenaireCourant;
    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private Runnable onSaveCallback;

    public void setPartenaire(Partenaire partenaire) {
        this.partenaireCourant = partenaire;
        if (partenaire != null) {
            titleLabel.setText("Modifier l'université");
            nomField.setText(partenaire.getNom());
            secteurCombo.setValue(partenaire.getSecteur());
            villeField.setText(partenaire.getVille());
            telephoneField.setText(partenaire.getTelephone());
            adresseField.setText(partenaire.getAdresse());
            emailField.setText(partenaire.getEmail());
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

        boolean isNew = (partenaireCourant == null);
        if (isNew) {
            partenaireCourant = new Partenaire();
        }

        partenaireCourant.setNom(nomField.getText());
        partenaireCourant.setSecteur(secteurCombo.getValue());
        partenaireCourant.setVille(villeField.getText());
        partenaireCourant.setTelephone(telephoneField.getText());
        partenaireCourant.setAdresse(adresseField.getText());
        partenaireCourant.setEmail(emailField.getText());
        partenaireCourant.setStatut("actif"); 

        try {
            if (isNew) {
                partenaireDAO.insert(partenaireCourant);
            } else {
                partenaireDAO.update(partenaireCourant);
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
        StringBuilder errors = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom de l'université est requis.\n");
        }
        if (secteurCombo.getValue() == null) {
            errors.append("- Veuillez sélectionner un type d'établissement.\n");
        }
        if (telephoneField.getText() != null && !telephoneField.getText().trim().isEmpty()) {
            if (!telephoneField.getText().matches("^[+]?\\d{8,15}$")) {
                errors.append("- Le numéro de téléphone est invalide (doit contenir entre 8 et 15 chiffres).\n");
            }
        }
        
        if (emailField.getText() != null && !emailField.getText().trim().isEmpty()) {
            if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                 errors.append("- Le format de l'e-mail est invalide.\n");
            }
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation");
            alert.setHeaderText("Corrigez les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/partenaires.fxml", "Gestion des Universités");
    }
}
