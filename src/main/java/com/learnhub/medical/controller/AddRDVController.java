package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class AddRDVController {

    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtMotif;
    @FXML private TextArea  txtDescription;
    @FXML private TextField txtCIN;

    private StudentPlanningController parentController;
    private Creneau selectedCreneau;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            txtNom.setText(user.getFullName());
            txtEmail.setText(user.getEmail());
            txtCIN.setText(user.getCin() != null ? user.getCin() : "");
        }
    }

    public void setSessionData(StudentPlanningController parent, Creneau c) {
        this.parentController = parent;
        this.selectedCreneau = c;
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            RDV rdv = new RDV(0, 
                txtMotif.getText(), 
                txtDescription.getText(), 
                LocalDate.now(), 
                "En attente",
                null, 
                null, 
                userId, 
                selectedCreneau.getId());
                
            new RDVRepository().save(rdv);
            new CreneauRepository().updateAvailability(selectedCreneau.getId(), false);
            
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Succès");
            a.setHeaderText(null);
            a.setContentText("Votre rendez-vous a été enregistré avec succès !");
            a.showAndWait();

            if (parentController != null) parentController.refreshPlanning();
            close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de réserver : " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder sb = new StringBuilder();
        
        String nom = txtNom.getText().trim();
        String email = txtEmail.getText().trim();
        String motif = txtMotif.getText().trim();
        String desc = txtDescription.getText().trim();

        // Contrôle du Nom
        if (nom.isBlank()) {
            sb.append("- Votre nom est obligatoire.\n");
        } else if (nom.length() < 3) {
            sb.append("- Votre nom doit contenir au moins 3 caractères.\n");
        }

        // Contrôle de l'Email
        if (email.isBlank()) {
            sb.append("- Votre adresse email est obligatoire.\n");
        } else if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            sb.append("- Le format de l'adresse email est invalide (ex: nom@domaine.com).\n");
        }

        // Contrôle du Motif
        if (motif.isBlank()) {
            sb.append("- Le motif de consultation est obligatoire.\n");
        } else if (motif.length() < 3) {
            sb.append("- Le motif doit contenir au moins 3 caractères.\n");
        } else if (motif.matches("\\d+")) {
            sb.append("- Le motif ne peut pas contenir uniquement des chiffres.\n");
        }

        // Contrôle de la Description
        if (desc.isBlank()) {
            sb.append("- La description de vos symptômes est obligatoire.\n");
        } else if (desc.length() < 10) {
            sb.append("- La description doit être plus détaillée (au moins 10 caractères).\n");
        }

        if (sb.length() > 0) {
            showAlert("Formulaire incomplet", "Veuillez corriger les points suivants :\n" + sb.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) txtMotif.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
