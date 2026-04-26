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

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtMotif;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtCIN;
    @FXML
    private TextField txtTel;

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
        if (!validateForm())
            return;

        try {
            // Étape 1 : Préparation du paiement
            com.learnhub.medical.util.StripeIntegration stripe = new com.learnhub.medical.util.StripeIntegration();

            // On récupère l'URL de paiement (Simulé ou via API)
            // Pour la démo, on utilise l'URL simulée car le backend sans Stripe renverrait
            // 404
            String checkoutUrl = stripe.getMockCheckoutUrl();

            // Étape 2 : Ouverture de la fenêtre de paiement NATIVE (Intégrée) - 100% JavaFX
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/PaymentSimulationView.fxml"));
            javafx.scene.Parent root = loader.load();
            PaymentSimulationController ctrl = loader.getController();

            javafx.stage.Stage paymentStage = new javafx.stage.Stage();
            paymentStage.setTitle("Stripe Checkout");
            paymentStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            paymentStage.setScene(new javafx.scene.Scene(root));

            ctrl.setOnResult((Boolean success) -> {
                if (success) {
                    System.out.println(">>> Paiement validé, lancement de l'enregistrement...");
                    javafx.application.Platform.runLater(() -> processFinalRegistration());
                } else {
                    showAlert("Paiement Annulé", "Le paiement a été interrompu.");
                }
            });

            paymentStage.show();

        } catch (Exception e) {
            System.err.println("ERREUR CHARGEMENT PAIEMENT: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le module de paiement intégré.");
        }
    }

    private void processFinalRegistration() {
        try {
            System.out.println(">>> Enregistrement en base de données...");
            int userId = com.learnhub.medical.util.SessionManager.getInstance().getCurrentUserId();
            LocalDate rdvDate = LocalDate.now();
            try {
                if (selectedCreneau.getJour() != null && selectedCreneau.getJour().matches("\\d{4}-\\d{2}-\\d{2}")) {
                    rdvDate = LocalDate.parse(selectedCreneau.getJour());
                }
            } catch (Exception e) { System.out.println("Format date personnalisé..."); }

            RDV rdv = new RDV(0, txtMotif.getText(), txtDescription.getText(), rdvDate, 
                             "Payé", null, null, userId, selectedCreneau.getId());

            new RDVRepository().save(rdv);
            new CreneauRepository().updateAvailability(selectedCreneau.getId(), false);

            // Étape 5 : Déclenchement des Notifications Réelles (Sans Mot de Passe)
            String targetTel = txtTel.getText().trim();
            new com.learnhub.medical.util.TwilioWhatsAppService().sendWhatsAppConfirmation(targetTel, txtNom.getText(), rdvDate.toString(), selectedCreneau.getHeure().toString());
            
            String targetEmail = txtEmail.getText().trim();
            System.out.println(">>> TENTATIVE D'ENVOI MAIL VERS : [" + targetEmail + "]");
            
            String detailedInfo = "Jour : " + rdvDate + " 🕒 Heure : " + selectedCreneau.getHeure();
            new com.learnhub.medical.util.MailService().sendConfirmationEmail(targetEmail, txtNom.getText(), detailedInfo);

            System.out.println(">>> Affichage fenêtre de succès...");
            javafx.fxml.FXMLLoader sLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/SuccessView.fxml"));
            javafx.scene.Parent sRoot = sLoader.load();
            javafx.stage.Stage sStage = new javafx.stage.Stage();
            sStage.initStyle(javafx.stage.StageStyle.UNDECORATED); 
            sStage.setScene(new javafx.scene.Scene(sRoot));
            sStage.showAndWait(); // On attend la fermeture pour montrer le reçu

            System.out.println(">>> Affichage du reçu définitif...");
            javafx.fxml.FXMLLoader rLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/RecuView.fxml"));
            javafx.scene.Parent rRoot = rLoader.load();
            RecuController rCtrl = rLoader.getController();
            rCtrl.setData(rdv);
            javafx.stage.Stage rStage = new javafx.stage.Stage();
            rStage.setTitle("Récupération de mon Reçu - LearnHub Medical");
            rStage.setScene(new javafx.scene.Scene(rRoot));
            rStage.show();

            if (parentController != null) parentController.refreshPlanning();
            close();
        } catch (Exception e) {
            System.err.println("ERREUR FINALE: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Détail de l'erreur : " + e.getMessage());
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

        // Contrôle du Téléphone
        String tel = txtTel.getText().trim();
        if (tel.isBlank()) {
            sb.append("- Le numéro de téléphone est obligatoire.\n");
        } else if (!tel.startsWith("+") || tel.length() < 10) {
            sb.append("- Le téléphone doit être au format international (ex: +21628796935).\n");
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
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
