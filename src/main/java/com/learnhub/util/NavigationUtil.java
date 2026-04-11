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
            URL resource = NavigationUtil.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("⚠️  FXML non trouvé : " + fxmlPath);
                if (!fxmlPath.equals("/fxml/visiteur/home.fxml"))
                    navigateTo(stage, "/fxml/visiteur/home.fxml", "Accueil");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL css = NavigationUtil.class.getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("LearnHub — " + title);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur FXML [" + fxmlPath + "] : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FXMLLoader loadFXML(String fxmlPath) throws IOException {
        URL resource = NavigationUtil.class.getResource(fxmlPath);
        if (resource == null) throw new IOException("FXML non trouvé : " + fxmlPath);
        return new FXMLLoader(resource);
    }

    public static void redirectByRole(Stage stage) {
        String role = SessionManager.getInstance().getRole();
        System.out.println("=== REDIRECTION === Rôle : '" + role + "'");
        String fxmlPath, title;
        switch (role == null ? "" : role.toLowerCase().trim()) {
            case "admin":       fxmlPath = "/fxml/admin/dashboard.fxml";     title = "Espace Admin";      break;
            case "professeur":
            case "professor":   fxmlPath = "/fxml/professor/dashboard.fxml"; title = "Espace Professeur"; break;
            case "etudiant":
            case "student":     fxmlPath = "/fxml/student/dashboard.fxml";   title = "Espace Étudiant";   break;
            case "parent":      fxmlPath = "/fxml/parent/dashboard.fxml";    title = "Espace Parent";     break;
            case "medecin":     fxmlPath = "/fxml/medecin/dashboard.fxml";   title = "Espace Médecin";    break;
            default:            fxmlPath = "/fxml/visiteur/home.fxml";       title = "Accueil";           break;
        }
        System.out.println("→ " + fxmlPath);

        navigateTo(stage, fxmlPath, title);

    }
}
