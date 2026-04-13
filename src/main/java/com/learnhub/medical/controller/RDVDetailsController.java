package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.medical.repository.RDVRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RDVDetailsController {

    @FXML private Label lblId, lblEtudiant, lblDate, lblCreneau;
    @FXML private Label lblMotif, lblDescription, lblStatus, lblCompteRendu, lblOrdonnance;
    @FXML private Button btnEdit, btnDelete;

    private RDV rdv;
    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
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
    }

    public void setViewOnlyMode(boolean viewOnly) {
        btnEdit.setVisible(!viewOnly);
        btnEdit.setManaged(!viewOnly);
        if (btnDelete != null) {
            btnDelete.setVisible(!viewOnly);
            btnDelete.setManaged(!viewOnly);
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

    public boolean isEditRequested() {
        return editRequested;
    }

    private void closeStage() {
        if (lblStatus.getScene() != null && lblStatus.getScene().getWindow() != null) {
            ((Stage) lblStatus.getScene().getWindow()).close();
        }
    }
}
