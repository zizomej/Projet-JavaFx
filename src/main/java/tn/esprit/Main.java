package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/dashboard.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("LearnHub - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PrintWriter pw = new PrintWriter("app-error.log");
                e.printStackTrace(pw);
                pw.close();
            } catch (Exception ex) {}
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}