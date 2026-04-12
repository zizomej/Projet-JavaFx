package com.learnhub.controller.auth;

import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur user = utilisateurDAO.findByEmail(email);
            if (user == null) {
                showError("Email ou mot de passe incorrect.");
                return;
            }
            // Simple password check (in production use BCrypt)
            if (!checkPassword(password, user.getPassword())) {
                showError("Email ou mot de passe incorrect.");
                return;
            }
            if (!user.isActif()) {
                showError("Votre compte est désactivé. Contactez l'administrateur.");
                return;
            }

            SessionManager.getInstance().setCurrentUser(user);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            NavigationUtil.redirectByRole(stage);

        } catch (Exception ex) {
            showError("Erreur de connexion: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() {
        Stage stage = (Stage) (loginButton != null ? loginButton.getScene().getWindow() : emailField.getScene().getWindow());
        NavigationUtil.navigateTo(stage, "/fxml/auth/register.fxml", "Inscription");
    }

    @FXML
    private void goToVisiteur() {
        Stage stage = (Stage) (loginButton != null ? loginButton.getScene().getWindow() : emailField.getScene().getWindow());
        NavigationUtil.navigateTo(stage, "/fxml/visiteur/home.fxml", "Accueil");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Accepts both plain-text passwords (demo data) and BCrypt hashes.
     */
    private boolean checkPassword(String plaintext, String stored) {
        if (stored == null) return false;
        // BCrypt hashes always start with $2
        if (stored.startsWith("$2")) {
            try {
                return at.favre.lib.crypto.bcrypt.BCrypt.verifyer()
                        .verify(plaintext.toCharArray(), stored.toCharArray()).verified;
            } catch (Exception e) {
                return false;
            }
        }
        // Fallback: plain-text comparison (demo accounts in database.sql)
        return stored.equals(plaintext);
    }
}
