package com.learnhub.controller.admin;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Seance;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class SeanceFormController {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Module> moduleCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private TextField salleField;
    @FXML private ComboBox<String> typeCombo;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private Seance currentSeance;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("CM", "TD", "TP", "Examen", "Autre");
        loadModules();
    }

    private void loadModules() {
        try {
            moduleCombo.getItems().setAll(moduleDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSeance(Seance seance) {
        this.currentSeance = seance;
        if (seance != null) {
            titleLabel.setText("Modifier la Séance");
            datePicker.setValue(LocalDate.parse(seance.getDate()));
            heureDebutField.setText(seance.getHeureDebut());
            heureFinField.setText(seance.getHeureFin());
            salleField.setText(seance.getSalle());
            typeCombo.setValue(seance.getType());
            
            // Match module
            for(Module m : moduleCombo.getItems()) {
                if(m.getId() == seance.getModuleId()) {
                    moduleCombo.setValue(m);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        if (currentSeance == null) currentSeance = new Seance();

        currentSeance.setModuleId(moduleCombo.getValue().getId());
        currentSeance.setDate(datePicker.getValue().toString());
        currentSeance.setHeureDebut(heureDebutField.getText());
        currentSeance.setHeureFin(heureFinField.getText());
        currentSeance.setSalle(salleField.getText());
        currentSeance.setType(typeCombo.getValue());

        try {
            if (currentSeance.getId() == 0) {
                seanceDAO.insert(currentSeance);
            } else {
                seanceDAO.update(currentSeance);
            }
            goBack();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'enregistrer la séance : " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        StringBuilder errorMsg = new StringBuilder();

        // Reset styles
        moduleCombo.setStyle("");
        datePicker.setStyle("");
        heureDebutField.setStyle("");
        heureFinField.setStyle("");
        salleField.setStyle("");
        typeCombo.setStyle("");

        if (moduleCombo.getValue() == null) {
            moduleCombo.setStyle("-fx-border-color: red;");
            isValid = false;
            errorMsg.append("- Le module est obligatoire.\n");
        }
        if (datePicker.getValue() == null) {
            datePicker.setStyle("-fx-border-color: red;");
            isValid = false;
            errorMsg.append("- La date est obligatoire.\n");
        }
        if (heureDebutField.getText().isEmpty()) {
            heureDebutField.setStyle("-fx-border-color: red;");
            isValid = false;
            errorMsg.append("- L'heure de début est obligatoire.\n");
        }
        if (heureFinField.getText().isEmpty()) {
            heureFinField.setStyle("-fx-border-color: red;");
            isValid = false;
            errorMsg.append("- L'heure de fin est obligatoire.\n");
        }
        if (salleField.getText().isEmpty()) {
            salleField.setStyle("-fx-border-color: red;");
            isValid = false;
            errorMsg.append("- La salle est obligatoire.\n");
        }
        if (typeCombo.getValue() == null) {
            typeCombo.setStyle("-fx-border-color: red;");
            isValid = false;
            errorMsg.append("- Le type de séance est obligatoire.\n");
        }

        if (!isValid) {
            showAlert("Champs manquants", "Veuillez remplir les champs en rouge :\n" + errorMsg.toString());
            return false;
        }

        // Time format check HH:mm
        String timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        if (!heureDebutField.getText().matches(timeRegex)) {
            heureDebutField.setStyle("-fx-border-color: red;");
            showAlert("Format invalide", "L'heure de début doit être au format HH:mm (ex: 08:30).");
            return false;
        }
        if (!heureFinField.getText().matches(timeRegex)) {
            heureFinField.setStyle("-fx-border-color: red;");
            showAlert("Format invalide", "L'heure de fin doit être au format HH:mm (ex: 10:30).");
            return false;
        }

        // Logical check: End time must be after start time
        try {
            java.time.LocalTime start = java.time.LocalTime.parse(heureDebutField.getText());
            java.time.LocalTime end = java.time.LocalTime.parse(heureFinField.getText());
            if (!end.isAfter(start)) {
                heureFinField.setStyle("-fx-border-color: red;");
                showAlert("Erreur de cohérence", "L'heure de fin doit être postérieure à l'heure de début.");
                return false;
            }
        } catch (Exception e) {
            // Should not happen due to regex check
        }

        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo((Stage) datePicker.getScene().getWindow(), "/fxml/admin/seances.fxml", "Gestion des Séances");
    }
}
