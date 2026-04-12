package com.learnhub.controller.admin;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Partenaire;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class PartenaireFormController {
    
    @FXML private TextField nomField;
    @FXML private ComboBox<String> secteurBox;
    @FXML private ComboBox<String> statutBox;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextField adresseField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField websiteField;
    @FXML private TextArea descriptionArea;

    @FXML private Label titleIcon;
    @FXML private Label titleLabel;
    @FXML private Button saveButton;

    @FXML private Label nomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label villeError;
    @FXML private Label secteurError;

    private Partenaire partenaire;
    private boolean edit;
    private Stage stage;
    private Runnable onSaveCallback;
    private final PartenaireDAO dao = new PartenaireDAO();

    public void initData(Partenaire partenaire, boolean edit, Stage stage, Runnable onSaveCallback) {
        this.partenaire = partenaire;
        this.edit = edit;
        this.stage = stage;
        this.onSaveCallback = onSaveCallback;

        secteurBox.getItems().addAll("Technologie", "Finance", "Commerce", "Sante", "Education", "General");
        statutBox.getItems().addAll("actif", "inactif", "en_attente");

        if (edit) {
            if (titleIcon != null) titleIcon.setText("✏");
            if (titleLabel != null) titleLabel.setText("Modifier le Partenaire");
            if (saveButton != null) saveButton.setText("Enregistrer les modifications");
        }

        if (edit && partenaire != null) {
            nomField.setText(safe(partenaire.getNom()));
            secteurBox.setValue(safe(partenaire.getSecteur()));
            statutBox.setValue(partenaire.getStatut() != null ? partenaire.getStatut() : "actif");
            villeField.setText(safe(partenaire.getVille()));
            paysField.setText(safe(partenaire.getPays()));
            adresseField.setText(safe(partenaire.getAdresse()));
            emailField.setText(safe(partenaire.getEmail()));
            telephoneField.setText(safe(partenaire.getTelephone()));
            websiteField.setText(safe(partenaire.getWebsite()));
            descriptionArea.setText(safe(partenaire.getDescription()));
        } else {
            statutBox.setValue("actif");
        }
    }

    @FXML
    public void handleCreate() {
        boolean valid = true;
        
        if (nomError != null) { nomError.setVisible(false); nomError.setManaged(false); }
        if (emailError != null) { emailError.setVisible(false); emailError.setManaged(false); }
        if (telephoneError != null) { telephoneError.setVisible(false); telephoneError.setManaged(false); }
        if (villeError != null) { villeError.setVisible(false); villeError.setManaged(false); }
        if (secteurError != null) { secteurError.setVisible(false); secteurError.setManaged(false); }

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            if (nomError != null) { nomError.setVisible(true); nomError.setManaged(true); }
            valid = false;
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            if (emailError != null) { emailError.setVisible(true); emailError.setManaged(true); }
            valid = false;
        }
        if (secteurBox.getValue() == null) {
            if (secteurError != null) { secteurError.setVisible(true); secteurError.setManaged(true); }
            valid = false;
        }
        
        if (!valid) return;

        partenaire.setNom(nomField.getText().trim());
        partenaire.setSecteur(secteurBox.getValue() != null ? secteurBox.getValue() : "");
        partenaire.setStatut(statutBox.getValue());
        partenaire.setVille(villeField.getText().trim());
        partenaire.setPays(paysField.getText().trim());
        partenaire.setAdresse(adresseField.getText().trim());
        partenaire.setEmail(emailField.getText().trim());
        partenaire.setTelephone(telephoneField.getText().trim());
        partenaire.setWebsite(websiteField.getText().trim());
        partenaire.setDescription(descriptionArea.getText().trim());

        try {
            if (edit) {
                dao.update(partenaire);
            } else {
                dao.insert(partenaire);
            }
            if (onSaveCallback != null) onSaveCallback.run();
            if (stage != null) stage.close();
        } catch (SQLException e) {
            showError("Enregistrement impossible : " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        if (stage != null) stage.close();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
