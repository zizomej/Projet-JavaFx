package com.learnhub.controller.auth;

import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleBox;
    @FXML private TextField telephoneField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button registerButton;

    private final UtilisateurDAO dao = new UtilisateurDAO();

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("ROLE_ETUDIANT", "ROLE_PARENT", "ROLE_PROFESSEUR", "ROLE_MEDECIN");
        roleBox.setValue("ROLE_ETUDIANT");
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String role = roleBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }
        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!email.contains("@")) {
            showError("Email invalide.");
            return;
        }

        try {
            if (dao.emailExists(email)) {
                showError("Cet email est déjà utilisé.");
                return;
            }

            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            // Hash password with BCrypt
            String hashed = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, password.toCharArray());
            user.setPassword(hashed);
            user.setRoles(role);
            user.setTelephone(telephoneField.getText().trim());
            user.setActif(true);

            dao.insert(user);
            errorLabel.setVisible(false);
            successLabel.setText("Compte créé avec succès ! Vous pouvez vous connecter.");
            successLabel.setVisible(true);
            clearFields();

        } catch (Exception ex) {
            showError("Erreur lors de l'inscription: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void clearFields() {
        nomField.clear(); prenomField.clear(); emailField.clear();
        passwordField.clear(); confirmPasswordField.clear(); telephoneField.clear();
    }
}
