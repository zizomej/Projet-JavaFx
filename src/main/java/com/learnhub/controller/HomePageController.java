package com.learnhub.controller;

import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomePageController {

    @FXML private Label titleLabel;

    @FXML public void initialize() {}

    @FXML private void goToHome()     { nav("/fxml/visiteur/home.fxml",     "Accueil"); }
    @FXML private void goToPrograms() { nav("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goToEvents()   { nav("/fxml/visiteur/events.fxml",   "Événements"); }
    @FXML private void goToPartners() { nav("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goToLogin()    {
        Stage s = stage();
        if (s != null) NavigationUtil.navigateTo(s, "/fxml/auth/login.fxml", "Connexion");
    }
    @FXML private void goToRegister() {
        Stage s = stage();
        if (s != null) NavigationUtil.navigateTo(s, "/fxml/auth/register.fxml", "Inscription");
    }

    private void nav(String fxml, String title) {
        Stage s = stage();
        if (s != null) NavigationUtil.navigateTo(s, fxml, title);
    }

    private Stage stage() {
        if (titleLabel != null && titleLabel.getScene() != null)
            return (Stage) titleLabel.getScene().getWindow();
        return null;
    }
}
