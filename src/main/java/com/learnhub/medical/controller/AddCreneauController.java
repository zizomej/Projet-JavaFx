package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;

public class AddCreneauController {

    @FXML private DatePicker      datePicker;
    @FXML private ComboBox<String> comboHeure;
    @FXML private ComboBox<String> comboMinute;
    @FXML private ComboBox<String> comboRecurrence;
    @FXML private CheckBox        checkDisponibilite;
    @FXML private Label           lblTitle;
    @FXML private Label           lblSubtitle;
    @FXML private Button          btnSave;
    @FXML private Button          btnDelete;
    @FXML private Button          btnRetour;

    private ICreneauRefreshing parentController;
    private Creneau editingCreneau = null;

    @FXML
    public void initialize() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 8; i <= 18; i++) hours.add(String.format("%02d", i));
        comboHeure.setItems(hours);
        comboHeure.setValue("09");

        comboMinute.setItems(FXCollections.observableArrayList("00", "15", "30", "45"));
        comboMinute.setValue("00");

        comboRecurrence.setItems(FXCollections.observableArrayList(
            "Une seule fois", "Hebdomadaire", "Mensuelle"));
        comboRecurrence.setValue("Une seule fois");

        checkDisponibilite.setSelected(true);

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        datePicker.setValue(LocalDate.now());
        
        // Default mode: Add
        lblTitle.setText("Nouveau Créneau");
        lblSubtitle.setText("Définissez une nouvelle plage horaire disponible");
        btnSave.setText("💾 Enregistrer le Créneau");
        if (btnDelete != null) btnDelete.setVisible(false);
    }

    public void setParentController(ICreneauRefreshing ctrl) {
        this.parentController = ctrl;
    }

    public void setEditData(Creneau c) {
        this.editingCreneau = c;
        lblTitle.setText("Modifier le Créneau");
        lblSubtitle.setText("Mettre à jour les informations du créneau");
        btnSave.setText("💾 Mettre à jour");
        if (btnDelete != null) btnDelete.setVisible(true);

        datePicker.setValue(LocalDate.parse(c.getJour()));
        comboHeure.setValue(String.format("%02d", c.getHeure().getHour()));
        
        String minuteStr = String.format("%02d", c.getHeure().getMinute());
        if (!comboMinute.getItems().contains(minuteStr)) {
            comboMinute.getItems().add(minuteStr);
            FXCollections.sort(comboMinute.getItems());
        }
        comboMinute.setValue(minuteStr);
        
        comboRecurrence.setValue(c.getRecurrence());
        checkDisponibilite.setSelected(c.isDisponibilite());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        try {
            LocalTime time = LocalTime.of(
                Integer.parseInt(comboHeure.getValue()),
                Integer.parseInt(comboMinute.getValue()));
            
            String jourStr = datePicker.getValue().toString();
            CreneauRepository repo = new CreneauRepository();

            // Unique test: Check if slot already exists
            int currentId = (editingCreneau != null) ? editingCreneau.getId() : 0;
            if (repo.exists(jourStr, time, currentId)) {
                showAlert("Ce créneau existe déjà pour cette date et cet horaire. Veuillez en choisir un autre.");
                return;
            }

            if (editingCreneau == null) {
                // Mode Ajout
                Creneau newC = new Creneau(0, jourStr, time, comboRecurrence.getValue(), checkDisponibilite.isSelected());
                repo.save(newC);
                showAlertSuccess("Créneau créé avec succès !");
            } else {
                // Mode Modification
                editingCreneau.setJour(jourStr);
                editingCreneau.setHeure(time);
                editingCreneau.setRecurrence(comboRecurrence.getValue());
                editingCreneau.setDisponibilite(checkDisponibilite.isSelected());
                repo.update(editingCreneau);
                showAlertSuccess("Créneau mis à jour !");
            }

            if (parentController != null) parentController.refreshPlanning();
            close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (editingCreneau == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Voulez-vous vraiment supprimer ce créneau ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                new CreneauRepository().delete(editingCreneau.getId());
                showAlertSuccess("Créneau supprimé !");
                if (parentController != null) parentController.refreshPlanning();
                close();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        if (datePicker.getValue() == null) {
            showAlert("Veuillez choisir une date."); return false;
        }
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("La date ne peut pas être dans le passé."); return false;
        }
        if (comboHeure.getValue() == null || comboMinute.getValue() == null) {
            showAlert("Veuillez sélectionner une heure."); return false;
        }
        return true;
    }

    private void showAlertSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    @FXML private void handleCancel() { close(); }

    private void close() {
        ((Stage) datePicker.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validation"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
