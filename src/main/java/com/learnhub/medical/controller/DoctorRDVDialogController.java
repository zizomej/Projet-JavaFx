package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class DoctorRDVDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMotif;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Creneau> comboCreneau;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextArea txtCompteRendu;
    @FXML private Button btnSave;

    private RDV rdv;
    private boolean saved = false;
    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();

    @FXML
    public void initialize() {
        comboStatut.setItems(FXCollections.observableArrayList(
            "En attente", "Confirmé", "Annulé", "Terminé"));
        
        try {
            comboCreneau.setItems(FXCollections.observableArrayList(creneauRepo.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRDV(RDV rdv) {
        this.rdv = rdv;
        if (rdv != null && rdv.getId() > 0) {
            lblTitle.setText("Modifier le Rendez-vous");
            txtMotif.setText(rdv.getMotif());
            txtDescription.setText(rdv.getDescription());
            datePicker.setValue(rdv.getDateDemande());
            comboStatut.setValue(rdv.getStatut());
            txtCompteRendu.setText(rdv.getCompteRendu());
            
            comboCreneau.getItems().stream()
                .filter(c -> c.getId() == rdv.getCreneauId())
                .findFirst().ifPresent(comboCreneau::setValue);
        } else {
            lblTitle.setText("Nouveau Rendez-vous");
            comboStatut.setValue("En attente");
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        try {
            if (rdv == null || rdv.getId() == 0) {
                // New RDV
                int studentId = rdvRepo.getFirstStudentId();
                System.out.println("[DEBUG] Création nouveau RDV avec Etudiant ID: " + studentId);
                
                RDV newRdv = new RDV(0, txtMotif.getText().trim(), txtDescription.getText().trim(),
                    datePicker.getValue(), comboStatut.getValue(),
                    txtCompteRendu.getText().trim(), "", studentId, comboCreneau.getValue().getId());
                rdvRepo.save(newRdv);
                showAlertSuccess("Création réussie", "Le nouveau rendez-vous a été ajouté avec succès.");
            } else {
                // Update RDV
                System.out.println("[DEBUG] Mise à jour RDV ID: " + rdv.getId());
                rdv.setMotif(txtMotif.getText().trim());
                rdv.setDescription(txtDescription.getText().trim());
                rdv.setDateDemande(datePicker.getValue());
                rdv.setStatut(comboStatut.getValue());
                rdv.setCompteRendu(txtCompteRendu.getText().trim());
                rdv.setCreneauId(comboCreneau.getValue().getId());
                rdvRepo.update(rdv);
                showAlertSuccess("Modification réussie", "Les changements ont été enregistrés.");
            }
            
            saved = true;
            closeStage();
        } catch (Exception e) {
            System.err.println("[ERROR] Echec de la sauvegarde : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur critique", "L'opération a échoué.\nDétail : " + e.getMessage());
        }
    }

    private void showAlertSuccess(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private boolean validateForm() {
        StringBuilder sb = new StringBuilder();
        
        // 1. Mandatory fields & Data Types
        String motif = txtMotif.getText().trim();
        if (motif.isBlank()) {
            sb.append("- Le motif est obligatoire.\n");
        } else if (motif.length() < 3) {
            sb.append("- Le motif doit contenir au moins 3 caractères.\n");
        }
        
        if (datePicker.getValue() == null) sb.append("- La date du rendez-vous est obligatoire.\n");
        if (comboCreneau.getValue() == null) sb.append("- Le choix d'un créneau est obligatoire.\n");
        if (comboStatut.getValue() == null) sb.append("- Le statut est obligatoire.\n");

        // 2. Logic validation (Date)
        if (datePicker.getValue() != null && datePicker.getValue().isBefore(LocalDate.now())) {
            sb.append("- La date ne peut pas être dans le passé.\n");
        }
        
        // 3. Medical Logic Rule
        if ("Terminé".equalsIgnoreCase(comboStatut.getValue()) && txtCompteRendu.getText().trim().isBlank()) {
            sb.append("- Le Compte Rendu est obligatoire lorsque le statut est 'Terminé'.\n");
        }

        if (sb.length() > 0) {
            showAlert("Erreur de Validation", "Veuillez corriger les points suivants :\n" + sb.toString());
            return false;
        }

        // 3. Uniqueness check (Slot availability)
        try {
            int currentId = (rdv != null) ? rdv.getId() : 0;
            if (rdvRepo.isCreneauTaken(comboCreneau.getValue().getId(), datePicker.getValue(), currentId)) {
                showAlert("Conflit de calendrier", "Ce créneau est déjà réservé par un autre patient pour cette date. Veuillez en choisir un autre.");
                return false;
            }
        } catch (SQLException e) {
            showAlert("Erreur Système", "Impossible de vérifier la disponibilité du créneau.");
            return false;
        }

        return true;
    }

    private void closeStage() {
        ((Stage) txtMotif.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
