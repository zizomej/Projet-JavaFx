package com.learnhub.controller.visiteur;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.Filiere;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class PartnersController {

    @FXML private BorderPane rootPane;
    @FXML private TilePane filieresPane;
    @FXML private VBox emptyFilieres;

    private static final String[] ICONS = { "📚", "🔬", "💻", "🎨", "⚙️", "🏗️", "🌱", "🏥", "⚖️", "🎵" };

    @FXML
    public void initialize() {
        try {
            List<Filiere> filieres = new FiliereDAO().findAll();
            if (filieres.isEmpty()) {
                emptyFilieres.setVisible(true);
                emptyFilieres.setManaged(true);
                filieresPane.setVisible(false);
                filieresPane.setManaged(false);
            } else {
                for (int i = 0; i < filieres.size(); i++) {
                    filieresPane.getChildren().add(buildFiliereCard(filieres.get(i), i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox buildFiliereCard(Filiere f, int index) {
        VBox card = new VBox(10);
        card.getStyleClass().add("partner-card");
        card.setPrefWidth(280);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));

        String icon = ICONS[index % ICONS.length];
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("partner-card-icon");

        Label nameLabel = new Label(f.getNom() != null ? f.getNom() : "Filière");
        nameLabel.getStyleClass().add("partner-card-name");
        nameLabel.setWrapText(true);

        Label niveauLabel = new Label(
            (f.getNiveau() != null ? f.getNiveau() : "") +
            (f.getDuree() > 0 ? "  •  " + f.getDuree() + " ans" : "")
        );
        niveauLabel.getStyleClass().add("badge-warning");
        niveauLabel.getStyleClass().add("badge");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0;");

        if (f.getDescription() != null && !f.getDescription().isBlank()) {
            String shortDesc = f.getDescription().length() > 80
                ? f.getDescription().substring(0, 80) + "..." : f.getDescription();
            Label descLabel = new Label(shortDesc);
            descLabel.getStyleClass().add("partner-card-desc");
            descLabel.setWrapText(true);
            card.getChildren().addAll(iconLabel, nameLabel, niveauLabel, sep, descLabel);
        } else {
            card.getChildren().addAll(iconLabel, nameLabel, niveauLabel);
        }

        if (f.getResponsable() != null && !f.getResponsable().isBlank()) {
            Label resp = new Label("👤  " + f.getResponsable());
            resp.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
            card.getChildren().add(resp);
        }

        return card;
    }

    @FXML private void goHome()     { navigate("/fxml/visiteur/home.fxml", "LearnHub — Accueil"); }
    @FXML private void goPrograms() { navigate("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goEvents()   { navigate("/fxml/visiteur/events.fxml", "Événements"); }
    @FXML private void goPartners() { /* déjà ici */ }
    @FXML private void goLogin()    { navigate("/fxml/auth/login.fxml", "Connexion"); }
    @FXML private void goRegister() { navigate("/fxml/auth/register.fxml", "Inscription"); }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
