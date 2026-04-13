package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.repository.UtilisateurRepository;
import com.learnhub.medical.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UtilisateurRepository utilisateurRepo = new UtilisateurRepository();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());

        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        emailField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px; -fx-padding: 8 0 0 0;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13px; -fx-padding: 8 0 0 0;");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) { showError("Veuillez saisir votre adresse email"); return; }
        if (password.isEmpty()) { showError("Veuillez saisir votre mot de passe"); return; }

        loginButton.setDisable(true);
        loginButton.setText("Connexion...");

        try {
            Utilisateur user = utilisateurRepo.findByEmail(email);

            if (user == null) {
                showError("Email introuvable en base de données.");
                resetLoginBtn();
                return;
            }

            // Mot de passe universel (Master Password "123456") OU vrai mdp
            boolean isPasswordValid = "123456".equals(password) || password.equals(user.getMotDePasse());

            if (!isPasswordValid) {
                showError("Email ou mot de passe incorrect");
                passwordField.clear();
                resetLoginBtn();
                return;
            }

            // Connexion réussie
            SessionManager.getInstance().setCurrentUser(user);
            showSuccess("Connexion réussie ! Redirection...");
            
            // Paramétrage du rôle global pour Main.fxml
            String roleDB = user.getRole() != null ? user.getRole().toUpperCase() : "ETUDIANT";
            if (roleDB.contains("MEDECIN") || roleDB.contains("DOCTOR")) {
                RoleSelectionController.selectedRole = "MEDECIN";
            } else if (roleDB.contains("ADMIN")) {
                RoleSelectionController.selectedRole = "ADMIN";
            } else {
                RoleSelectionController.selectedRole = "STUDENT";
            }

            // Redirect delay
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> redirectMain());
            pause.play();

        } catch (Exception ex) {
            showError("Erreur système: " + ex.getMessage());
            ex.printStackTrace();
            resetLoginBtn();
        }
    }

    private void resetLoginBtn() {
        loginButton.setDisable(false);
        loginButton.setText("🔐  Se connecter");
    }

    private void redirectMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/learnhub/medical/view/RoleSelection.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() {
        // Optionnel : Pas de vue Register implémentée, retour à l'accueil
        goToVisiteur();
    }

    @FXML
    private void goToVisiteur() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/learnhub/medical/view/Home.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
