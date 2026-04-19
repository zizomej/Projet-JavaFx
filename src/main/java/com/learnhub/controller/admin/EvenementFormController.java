package com.learnhub.controller.admin;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Font;
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

    // ─── Style constants ─────────────────────────────────────────────
    private static final String STYLE_ERROR   = "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;";
    private static final String STYLE_OK      = "-fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;";
    private static final String STYLE_DEFAULT = "";

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("Conférence", "Atelier", "Fête", "Sport", "Culturel", "Autre");
        statutCombo.getItems().addAll("En cours", "Terminé", "Annulé");

        // Real-time reset on input
        titreField.textProperty().addListener((o, ov, nv) -> resetStyle(titreField));
        lieuField.textProperty().addListener((o, ov, nv) -> resetStyle(lieuField));
        heureDebutField.textProperty().addListener((o, ov, nv) -> resetStyle(heureDebutField));
        heureFinField.textProperty().addListener((o, ov, nv) -> resetStyle(heureFinField));
        capaciteField.textProperty().addListener((o, ov, nv) -> resetStyle(capaciteField));
        typeCombo.valueProperty().addListener((o, ov, nv) -> resetComboStyle(typeCombo));
        statutCombo.valueProperty().addListener((o, ov, nv) -> resetComboStyle(statutCombo));
        dateDebutPicker.valueProperty().addListener((o, ov, nv) -> resetDateStyle(dateDebutPicker));
        dateFinPicker.valueProperty().addListener((o, ov, nv) -> resetDateStyle(dateFinPicker));

        // Enforce digits-only for capaciteField
        capaciteField.textProperty().addListener((obs, ov, nv) -> {
            if (!nv.matches("\\d*")) capaciteField.setText(nv.replaceAll("[^\\d]", ""));
        });

        // Format validation on focus lost for time fields
        heureDebutField.focusedProperty().addListener((o, ov, focused) -> {
            if (!focused && !heureDebutField.getText().isEmpty()) validateTimeField(heureDebutField);
        });
        heureFinField.focusedProperty().addListener((o, ov, focused) -> {
            if (!focused && !heureFinField.getText().isEmpty()) validateTimeField(heureFinField);
        });
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

        currentEvent.setTitre(titreField.getText().trim());
        currentEvent.setTypeEvenement(typeCombo.getValue());
        currentEvent.setLieuNom(lieuField.getText().trim());
        currentEvent.setDateDebut(dateDebutPicker.getValue());
        currentEvent.setDateFin(dateFinPicker.getValue());

        try {
            currentEvent.setHeureDebut(LocalTime.parse(heureDebutField.getText().trim()));
            currentEvent.setHeureFin(LocalTime.parse(heureFinField.getText().trim()));

            int lieuId = lieuDAO.getOrCreateLieuId(lieuField.getText().trim());
            currentEvent.setLieuId(lieuId);

            currentEvent.setCapacite(Integer.parseInt(capaciteField.getText().trim()));
            currentEvent.setStatut(statutCombo.getValue());
            currentEvent.setDescription(descriptionArea.getText());

            if (currentEvent.getId() == 0) {
                evenementDAO.insert(currentEvent);
            } else {
                evenementDAO.update(currentEvent);
            }
            goBack();
        } catch (DateTimeParseException e) {
            showAlert("Format invalide", "L'heure doit être au format HH:mm (ex: 09:00).");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur base de données", "Impossible d'enregistrer l'événement : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Format invalide", "La capacité doit être un nombre entier.");
        }
    }

    // ─── Validation ──────────────────────────────────────────────────
    private boolean validateInput() {
        boolean valid = true;

        // Titre
        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            markError(titreField, "Le titre est obligatoire.");
            valid = false;
        } else {
            markOk(titreField);
        }

        // Type
        if (typeCombo.getValue() == null) {
            markComboError(typeCombo, "Veuillez choisir un type.");
            valid = false;
        } else {
            markComboOk(typeCombo);
        }

        // Statut
        if (statutCombo.getValue() == null) {
            markComboError(statutCombo, "Veuillez choisir un statut.");
            valid = false;
        } else {
            markComboOk(statutCombo);
        }

        // Lieu
        if (lieuField.getText() == null || lieuField.getText().trim().isEmpty()) {
            markError(lieuField, "Le lieu / emplacement est obligatoire.");
            valid = false;
        } else {
            markOk(lieuField);
        }

        // Date début
        if (dateDebutPicker.getValue() == null) {
            markDateError(dateDebutPicker, "La date de début est obligatoire.");
            valid = false;
        } else {
            markDateOk(dateDebutPicker);
        }

        // Date fin — maintenant obligatoire
        if (dateFinPicker.getValue() == null) {
            markDateError(dateFinPicker, "La date de fin est obligatoire.");
            valid = false;
        } else {
            markDateOk(dateFinPicker);
        }

        // Heure début
        if (heureDebutField.getText() == null || heureDebutField.getText().trim().isEmpty()) {
            markError(heureDebutField, "L'heure de début est obligatoire.");
            valid = false;
        } else {
            if (!validateTimeField(heureDebutField)) valid = false;
        }

        // Heure fin
        if (heureFinField.getText() == null || heureFinField.getText().trim().isEmpty()) {
            markError(heureFinField, "L'heure de fin est obligatoire.");
            valid = false;
        } else {
            if (!validateTimeField(heureFinField)) valid = false;
        }

        // Capacité
        if (capaciteField.getText() == null || capaciteField.getText().trim().isEmpty()) {
            markError(capaciteField, "La capacité est obligatoire.");
            valid = false;
        } else {
            try {
                int cap = Integer.parseInt(capaciteField.getText().trim());
                if (cap <= 0) {
                    markError(capaciteField, "La capacité doit être supérieure à 0.");
                    valid = false;
                } else {
                    markOk(capaciteField);
                }
            } catch (NumberFormatException e) {
                markError(capaciteField, "La capacité doit être un nombre entier.");
                valid = false;
            }
        }

        // Cohérence des dates
        if (valid && dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                markDateError(dateFinPicker, "La date de fin ne peut pas être avant la date de début.");
                valid = false;
            } else if (dateFinPicker.getValue().isEqual(dateDebutPicker.getValue())) {
                try {
                    LocalTime start = LocalTime.parse(heureDebutField.getText().trim());
                    LocalTime end   = LocalTime.parse(heureFinField.getText().trim());
                    if (!end.isAfter(start)) {
                        markError(heureFinField, "L'heure de fin doit être après l'heure de début.");
                        valid = false;
                    }
                } catch (DateTimeParseException ignored) {}
            }
        }

        return valid;
    }

    private boolean validateTimeField(TextField field) {
        try {
            LocalTime.parse(field.getText().trim());
            markOk(field);
            return true;
        } catch (DateTimeParseException e) {
            markError(field, "Format invalide — utilisez HH:mm (ex: 09:30).");
            return false;
        }
    }

    // ─── Visual Helpers ───────────────────────────────────────────────
    private void markError(TextField f, String msg) {
        f.setStyle(STYLE_ERROR);
        f.setTooltip(makeTooltip(msg));
        f.setPromptText("⚠ " + msg);
    }

    private void markOk(TextField f) {
        f.setStyle(STYLE_OK);
        f.setTooltip(null);
    }

    private void resetStyle(TextField f) {
        f.setStyle(STYLE_DEFAULT);
        f.setTooltip(null);
    }

    private void markComboError(ComboBox<?> cb, String msg) {
        cb.setStyle(STYLE_ERROR);
        cb.setTooltip(makeTooltip(msg));
    }

    private void markComboOk(ComboBox<?> cb) {
        cb.setStyle(STYLE_OK);
        cb.setTooltip(null);
    }

    private void resetComboStyle(ComboBox<?> cb) {
        cb.setStyle(STYLE_DEFAULT);
        cb.setTooltip(null);
    }

    private void markDateError(DatePicker dp, String msg) {
        dp.setStyle(STYLE_ERROR);
        dp.setTooltip(makeTooltip(msg));
    }

    private void markDateOk(DatePicker dp) {
        dp.setStyle(STYLE_OK);
        dp.setTooltip(null);
    }

    private void resetDateStyle(DatePicker dp) {
        dp.setStyle(STYLE_DEFAULT);
        dp.setTooltip(null);
    }

    private Tooltip makeTooltip(String msg) {
        Tooltip tip = new Tooltip("⚠ " + msg);
        tip.setFont(Font.font(13));
        return tip;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo((Stage) titreField.getScene().getWindow(),
                "/fxml/admin/evenements.fxml", "Gestion des Événements");
    }
}
