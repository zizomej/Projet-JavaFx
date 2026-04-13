package com.learnhub.medical.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML private Button btnLogin;

    @FXML
    private void goLogin() {
        navigateTo("/com/learnhub/medical/view/Login.fxml", "Connexion à LearnHub");
    }

    @FXML
    private void goHome() {
        System.out.println("Déjà sur la page d'accueil.");
    }

    @FXML
    private void goPrograms() {
        System.out.println("Section Programmes (Mise en attente).");
    }

    @FXML
    private void goEvents() {
        System.out.println("Section Événements (Mise en attente).");
    }

    @FXML
    private void goPartners() {
        System.out.println("Section Partenaires (Mise en attente).");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = Stage.getWindows().stream()
                .filter(w -> w.isShowing() && w instanceof Stage)
                .map(w -> (Stage) w)
                .findFirst()
                .orElse(null);

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle(title);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossible de charger " + fxmlPath);
        }
    }
}
