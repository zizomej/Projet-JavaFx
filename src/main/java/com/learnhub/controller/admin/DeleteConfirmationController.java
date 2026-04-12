package com.learnhub.controller.admin;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class DeleteConfirmationController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private VBox detailsBox;

    private boolean confirmed = false;
    private Stage stage;

    public void initData(Stage stage, String itemType, String explicitMessage, Map<String, String> details) {
        this.stage = stage;
        
        if (explicitMessage == null || explicitMessage.isBlank()) {
            messageLabel.setText("Êtes-vous sûr de vouloir supprimer cette " + itemType + " ? Cette action est irréversible.");
        } else {
            messageLabel.setText(explicitMessage);
        }

        detailsBox.getChildren().clear();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : details.entrySet()) {
            if (!first) {
                Region separator = new Region();
                separator.setStyle("-fx-border-color: #fca5a5 transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-border-style: dotted;");
                separator.setMinHeight(1);
                detailsBox.getChildren().add(separator);
            }
            
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label keyLabel = new Label(entry.getKey() + " :");
            keyLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label valueLabel = new Label(entry.getValue());
            valueLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-alignment: right;");

            row.getChildren().addAll(keyLabel, spacer, valueLabel);
            detailsBox.getChildren().add(row);
            
            first = false;
        }
    }

    @FXML
    private void handleConfirm() {
        confirmed = true;
        if (stage != null) stage.close();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        if (stage != null) stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
