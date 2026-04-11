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
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        NavigationUtil.navigateTo(primaryStage, "/fxml/visiteur/home.fxml", "Accueil");
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
