package com.learnhub;

import com.learnhub.util.DatabaseConnection;
import com.learnhub.util.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LearnHub");
        // Start on visiteur home (maximized, resizable)
        NavigationUtil.navigateTo(primaryStage, "/fxml/visiteur/home.fxml", "Accueil");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
