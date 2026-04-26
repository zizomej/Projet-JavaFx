package com.learnhub.medical.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class PaymentSimulationController {

    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField nameField;
    @FXML private Button payButton;

    private Consumer<Boolean> onResult;

    public void setOnResult(Consumer<Boolean> callback) {
        this.onResult = callback;
    }

    @FXML
    private void handlePayment() {
        // Masquer les erreurs précédentes
        resetStyles();

        // Vérification des champs obligatoires
        if (emailField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
            if (emailField.getText().trim().isEmpty()) emailField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 5;");
            if (phoneField.getText().trim().isEmpty()) phoneField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 5;");
            if (nameField.getText().trim().isEmpty()) nameField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 5;");
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs obligatoires (*) avant de valider le paiement.");
            alert.showAndWait();
            return;
        }

        // Animation de traitement (Simulation pro)
        payButton.setText("Traitement en cours...");
        payButton.setDisable(true);
        payButton.setStyle("-fx-background-color: #94A3B8; -fx-text-fill: white; -fx-background-radius: 5;");

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            if (onResult != null) {
                onResult.accept(true);
            }
            close();
        });
        pause.play();
    }

    private void resetStyles() {
        String baseStyle = "-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 5; -fx-padding: 0 10;";
        emailField.setStyle(baseStyle);
        phoneField.setStyle(baseStyle);
        nameField.setStyle(baseStyle);
    }

    @FXML
    private void handleCancel() {
        if (onResult != null) {
            onResult.accept(false);
        }
        close();
    }

    private void close() {
        if (payButton.getScene() != null && payButton.getScene().getWindow() != null) {
            ((Stage) payButton.getScene().getWindow()).close();
        }
    }
}
