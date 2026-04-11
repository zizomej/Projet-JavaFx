package com.learnhub.controller.admin;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class FiliereFormController {

    @FXML private Label titleLabel;
    @FXML private TextField codeField;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private TextField dureeField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<Partenaire> universiteCombo;

    private Filiere filiereCourante;
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final PartenaireDAO partenaireDAO = new PartenaireDAO();

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll("Licence", "Master", "Doctorat", "Ingénierie");

        // Load Universités (Partenaires)
        try {
            List<Partenaire> partenaires = partenaireDAO.findAll();
            universiteCombo.getItems().addAll(partenaires);

            // Set ComboBox to display the "nom" of Partenaire
            universiteCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Partenaire p) {
                    return p != null ? p.getNom() : "";
                }

                @Override
                public Partenaire fromString(String string) {
                    return null; // Not needed
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
        filiereCourante.setResponsableId(1); // placeholder ou a gerer si besoin

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
        StringBuilder errors = new StringBuilder();

        if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
            errors.append("- Le code de la filière est requis.\n");
        }
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom de la filière est requis.\n");
        }
        if (niveauCombo.getValue() == null) {
            errors.append("- Veuillez sélectionner un niveau d'études.\n");
        }
        if (universiteCombo.getValue() == null) {
            errors.append("- Veuillez sélectionner l'université d'appartenance.\n");
        }

        try {
            Integer.parseInt(dureeField.getText());
        } catch (NumberFormatException e) {
            errors.append("- La durée doit être un nombre entier valide.\n");
        }

        try {
            Integer.parseInt(capaciteField.getText());
        } catch (NumberFormatException e) {
            errors.append("- La capacité maximale doit être un nombre entier valide.\n");
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
        Stage stage = (Stage) codeField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/filieres.fxml", "Gestion des Filières");
    }
}
