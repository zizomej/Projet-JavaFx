package com.learnhub.controller;

import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class PartnersPageController {

    @FXML private Label titleLabel;
    @FXML private Label heroSubtitleLabel;
    @FXML private Label statPartenairesLabel;
    @FXML private Label statOffresLabel;
    @FXML private TextField visitorSearchField;
    @FXML private Label partnersStatusLabel;
    @FXML private FlowPane partnersFlow;

    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private final OffreStageDAO offreStageDAO = new OffreStageDAO();

    @FXML
    public void initialize() {
        loadStats();
        loadPartners();
        if (visitorSearchField != null) {
            visitorSearchField.textProperty().addListener((obs, o, n) -> loadPartners());
        }
    }

    private void loadStats() {
        try {
            List<Partenaire> actifs = partenaireDAO.findActiveOrdered();
            int n = actifs.isEmpty() ? partenaireDAO.count() : actifs.size();
            if (statPartenairesLabel != null) {
                statPartenairesLabel.setText(String.valueOf(n));
            }
            if (heroSubtitleLabel != null) {
                heroSubtitleLabel.setText("Données en direct : " + n + " partenaire(s) référencé(s) sur LearnHub.");
            }
            if (statOffresLabel != null) {
                statOffresLabel.setText(String.valueOf(offreStageDAO.count()));
            }
        } catch (SQLException e) {
            if (partnersStatusLabel != null) {
                partnersStatusLabel.setText("Impossible de charger les statistiques.");
            }
        }
    }

    private void loadPartners() {
        if (partnersFlow == null) {
            return;
        }
        partnersFlow.getChildren().clear();
        String q = visitorSearchField != null && visitorSearchField.getText() != null
                ? visitorSearchField.getText().trim() : "";
        try {
            List<Partenaire> list = partenaireDAO.findActiveOrdered();
            if (list.isEmpty()) {
                list = partenaireDAO.findAll();
            }
            if (!q.isEmpty()) {
                String needle = q.toLowerCase(Locale.ROOT);
                list = list.stream()
                        .filter(p -> matches(p, needle))
                        .toList();
            }
            if (partnersStatusLabel != null) {
                partnersStatusLabel.setText(list.size() + " partenaire(s) affiché(s).");
            }
            for (Partenaire p : list) {
                partnersFlow.getChildren().add(buildPartnerCard(p));
            }
            if (list.isEmpty()) {
                Label empty = new Label("Aucun partenaire ne correspond à votre recherche.");
                empty.setStyle("-fx-text-fill:#64748b; -fx-font-size:14px; -fx-padding:16;");
                partnersFlow.getChildren().add(empty);
            }
        } catch (SQLException e) {
            if (partnersStatusLabel != null) {
                partnersStatusLabel.setText("Erreur : " + e.getMessage());
            }
        }
    }

    private boolean matches(Partenaire p, String needle) {
        return contains(p.getNom(), needle)
                || contains(p.getSecteur(), needle)
                || contains(p.getVille(), needle)
                || contains(p.getPays(), needle)
                || contains(p.getDescription(), needle);
    }

    private boolean contains(String s, String needle) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(needle);
    }

    private VBox buildPartnerCard(Partenaire p) {
        String initials = initialsFor(p.getNom());
        String color = colorForName(p.getNom());

        StackPane avatar = new StackPane();
        avatar.setMinSize(56, 56);
        avatar.setMaxSize(56, 56);
        avatar.setStyle("-fx-background-color:" + color + "; -fx-background-radius:14;");
        Label av = new Label(initials);
        av.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:18px;");

        avatar.getChildren().add(av);

        Label name = new Label(p.getNom() != null ? p.getNom() : "—");
        name.setStyle("-fx-font-size:15px; -fx-font-weight:800; -fx-text-fill:#0f172a;");

        String badgeText = (p.getSecteur() != null && !p.getSecteur().isBlank())
                ? p.getSecteur()
                : ((p.getVille() != null && !p.getVille().isBlank()) ? p.getVille() : "Partenaire");
        Label badge = new Label(badgeText);
        badge.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b; -fx-background-color:#f1f5f9; -fx-padding:4 10; -fx-background-radius:999;");

        VBox card = new VBox(10, avatar, name, badge);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(18, 16, 18, 16));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 14, 0, 0, 3); -fx-border-color:#f1f5f9; -fx-border-width:1; -fx-border-radius:18;");
        return card;
    }

    private String initialsFor(String nom) {
        if (nom == null || nom.isBlank()) {
            return "?";
        }
        String[] parts = nom.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private static final String[] CARD_COLORS = {
            "#2563eb", "#7c3aed", "#db2777", "#ea580c", "#059669", "#0d9488", "#4f46e5", "#b45309"
    };

    private String colorForName(String nom) {
        int i = nom == null ? 0 : Math.abs(nom.hashCode() % CARD_COLORS.length);
        return CARD_COLORS[i];
    }

    @FXML
    private void handleVisitorRefresh() {
        loadStats();
        loadPartners();
    }

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
