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

    private Filiere filiereCourante;
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final UniversiteDAO universiteDAO = new UniversiteDAO();

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll("Licence", "Master", "Doctorat", "Ingénierie");

        // Load Universités depuis la table 'universite'
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

        // Utiliser l'id de l'admin connecté au lieu d'un ID fixe qui peut ne pas
        // exister
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId > 0) {
            filiereCourante.setResponsableId(currentUserId);
        } else {
            // Fallback sur 1 seulement si vraiment nécessaire, mais idéalement on devrait
            // avoir un utilisateur
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
        StringBuilder errors = new StringBuilder();

        // 1. Code de la filière > 3 caractères
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.length() <= 3) {
            errors.append("- Le code de la filière doit comporter plus de 3 caractères.\n");
        }

        // 2. Nom de la filière > 3 caractères
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.length() <= 3) {
            errors.append("- Le nom de la filière doit comporter plus de 3 caractères.\n");
        }

        // 3. Niveau d'études et Université obligatoires
        if (niveauCombo.getValue() == null) {
            errors.append("- Le niveau d'études est obligatoire.\n");
        }
        if (universiteCombo.getValue() == null) {
            errors.append("- L'université d'appartenance est obligatoire.\n");
        }

        // 4. Durée doit être un nombre entier
        String dureeTxt = dureeField.getText() == null ? "" : dureeField.getText().trim();
        if (dureeTxt.isEmpty()) {
            errors.append("- La durée est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(dureeTxt);
            } catch (NumberFormatException e) {
                errors.append("- La durée doit être un nombre entier.\n");
            }
        }

        // 5. Capacité maximale doit être un nombre entier
        String capaciteTxt = capaciteField.getText() == null ? "" : capaciteField.getText().trim();
        if (capaciteTxt.isEmpty()) {
            errors.append("- La capacité maximale est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(capaciteTxt);
            } catch (NumberFormatException e) {
                errors.append("- La capacité maximale doit être un nombre entier.\n");
            }
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Saisie");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI';");
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
