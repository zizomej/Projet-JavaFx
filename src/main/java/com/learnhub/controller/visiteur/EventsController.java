package com.learnhub.controller.visiteur;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class EventsController {

    @FXML private BorderPane rootPane;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> filterCombo;
    @FXML private TilePane eventsPane;
    @FXML private VBox emptyState;

    @FXML
    public void initialize() {
        try {
            List<Evenement> events = new EvenementDAO().findAll();

            // Remplir le filtre par catégorie
            filterCombo.getItems().add("Toutes les catégories");
            events.stream()
                  .map(Evenement::getCategorie)
                  .filter(c -> c != null && !c.isBlank())
                  .distinct()
                  .sorted()
                  .forEach(cat -> filterCombo.getItems().add(cat));
            filterCombo.setValue("Toutes les catégories");

            filterCombo.setOnAction(e -> {
                String selected = filterCombo.getValue();
                eventsPane.getChildren().clear();
                List<Evenement> filtered = events;
                if (selected != null && !selected.equals("Toutes les catégories")) {
                    filtered = events.stream()
                        .filter(ev -> selected.equals(ev.getCategorie()))
                        .collect(Collectors.toList());
                }
                afficherEvenements(filtered);
                totalLabel.setText(filtered.size() + " événement(s)");
            });

            afficherEvenements(events);
            totalLabel.setText(events.size() + " événement(s) disponible(s)");

            if (events.isEmpty()) {
                emptyState.setVisible(true);
                emptyState.setManaged(true);
                eventsPane.setVisible(false);
                eventsPane.setManaged(false);
            }

        } catch (SQLException e) {
            totalLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    private void afficherEvenements(List<Evenement> events) {
        eventsPane.getChildren().clear();
        for (Evenement ev : events) {
            eventsPane.getChildren().add(buildEventCard(ev));
        }
    }

    private VBox buildEventCard(Evenement ev) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(360);
        card.setPadding(new Insets(22));

        // Categorie badge
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        if (ev.getCategorie() != null && !ev.getCategorie().isBlank()) {
            Label catBadge = new Label("🏷  " + ev.getCategorie());
            catBadge.getStyleClass().add("event-category-badge");
            topRow.getChildren().add(catBadge);
        }

        // Titre
        Label titre = new Label(ev.getTitre() != null ? ev.getTitre() : "Sans titre");
        titre.getStyleClass().add("event-card-title");
        titre.setWrapText(true);

        // Description
        Label desc = new Label();
        if (ev.getDescription() != null && !ev.getDescription().isBlank()) {
            String shortDesc = ev.getDescription().length() > 100
                ? ev.getDescription().substring(0, 100) + "..." : ev.getDescription();
            desc.setText(shortDesc);
            desc.setWrapText(true);
            desc.getStyleClass().add("event-card-meta");
        }

        // Meta info
        HBox metaRow = new HBox(16);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        if (ev.getDate() != null) {
            Label date = new Label("📅  " + ev.getDate());
            date.getStyleClass().add("event-card-meta");
            metaRow.getChildren().add(date);
        }
        if (ev.getLieu() != null && !ev.getLieu().isBlank()) {
            Label lieu = new Label("📍  " + ev.getLieu());
            lieu.getStyleClass().add("event-card-meta");
            metaRow.getChildren().add(lieu);
        }

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0;");

        card.getChildren().addAll(topRow, titre);
        if (ev.getDescription() != null && !ev.getDescription().isBlank()) {
            card.getChildren().add(desc);
        }
        card.getChildren().addAll(sep, metaRow);
        return card;
    }

    @FXML private void goHome()     { navigate("/fxml/visiteur/home.fxml", "LearnHub — Accueil"); }
    @FXML private void goPrograms() { navigate("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goEvents()   { /* déjà ici */ }
    @FXML private void goPartners() { navigate("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goLogin()    { navigate("/fxml/auth/login.fxml", "Connexion"); }
    @FXML private void goRegister() { navigate("/fxml/auth/register.fxml", "Inscription"); }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
