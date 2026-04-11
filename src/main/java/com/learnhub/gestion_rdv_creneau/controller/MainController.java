package com.learnhub.gestion_rdv_creneau.controller;

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

    // Fields to hold the dynamically created buttons for reference
    private Button btnDashboard = new Button();
    private Button btnCreneaux = new Button();
    private Button btnRDV = new Button();

    @FXML
    public void initialize() {
        configureSidebar();
        showDashboard();
    }

    private void configureSidebar() {
        String role = RoleSelectionController.selectedRole;
        lblSessionInfo.setText("Session: " + role);
        
        // Clear sidebar and rebuild based on role
        sidebar.getChildren().clear();
        
        // Logo Section
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        logoBox.setPrefHeight(100.0);
        Label lblLogo1 = new Label("LearnHub");
        lblLogo1.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblLogo2 = new Label("Medical");
        lblLogo2.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ffc107;");
        logoBox.getChildren().addAll(lblLogo1, lblLogo2);
        sidebar.getChildren().add(logoBox);
        
        Region spacer1 = new Region();
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        sidebar.getChildren().add(spacer1);

        if ("MEDECIN".equals(role)) {
            addMenuLabel("ESPACE MÉDECIN");
            addMenuButton(btnDashboard, "Tableau de Bord", this::showDashboard);
            addMenuButton(btnCreneaux, "Mes Créneaux", this::showCreneaux);
        } else if ("STUDENT".equals(role)) {
            addMenuLabel("ESPACE ÉTUDIANT");
            addMenuButton(btnDashboard, "Prendre RDV", this::showStudentPlanning);
            addMenuButton(new Button(), "Mes Rendez-vous", this::showStudentAppointments);
        } else {
            addMenuLabel("ESPACE ADMINISTRATEUR");
            addMenuButton(btnDashboard, "Stats Globales", this::showDashboard);
            addMenuButton(btnCreneaux, "Gestion Créneaux", this::showCreneaux);
            addMenuButton(btnRDV, "Gestion Globale RDV", this::showRDV);
        }

        Region spacer2 = new Region();
        VBox.setVgrow(spacer2, Priority.ALWAYS);
        sidebar.getChildren().add(spacer2);
        
        Separator sep = new Separator();
        sep.setOpacity(0.1);
        sidebar.getChildren().add(sep);
        
        Button btnLogout = new Button("Déconnexion");
        btnLogout.getStyleClass().add("nav-button");
        btnLogout.setPrefWidth(210);
        btnLogout.setOnAction(e -> handleDeconnexion());
        sidebar.getChildren().add(btnLogout);
        
        sidebar.getChildren().add(lblSessionInfo);
    }

    private void addMenuLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #ffc107; -fx-opacity: 0.7;");
        VBox.setMargin(lbl, new javafx.geometry.Insets(0, 0, 0, 15));
        sidebar.getChildren().add(lbl);
    }

    private void addMenuButton(Button btn, String text, Runnable action) {
        btn.setText(text);
        btn.setMnemonicParsing(false);
        btn.setPrefWidth(210.0);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> {
            resetActiveButtons();
            btn.getStyleClass().add("nav-button-active");
            action.run();
        });
        sidebar.getChildren().add(btn);
    }

    private void resetActiveButtons() {
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("nav-button-active");
            }
        });
    }

    @FXML
    private void showDashboard() {
        loadPage("MedecinDashboard");
    }

    @FXML
    private void showCreneaux() {
        loadPage("CreneauManagement");
    }

    @FXML
    private void showRDV() {
        loadPage("RDVManagement");
    }

    @FXML
    private void showStudentPlanning() {
        loadPage("StudentPlanning");
    }

    @FXML
    private void showStudentAppointments() {
        loadPage("StudentAppointments");
    }

    @FXML
    private void handleDeconnexion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/learnhub/gestion_rdv_creneau/view/RoleSelection.fxml"));
            Stage stage = (Stage) sidebar.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String page) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/learnhub/gestion_rdv_creneau/view/" + page + ".fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn) {
        resetActiveButtons();
        activeBtn.getStyleClass().add("nav-button-active");
    }
}



