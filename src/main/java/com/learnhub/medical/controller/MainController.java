package com.learnhub.medical.controller;

import com.learnhub.medical.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Label lblSessionInfo;

    @FXML
    public void initialize() {
        buildSidebar();
        showDefault();
    }

    private void buildSidebar() {
        String role = RoleSelectionController.selectedRole;
        String userName = "";
        if (SessionManager.getInstance().getCurrentUser() != null)
            userName = SessionManager.getInstance().getCurrentUser().getFullName();
        lblSessionInfo.setText(userName.isBlank() ? role : userName);

        sidebar.getChildren().clear();

        // Logo
        HBox logo = new HBox(8);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(0, 0, 20, 0));
        Label l1 = new Label("LearnHub");
        l1.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");
        Label l2 = new Label("Medical");
        l2.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        logo.getChildren().addAll(l1, l2);
        sidebar.getChildren().add(logo);

        Separator sep1 = new Separator();
        sep1.setOpacity(0.2);
        sidebar.getChildren().add(sep1);

        // Menu selon role
        switch (role) {
            case "MEDECIN" -> {
                addSection("ESPACE MEDECIN");
                addBtn("Tableau de Bord",   this::showMedecinDashboard);
                addBtn("Mes Creneaux",      this::showCreneaux);
                addBtn("Rendez-vous",       this::showRDV);
            }
            case "STUDENT" -> {
                addSection("ESPACE ETUDIANT");
                addBtn("Prendre RDV",       this::showStudentPlanning);
                addBtn("Mes Rendez-vous",   this::showStudentAppointments);
            }
            default -> { // ADMIN
                addSection("ESPACE ADMINISTRATEUR");
                addBtn("Utilisateurs",      this::showUsers);
                addBtn("Gestion Creneaux",  this::showCreneaux);
                addBtn("Gestion RDV",       this::showRDV);
            }
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Separator sep2 = new Separator();
        sep2.setOpacity(0.2);
        sidebar.getChildren().add(sep2);

        // Session info
        Label lSession = new Label("Session : " + role);
        lSession.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 11; -fx-font-weight: bold;");
        sidebar.getChildren().add(lSession);
        sidebar.getChildren().add(lblSessionInfo);

        // Deconnexion
        Button btnLogout = new Button("Deconnexion");
        btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 8; " +
            "-fx-cursor: hand; -fx-alignment: center-left;");
        btnLogout.setPrefWidth(210);
        btnLogout.setOnAction(e -> handleDeconnexion());
        sidebar.getChildren().add(btnLogout);
    }

    private void addSection(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #FFC107; -fx-opacity: 0.7;");
        VBox.setMargin(lbl, new Insets(10, 0, 4, 15));
        sidebar.getChildren().add(lbl);
    }

    private void addBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(210);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> {
            sidebar.getChildren().forEach(n -> {
                if (n instanceof Button b) b.getStyleClass().remove("nav-button-active");
            });
            btn.getStyleClass().add("nav-button-active");
            action.run();
        });
        sidebar.getChildren().add(btn);
    }

    private void showDefault() {
        String role = RoleSelectionController.selectedRole;
        switch (role) {
            case "STUDENT" -> showStudentPlanning();
            case "MEDECIN" -> showMedecinDashboard();
            default -> showUsers();
        }
    }

    private void showMedecinDashboard()   { loadPage("MedecinDashboard"); }
    private void showStudentDashboard()   { loadPage("StudentDashboard"); }
    private void showUsers()              { loadPage("UserManagement"); }
    private void showCreneaux()           { loadPage("CreneauManagement"); }
    private void showRDV()                { loadPage("RDVManagement"); }
    private void showStudentPlanning()    { loadPage("StudentPlanning"); }
    private void showStudentAppointments(){ loadPage("StudentAppointments"); }

    private void loadPage(String page) {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/com/learnhub/medical/view/" + page + ".fxml"));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            Label err = new Label("Impossible de charger : " + page + "\n" + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().setAll(err);
        }
    }

    private void handleDeconnexion() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/com/learnhub/medical/view/Login.fxml"));
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
