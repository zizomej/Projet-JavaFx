package com.learnhub.medical.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.Node;

public class SuccessController {

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
