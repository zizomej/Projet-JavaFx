package com.learnhub.controller.admin;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.UniversiteDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Universite;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class FiliereFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<String> niveauCombo;
    @FXML
    private TextField dureeField;
    @FXML
    private TextField capaciteField;
    @FXML
    private ComboBox<Universite> universiteCombo;

    @FXML private Label codeErrorLabel;
    @FXML private Label nomErrorLabel;
    @FXML private Label niveauErrorLabel;
    @FXML private Label universiteErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label capaciteErrorLabel;

    private Filiere filiereCourante;
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final UniversiteDAO universiteDAO = new UniversiteDAO();

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll("Licence", "Master", "Doctorat", "Ingénierie");


        try {
            List<Universite> universites = universiteDAO.findAll();
            universiteCombo.getItems().addAll(universites);

            universiteCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Universite u) {
                    return u != null ? u.getNom() : "";
                }

                @Override
                public Universite fromString(String string) {
                    return null;
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setFiliere(Filiere filiere) {
        this.filiereCourante = filiere;
        if (filiere != null) {
            titleLabel.setText("Modifier la filière");
            codeField.setText(filiere.getCode());
            nomField.setText(filiere.getNom());
            niveauCombo.setValue(filiere.getNiveau());
            dureeField.setText(String.valueOf(filiere.getDureeAnnees()));
            capaciteField.setText(String.valueOf(filiere.getCapaciteMax()));

            if (filiere.getUniversiteId() > 0) {
                universiteCombo.getItems().stream()
                        .filter(p -> p.getId() == filiere.getUniversiteId())
                        .findFirst()
                        .ifPresent(universiteCombo::setValue);
            }

        } else {
            titleLabel.setText("Ajouter une filière");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        boolean isNew = (filiereCourante == null);
        if (isNew) {
            filiereCourante = new Filiere();
        }

        filiereCourante.setCode(codeField.getText());
        filiereCourante.setNom(nomField.getText());
        filiereCourante.setNiveau(niveauCombo.getValue());
        filiereCourante.setDureeAnnees(Integer.parseInt(dureeField.getText()));
        filiereCourante.setCapaciteMax(Integer.parseInt(capaciteField.getText()));
        filiereCourante.setUniversiteId(universiteCombo.getValue().getId());


        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId > 0) {
            filiereCourante.setResponsableId(currentUserId);
        } else {

            filiereCourante.setResponsableId(1);
        }

        try {
            if (isNew) {
                filiereDAO.insert(filiereCourante);
            } else {
                filiereDAO.update(filiereCourante);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Filière enregistrée avec succès !");
            alert.showAndWait();

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

        // Code Validation
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.length() <= 3) {
            showError(codeField, codeErrorLabel, "Le code doit comporter plus de 3 caractères.");
            isValid = false;
        }

        // Nom Validation
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.length() <= 3) {
            showError(nomField, nomErrorLabel, "Le nom doit comporter plus de 3 caractères.");
            isValid = false;
        }

        // Niveau Validation
        if (niveauCombo.getValue() == null) {
            showError(niveauCombo, niveauErrorLabel, "Le niveau d'études est obligatoire.");
            isValid = false;
        }

        // Université Validation
        if (universiteCombo.getValue() == null) {
            showError(universiteCombo, universiteErrorLabel, "L'université d'appartenance est obligatoire.");
            isValid = false;
        }

        // Durée Validation
        String dureeTxt = dureeField.getText() == null ? "" : dureeField.getText().trim();
        if (dureeTxt.isEmpty()) {
            showError(dureeField, dureeErrorLabel, "La durée est obligatoire.");
            isValid = false;
        } else {
            try {
                Integer.parseInt(dureeTxt);
            } catch (NumberFormatException e) {
                showError(dureeField, dureeErrorLabel, "La durée doit être un nombre entier.");
                isValid = false;
            }
        }

        // Capacité Validation
        String capaciteTxt = capaciteField.getText() == null ? "" : capaciteField.getText().trim();
        if (capaciteTxt.isEmpty()) {
            showError(capaciteField, capaciteErrorLabel, "La capacité maximale est obligatoire.");
            isValid = false;
        } else {
            try {
                Integer.parseInt(capaciteTxt);
            } catch (NumberFormatException e) {
                showError(capaciteField, capaciteErrorLabel, "La capacité maximale doit être un nombre entier.");
                isValid = false;
            }
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
        Control[] fields = {codeField, nomField, niveauCombo, universiteCombo, dureeField, capaciteField};
        Label[] labels = {codeErrorLabel, nomErrorLabel, niveauErrorLabel, universiteErrorLabel, dureeErrorLabel, capaciteErrorLabel};

        for (Control f : fields) f.getStyleClass().remove("form-control-error");
        for (Label l : labels) {
            l.setVisible(false);
            l.setManaged(false);
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/filieres.fxml", "Gestion des Filières");
    }
}
