package com.learnhub.gestion_rdv_creneau;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/learnhub/gestion_rdv_creneau/view/RoleSelection.fxml"));
        primaryStage.setTitle("LearnHub Medical Management");
        
        // Use a default size
        Scene scene = new Scene(root, 1100, 750);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


