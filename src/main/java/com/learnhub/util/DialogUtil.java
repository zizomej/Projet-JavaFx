package com.learnhub.util;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialogUtil {

    public static boolean showDeleteConfirmation(String entityType, String entityName) {
        AtomicBoolean confirmed = new AtomicBoolean(false);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        // Container applying rounded corners uniformly via CSS
        VBox root = new VBox();
        root.setStyle("-fx-background-color: transparent; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        root.setPrefWidth(400);

        VBox roundedContainer = new VBox();
        roundedContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        // Header (Red)
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 12 12 0 0; -fx-padding: 25;");
        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 36px; -fx-text-fill: #fcd34d;");
        Label title = new Label("Confirmer la suppression");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");
        header.getChildren().addAll(warningIcon, title);

        // Body (White)
        VBox body = new VBox(25);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding: 30 20 25 20;");

        Text t1 = new Text("Êtes-vous sûr de vouloir supprimer " + entityType + "\n\"");
        t1.setStyle("-fx-font-size: 14px; -fx-fill: #374151;");
        Text t2 = new Text(entityName);
        t2.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-fill: #1e3a8a;");
        Text t3 = new Text("\" ?");
        t3.setStyle("-fx-font-size: 14px; -fx-fill: #374151;");

        TextFlow messageFlow = new TextFlow(t1, t2, t3);
        messageFlow.setTextAlignment(TextAlignment.CENTER);

        HBox warningBox = new HBox(8);
        warningBox.setAlignment(Pos.CENTER);
        Label smallWarnIcon = new Label("⚠️");
        smallWarnIcon.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b;");
        Label smallWarnText = new Label("Cette action est irréversible et l'élément sera supprimé définitivement.");
        smallWarnText.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        warningBox.getChildren().addAll(smallWarnIcon, smallWarnText);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            confirmed.set(false);
            dialog.close();
        });

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            confirmed.set(true);
            dialog.close();
        });

        actions.getChildren().addAll(cancelBtn, deleteBtn);

        body.getChildren().addAll(messageFlow, warningBox, actions);
        roundedContainer.getChildren().addAll(header, body);
        root.getChildren().add(roundedContainer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();

        return confirmed.get();
    }
}
