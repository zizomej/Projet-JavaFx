package com.learnhub.gestion_rdv_creneau.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    public static String selectedRole = "ADMIN";

    @FXML private VBox cardMedecin;
    @FXML private VBox cardStudent;
    @FXML private VBox cardAdmin;

    @FXML
    private void selectMedecin() {
        selectedRole = "MEDECIN";
        navigateToMain();
    }

    @FXML
    private void selectStudent() {
        selectedRole = "STUDENT";
        navigateToMain();
    }

    @FXML
    private void selectAdmin() {
        selectedRole = "ADMIN";
        navigateToMain();
    }

    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/gestion_rdv_creneau/view/Main.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cardMedecin.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



