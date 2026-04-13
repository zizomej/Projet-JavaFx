package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    public static String selectedRole = "ADMIN";

    @FXML private VBox cardMedecin;
    @FXML private VBox cardStudent;
    @FXML private VBox cardAdmin;

    @FXML
    private void selectMedecin() {
        selectedRole = "MEDECIN";
        initMockSession("ROLE_MEDECIN", "Dr.", "Salah");
        navigateToMain();
    }

    @FXML
    private void selectStudent() {
        selectedRole = "STUDENT";
        initMockSession("ROLE_ETUDIANT", "Alice", "Student");
        navigateToMain();
    }

    @FXML
    private void selectAdmin() {
        selectedRole = "ADMIN";
        initMockSession("ROLE_ADMIN", "Admin", "User");
        navigateToMain();
    }

    private void initMockSession(String role, String prenom, String nom) {
        if (SessionManager.getInstance().getCurrentUser() == null) {
            Utilisateur u = new Utilisateur();
            u.setId(1);
            u.setRoles(role);
            u.setNom(nom);
            u.setPrenom(prenom);
            SessionManager.getInstance().setCurrentUser(u);
        }
    }

    private void navigateToMain() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/com/learnhub/medical/view/Main.fxml"));
            Stage stage = (Stage) cardMedecin.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
