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
        StringBuilder errors = new StringBuilder();

        // 1. Nom universitaire > 3 caractères
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.length() <= 3) {
            errors.append("- Le nom de l'université doit comporter plus de 3 caractères.\n");
        }

        // 2. Type obligatoire
        if (secteurCombo.getValue() == null) {
            errors.append("- Le type d'établissement est obligatoire.\n");
        }

        // 3. Ville obligatoire > 3 caractères
        String ville = villeField.getText() == null ? "" : villeField.getText().trim();
        if (ville.length() <= 3) {
            errors.append("- La ville doit comporter plus de 3 caractères.\n");
        }

        // 4. Téléphone (+216 71856935 ou 71 856 935)
        String telephone = telephoneField.getText() == null ? "" : telephoneField.getText().trim();
        // Regex supporte: +216 12345678, +21612345678, 12 345 678, 12345678
        if (!telephone.matches("^(\\+216\\s?\\d{8})|(\\d{2}\\s?\\d{3}\\s?\\d{3})|\\d{8}$")) {
            errors.append("- Le téléphone doit être sous la forme +216 71856935 ou 71 856 935.\n");
        }

        // 5. Adresse complète > 4 caractères
        String adresse = adresseField.getText() == null ? "" : adresseField.getText().trim();
        if (adresse.length() <= 4) {
            errors.append("- L'adresse complète doit comporter plus de 4 caractères.\n");
        }

        // 6. Email condition
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("- L'adresse e-mail n'est pas valide.\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Saisie");
            alert.setHeaderText("Veuillez corriger les informations :");
            alert.setContentText(errors.toString());
            alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");
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
