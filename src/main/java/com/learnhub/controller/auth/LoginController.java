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
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Validation en temps réel
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());

        // Connexion avec la touche Entrée
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
        emailField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
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

        // Validation des champs
        if (email.isEmpty()) {
            showError("Veuillez saisir votre adresse email");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Veuillez saisir votre mot de passe");
            passwordField.requestFocus();
            return;
        }

        // Désactiver le bouton pendant la connexion
        loginButton.setDisable(true);
        loginButton.setText("Connexion...");

        try {
            Utilisateur user = utilisateurDAO.findByEmail(email);

            if (user == null) {
                showError("Email ou mot de passe incorrect");
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
                return;
            }

            // Vérification du mot de passe avec BCrypt
            String hashed = user.getPassword();

            if (hashed != null && hashed.startsWith("$2y$")) {
                hashed = "$2a$" + hashed.substring(4);
            }

            if (!BCrypt.checkpw(password, hashed)) {
                showError("Email ou mot de passe incorrect");
                passwordField.clear();
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
                passwordField.requestFocus();
                return;
            }

            if (!user.isActif()) {
                showError("Votre compte est désactivé. Contactez l'administrateur.");
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
                return;
            }

            // Connexion réussie
            SessionManager.getInstance().setCurrentUser(user);

            // Afficher le rôle pour déboguer
            System.out.println("Connexion réussie pour: " + email);
            System.out.println("Rôle: " + user.getRoles());

            showSuccess("Connexion réussie ! Redirection en cours...");

            // Redirection après un court délai
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(event -> {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                NavigationUtil.redirectByRole(stage);
            });
            pause.play();

        } catch (Exception ex) {
            showError("Erreur de connexion: " + ex.getMessage());
            ex.printStackTrace();
            loginButton.setDisable(false);
            loginButton.setText("Se connecter");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    @FXML
    private void goToRegister() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/register.fxml", "Inscription");
    }

    @FXML
    private void goToVisiteur() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/visiteur/home.fxml", "Accueil");
    }
}
