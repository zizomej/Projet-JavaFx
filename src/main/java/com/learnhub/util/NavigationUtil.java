package com.learnhub.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class NavigationUtil {

    public static void navigateTo(Stage stage, String fxmlPath, String title) {
        try {
            // Preserve window state before scene swap
            boolean maximized = stage.isMaximized();
            double w = stage.getWidth();
            double h = stage.getHeight();

            URL resource = NavigationUtil.class.getResource(fxmlPath);
            if (resource == null) throw new IOException("FXML not found: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL css = NavigationUtil.class.getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("LearnHub - " + title);
            stage.setResizable(true);
            stage.setScene(scene);
            // Restore window state — never shrink from 1280×800
            if (maximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(Math.max(w, 1280));
                stage.setHeight(Math.max(h, 800));
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigate to a fixed-size window (login, register) — non-resizable, centered.
     */
    public static void navigateToFixed(Stage stage, String fxmlPath, String title, double width, double height) {
        try {
            URL resource = NavigationUtil.class.getResource(fxmlPath);
            if (resource == null) throw new IOException("FXML not found: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            URL css = NavigationUtil.class.getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("LearnHub - " + title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FXMLLoader loadFXML(String fxmlPath) throws IOException {
        URL resource = NavigationUtil.class.getResource(fxmlPath);
        if (resource == null) throw new IOException("FXML not found: " + fxmlPath);
        return new FXMLLoader(resource);
    }

    /**
     * DB roles are: admin, professeur, etudiant, parent, medecin (lowercase, no ROLE_ prefix)
     */
    public static void redirectByRole(Stage stage) {
        String role = SessionManager.getInstance().getRole();
        if (role == null) role = "";
        switch (role.toLowerCase()) {
            case "admin"      -> navigateTo(stage, "/fxml/admin/dashboard.fxml",    "Espace Admin");
            case "professeur" -> navigateTo(stage, "/fxml/professor/dashboard.fxml","Espace Professeur");
            case "etudiant"   -> navigateTo(stage, "/fxml/student/dashboard.fxml",  "Espace Étudiant");
            case "parent"     -> navigateTo(stage, "/fxml/parent/dashboard.fxml",   "Espace Parent");
            case "medecin"    -> navigateTo(stage, "/fxml/medecin/dashboard.fxml",  "Espace Médecin");
            default           -> navigateToFixed(stage, "/fxml/auth/login.fxml", "Connexion", 1100, 700);
        }
    }
}
