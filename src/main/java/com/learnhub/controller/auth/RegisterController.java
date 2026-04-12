package com.learnhub.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private RadioButton etudiantRadio;
    @FXML private RadioButton professeurRadio;
    @FXML private RadioButton adminRadio;
    @FXML private ToggleGroup accountTypeGroup;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private TextField telephoneField;

    @FXML private VBox filiereContainer;
    @FXML private ComboBox<String> filiereCombo;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label errorLabel;

    private int currentStep = 1;

    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private VBox step3Container;
    @FXML private Label step1Label;
    @FXML private Label step2Label;
    @FXML private Label step3Label;
    @FXML private StackPane step1Badge;
    @FXML private StackPane step2Badge;
    @FXML private StackPane step3Badge;
    @FXML private Region stepLine1;
    @FXML private Region stepLine2;
    @FXML private VBox etudiantCard;
    @FXML private VBox professeurCard;
    @FXML private VBox adminCard;

    @FXML
    public void initialize() {
        updateStepsVisibility();

        filiereContainer.setVisible(false);
        filiereContainer.setManaged(false);

        accountTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == etudiantRadio) {
                filiereContainer.setVisible(true);
                filiereContainer.setManaged(true);
            } else {
                filiereContainer.setVisible(false);
                filiereContainer.setManaged(false);
            }
            updateRoleCards();
        });

        filiereCombo.getItems().addAll(
                "Informatique",
                "Genie Civil",
                "Genie Mecanique",
                "Genie Electrique",
                "Marketing",
                "Finance",
                "Medecine",
                "Droit"
        );

        updateRoleCards();
    }

    @FXML
    private void nextStep() {
        switch (currentStep) {
            case 1:
                if (validateStep1()) {
                    currentStep = 2;
                    updateStepsVisibility();
                }
                break;
            case 2:
                if (validateStep2()) {
                    currentStep = 3;
                    updateStepsVisibility();
                }
                break;
            case 3:
                handleRegister();
                break;
            default:
                break;
        }
    }

    @FXML
    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateStepsVisibility();
        }
    }

    private void updateStepsVisibility() {
        if (step1Container != null) {
            boolean active = currentStep == 1;
            step1Container.setVisible(active);
            step1Container.setManaged(active);
        }
        if (step2Container != null) {
            boolean active = currentStep == 2;
            step2Container.setVisible(active);
            step2Container.setManaged(active);
        }
        if (step3Container != null) {
            boolean active = currentStep == 3;
            step3Container.setVisible(active);
            step3Container.setManaged(active);
        }

        if (step1Label != null) {
            step1Label.setStyle(currentStep == 1
                    ? "-fx-text-fill: #facc15; -fx-font-weight: bold;"
                    : "-fx-text-fill: rgba(255,255,255,0.72);");
        }
        if (step2Label != null) {
            step2Label.setStyle(currentStep == 2
                    ? "-fx-text-fill: #facc15; -fx-font-weight: bold;"
                    : "-fx-text-fill: rgba(255,255,255,0.72);");
        }
        if (step3Label != null) {
            step3Label.setStyle(currentStep == 3
                    ? "-fx-text-fill: #facc15; -fx-font-weight: bold;"
                    : "-fx-text-fill: rgba(255,255,255,0.72);");
        }

        if (step1Badge != null) {
            step1Badge.setStyle(currentStep >= 1
                    ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FFC107, #FF6F61); -fx-background-radius: 999;"
                    : "-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 999;");
        }
        if (step2Badge != null) {
            step2Badge.setStyle(currentStep >= 2
                    ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FFC107, #FF6F61); -fx-background-radius: 999;"
                    : "-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 999;");
        }
        if (step3Badge != null) {
            step3Badge.setStyle(currentStep >= 3
                    ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FFC107, #FF6F61); -fx-background-radius: 999;"
                    : "-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 999;");
        }
        if (stepLine1 != null) {
            stepLine1.setStyle(currentStep >= 2
                    ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #FFC107, #FF6F61); -fx-background-radius: 999;"
                    : "-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 999;");
        }
        if (stepLine2 != null) {
            stepLine2.setStyle(currentStep >= 3
                    ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #FFC107, #FF6F61); -fx-background-radius: 999;"
                    : "-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 999;");
        }
    }

    private void updateRoleCards() {
        if (etudiantCard != null) etudiantCard.getStyleClass().remove("auth-role-card-active");
        if (professeurCard != null) professeurCard.getStyleClass().remove("auth-role-card-active");
        if (adminCard != null) adminCard.getStyleClass().remove("auth-role-card-active");

        if (accountTypeGroup == null || accountTypeGroup.getSelectedToggle() == null) {
            return;
        }

        if (accountTypeGroup.getSelectedToggle() == etudiantRadio && etudiantCard != null) {
            etudiantCard.getStyleClass().add("auth-role-card-active");
        } else if (accountTypeGroup.getSelectedToggle() == professeurRadio && professeurCard != null) {
            professeurCard.getStyleClass().add("auth-role-card-active");
        } else if (accountTypeGroup.getSelectedToggle() == adminRadio && adminCard != null) {
            adminCard.getStyleClass().add("auth-role-card-active");
        }
    }

    private boolean validateStep1() {
        RadioButton selected = (RadioButton) accountTypeGroup.getSelectedToggle();
        if (selected == null) {
            showError("Veuillez selectionner un type de compte");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();

        if (nom.isEmpty() || prenom.isEmpty()) {
            showError("Veuillez entrer votre nom et prenom");
            return false;
        }

        RadioButton selected = (RadioButton) accountTypeGroup.getSelectedToggle();
        if (selected == etudiantRadio && (filiereCombo.getValue() == null || filiereCombo.getValue().isEmpty())) {
            showError("Veuillez selectionner une filiere");
            return false;
        }

        return true;
    }

    @FXML
    private void handleRegister() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caracteres");
            return;
        }

        RadioButton selected = (RadioButton) accountTypeGroup.getSelectedToggle();
        if (selected == null) {
            showError("Veuillez selectionner un type de compte");
            return;
        }

        String accountType = selected.getText();
        if (accountType.equals("Etudiant") && (filiereCombo.getValue() == null || filiereCombo.getValue().isEmpty())) {
            showError("Veuillez selectionner une filiere");
            return;
        }

        System.out.println("=== INSCRIPTION REUSSIE ===");
        System.out.println("Nom: " + nom);
        System.out.println("Prenom: " + prenom);
        System.out.println("CIN: " + cinField.getText());
        System.out.println("Telephone: " + telephoneField.getText());
        System.out.println("Email: " + email);
        System.out.println("Type de compte: " + accountType);
        if (accountType.equals("Etudiant")) {
            System.out.println("Filiere: " + filiereCombo.getValue());
        }

        goToLogin();
    }

    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            com.learnhub.util.NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page de connexion");
        }
    }

    @FXML
    private void goToHome() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            com.learnhub.util.NavigationUtil.navigateTo(stage, "/fxml/visiteur/home.fxml", "Accueil");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
