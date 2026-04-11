package com.learnhub.controller.admin;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class EvenementFormController {

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField lieuField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea descriptionArea;

    private final EvenementDAO evenementDAO = new EvenementDAO();
    private final com.learnhub.dao.LieuDAO lieuDAO = new com.learnhub.dao.LieuDAO();
    private Evenement currentEvent;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("Conférence", "Atelier", "Fête", "Sport", "Culturel", "Autre");
        statutCombo.getItems().addAll("En cours", "Terminé", "Annulé");
    }

    public void setEvenement(Evenement event) {
        this.currentEvent = event;
        if (event != null) {
            titleLabel.setText("Modifier l'Événement");
            titreField.setText(event.getTitre());
            typeCombo.setValue(event.getTypeEvenement());
            lieuField.setText(event.getLieuNom());
            dateDebutPicker.setValue(event.getDateDebut());
            dateFinPicker.setValue(event.getDateFin());
            heureDebutField.setText(event.getHeureDebut() != null ? event.getHeureDebut().toString() : "");
            heureFinField.setText(event.getHeureFin() != null ? event.getHeureFin().toString() : "");
            capaciteField.setText(String.valueOf(event.getCapacite()));
            statutCombo.setValue(event.getStatut());
            descriptionArea.setText(event.getDescription());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        if (currentEvent == null) currentEvent = new Evenement();

        currentEvent.setTitre(titreField.getText());
        currentEvent.setTypeEvenement(typeCombo.getValue());
        currentEvent.setLieuNom(lieuField.getText());
        currentEvent.setDateDebut(dateDebutPicker.getValue());
        currentEvent.setDateFin(dateFinPicker.getValue());
        
        try {
            currentEvent.setHeureDebut(LocalTime.parse(heureDebutField.getText()));
            currentEvent.setHeureFin(LocalTime.parse(heureFinField.getText()));
            
            // Résolution du lieuId à partir du nom
            int lieuId = lieuDAO.getOrCreateLieuId(lieuField.getText());
            currentEvent.setLieuId(lieuId);
            
            currentEvent.setCapacite(Integer.parseInt(capaciteField.getText()));
            currentEvent.setStatut(statutCombo.getValue());
            currentEvent.setDescription(descriptionArea.getText());

            if (currentEvent.getId() == 0) {
                evenementDAO.insert(currentEvent);
            } else {
                evenementDAO.update(currentEvent);
            }
            goBack();
        } catch (DateTimeParseException e) {
            showAlert("Format invalide", "L'heure est invalide.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer l'événement : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Format invalide", "La capacité doit être un nombre.");
        }
    }

    private boolean validateInput() {
        if (titreField.getText().isEmpty() || typeCombo.getValue() == null || 
            lieuField.getText().isEmpty() ||
            dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null || 
            heureDebutField.getText().isEmpty() || heureFinField.getText().isEmpty() ||
            statutCombo.getValue() == null) {
            showAlert("Validation", "Veuillez remplir tous les champs obligatoires (incluant le Lieu).");
            return false;
        }


        try {
            LocalTime.parse(heureDebutField.getText());
            LocalTime.parse(heureFinField.getText());
        } catch (DateTimeParseException e) {
            showAlert("Format invalide", "L'heure doit être au format HH:mm (ex: 09:00).");
            return false;
        }

        try {
            Integer.parseInt(capaciteField.getText());
        } catch (NumberFormatException e) {
            showAlert("Format invalide", "La capacité doit être un nombre entier.");
            return false;
        }

        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showAlert("Validation date", "La date de fin ne peut pas être avant la date de début.");
            return false;
        }

        if (dateFinPicker.getValue().isEqual(dateDebutPicker.getValue())) {
            LocalTime start = LocalTime.parse(heureDebutField.getText());
            LocalTime end = LocalTime.parse(heureFinField.getText());
            if (!end.isAfter(start)) {
                showAlert("Validation heure", "L'heure de fin doit être après l'heure de début pour un événement le même jour.");
                return false;
            }
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
        NavigationUtil.navigateTo((Stage) titreField.getScene().getWindow(), "/fxml/admin/evenements.fxml", "Gestion des Événements");
    }
}
