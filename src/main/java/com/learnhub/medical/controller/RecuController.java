package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class RecuController {

    @FXML private Label lblReference, lblDateEmission, lblPatientNom, lblPatientEmail, lblMotif, lblRdvDate;
    @FXML private Button btnEnregistrer;

    private RDV rdv;

    public void setData(RDV rdv) {
        this.rdv = rdv;
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        
        lblReference.setText("Référence: #" + rdv.getId() + "-2026");
        lblDateEmission.setText("Émis le: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        String fullName = (user != null) ? user.getFullName() : "Tasnim Araar";
        String email = (user != null) ? user.getEmail() : "araartasnim7@gmail.com";
        
        lblPatientNom.setText(fullName);
        lblPatientEmail.setText(email);
        lblMotif.setText("Motif: " + rdv.getMotif());
        lblRdvDate.setText("Date du RDV: " + rdv.getDateDemande());
    }

    @FXML
    private void handleExportPDF() {
        btnEnregistrer.setText("⏳ Génération...");
        btnEnregistrer.setDisable(true);

        new Thread(() -> {
            try {
                // Nom unique avec Timestamp pour éviter les conflits
                String fileName = "Recu_Medical_" + rdv.getId() + "_" + System.currentTimeMillis() + ".pdf";
                String path = System.getProperty("user.home") + File.separator + fileName;
                
                com.learnhub.medical.util.PdfService.generateReceipt(rdv, path);
                
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(new File(path));
                }

                Platform.runLater(() -> {
                    btnEnregistrer.setText("Enregistrer en PDF");
                    btnEnregistrer.setDisable(false);
                    showSimpleAlert("Succès", "Votre reçu a été enregistré dans votre dossier utilisateur :\n" + fileName);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    btnEnregistrer.setText("Enregistrer en PDF");
                    btnEnregistrer.setDisable(false);
                    showSimpleAlert("Erreur", "Impossible de sauvegarder le PDF. Vérifiez qu'il n'est pas déjà ouvert.\nErreur : " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleClose() {
        ((Stage) btnEnregistrer.getScene().getWindow()).close();
    }

    private void showSimpleAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
