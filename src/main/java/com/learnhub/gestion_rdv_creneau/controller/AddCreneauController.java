package com.learnhub.gestion_rdv_creneau.controller;

import com.learnhub.gestion_rdv_creneau.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

public class AddCreneauController {

    @FXML private DatePicker datePicker;
    @FXML private TextField txtHeure;
    @FXML private TextField txtRecurrence;
    @FXML private CheckBox checkDisponibilite;

    private CreneauManagementController parentController;

    public void setParentController(CreneauManagementController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSave() {
        if (datePicker.getValue() == null || txtHeure.getText().isEmpty()) {
            showAlert("Validation", "La date et l'heure sont obligatoires !");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO creneau (jour, heure, recurrence, disponibilite) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            // Format date as string YYYY-MM-DD for simpler parsing later in the dynamic grid
            pstmt.setString(1, datePicker.getValue().toString());
            
            // Parse time
            String timeStr = txtHeure.getText();
            if (!timeStr.contains(":")) timeStr += ":00";
            pstmt.setTime(2, Time.valueOf(LocalTime.parse(timeStr)));
            
            pstmt.setString(3, txtRecurrence.getText());
            pstmt.setBoolean(4, checkDisponibilite.isSelected());
            
            pstmt.executeUpdate();
            
            if (parentController != null) {
                parentController.refreshPlanning();
            }
            
            close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer le créneau : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
