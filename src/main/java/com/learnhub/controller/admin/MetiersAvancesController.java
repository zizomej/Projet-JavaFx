package com.learnhub.controller.admin;

import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.Node;

public class MetiersAvancesController {

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo(getStage(), "/fxml/admin/dashboard.fxml", "Tableau de Bord");
    }

    @FXML
    private void goEvenements() {
        NavigationUtil.navigateTo(getStage(), "/fxml/admin/evenements.fxml", "Gestion des Événements");
    }

    private Stage getStage() {
        // Method to get current stage from any existing controller context
        // Here we can use any node in the future if needed, but navigateTo handles it
        return (Stage) Stage.getWindows().stream().filter(w -> w.isFocused()).findFirst().orElse(null);
    }
    
    // In case goBack/goEvenements are called from FXML, we need to ensure the stage is correctly retrieved
    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/dashboard.fxml", "Tableau de Bord");
    }
    
    @FXML
    private void handleEvenements(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/evenements.fxml", "Gestion des Événements");
    }
}
