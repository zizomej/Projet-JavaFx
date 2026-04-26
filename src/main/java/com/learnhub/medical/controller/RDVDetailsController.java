package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.medical.repository.RDVRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.learnhub.medical.util.PenaltyService;
import com.learnhub.medical.util.CancellationTooLateException;
import javafx.application.Platform;

import java.sql.SQLException;

public class RDVDetailsController {

    @FXML private Label lblId, lblEtudiant, lblDate, lblCreneau;
    @FXML private Label lblMotif, lblDescription, lblStatus, lblCompteRendu, lblOrdonnance;
    @FXML private Button btnEdit, btnDelete, btnCancel;

    private RDV rdv;
    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final PenaltyService penaltyService = new PenaltyService();
    private boolean editRequested = false;

    public void setRDV(RDV rdv) {
        this.rdv = rdv;
        lblId.setText(String.valueOf(rdv.getId()));
        lblEtudiant.setText(rdv.getStudentName() != null ? rdv.getStudentName() : "Non assigné");
        lblDate.setText(rdv.getDateDemande() != null ? rdv.getDateDemande().toString() : "--");
        lblMotif.setText(rdv.getMotif());
        lblDescription.setText(rdv.getDescription());
        lblStatus.setText(rdv.getStatut());
        lblCompteRendu.setText(rdv.getCompteRendu() != null && !rdv.getCompteRendu().trim().isEmpty() ? rdv.getCompteRendu() : "Aucun");
        lblOrdonnance.setText(rdv.getOrdonnanceUrl() != null && !rdv.getOrdonnanceUrl().trim().isEmpty() ? rdv.getOrdonnanceUrl() : "-");

        try {
            Creneau c = creneauRepo.findById(rdv.getCreneauId());
            if (c != null) {
                lblCreneau.setText(c.getJour() + " à " + c.getHeure());
            }
        } catch (SQLException e) {
            lblCreneau.setText("Créneau #" + rdv.getCreneauId());
        }

        // Colorize status badge
        String status = rdv.getStatut() != null ? rdv.getStatut() : "";
        if (status.equalsIgnoreCase("Confirmé") || status.equalsIgnoreCase("Accepté")) {
            lblStatus.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 4 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else if (status.equalsIgnoreCase("Terminé")) {
            lblStatus.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 4 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else if (status.equalsIgnoreCase("Annulé")) {
            lblStatus.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 4 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        } else {
            lblStatus.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 4 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        }

        // Hide cancel button if already cancelled or finished
        if (status.contains("CANCELLED") || status.equalsIgnoreCase("Annulé") || status.equalsIgnoreCase("Terminé")) {
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
        }
    }

    public void setViewOnlyMode(boolean viewOnly) {
        btnEdit.setVisible(!viewOnly);
        btnEdit.setManaged(!viewOnly);
        if (btnDelete != null) {
            btnDelete.setVisible(!viewOnly);
            btnDelete.setManaged(!viewOnly);
        }
        // Le bouton annuler est visible pour les étudiants (viewOnly=true)
        if (btnCancel != null) {
            btnCancel.setVisible(viewOnly);
            btnCancel.setManaged(viewOnly);
        }
    }

    @FXML
    private void handleBack() {
        closeStage();
    }

    @FXML
    private void handleEdit() {
        editRequested = true;
        closeStage();
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                rdvRepo.delete(rdv.getId());
                closeStage();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Voulez-vous vraiment annuler ce rendez-vous ?\n\nNote : Des pénalités peuvent s'appliquer si l'annulation est tardive.", 
            ButtonType.YES, ButtonType.NO);
            
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                String message = penaltyService.processCancellation(rdv.getId());
                showInfo("Annulation Réussie", message);
                closeStage();
            } catch (CancellationTooLateException e) {
                showAlert("Action Interdite", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur est survenue lors de l'annulation : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDownloadPDF() {
        new Thread(() -> {
            try {
                String status = rdv.getStatut() != null ? rdv.getStatut() : "";
                String type = status.equalsIgnoreCase("Terminé") ? "Ordonnance" : "Recu";
                String fileName = type + "_Medical_" + rdv.getId() + "_" + System.currentTimeMillis() + ".pdf";
                String path = System.getProperty("user.home") + java.io.File.separator + fileName;
                java.io.File file = new java.io.File(path);

                if (type.equals("Ordonnance")) {
                    com.learnhub.medical.util.PdfService.generatePrescription(rdv, rdv.getCompteRendu(), path);
                } else {
                    com.learnhub.medical.util.PdfService.generateReceipt(rdv, path);
                }
                
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Réussi");
                    alert.setHeaderText("Document généré !");
                    alert.setContentText("Le fichier (" + type + ") a été ouvert et enregistré dans votre dossier utilisateur.");
                    alert.show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de génération");
                    alert.setContentText("Détails : " + e.getMessage());
                    alert.show();
                });
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public boolean isEditRequested() {
        return editRequested;
    }

    private void closeStage() {
        if (lblStatus.getScene() != null && lblStatus.getScene().getWindow() != null) {
            ((Stage) lblStatus.getScene().getWindow()).close();
        }
    }
}
