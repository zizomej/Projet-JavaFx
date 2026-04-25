package com.learnhub.controller.visiteur;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Universite;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class UniversiteDetailsController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Label uniNameLabel;
    @FXML
    private Label uniSubInfoLabel;
    @FXML
    private Label filieresCountLabel;
    @FXML
    private FlowPane filieresContainer;

    // Sidebar bindings
    @FXML
    private Label uniAddressLabel;
    @FXML
    private Label uniCityLabel;
    @FXML
    private Label uniPhoneLabel;
    @FXML
    private Label uniEmailLabel;

    private Universite universite;
    private final FiliereDAO filiereDAO = new FiliereDAO();

    public void setUniversite(Universite u) {
        this.universite = u;
        if (u != null) {
            uniNameLabel.setText(u.getNom());
            uniSubInfoLabel.setText((u.getType() != null ? u.getType() : "Université") +
                    (u.getVille() != null && !u.getVille().isEmpty() ? " • " + u.getVille() : ""));

            // Populate Sidebar
            uniAddressLabel
                    .setText(u.getAdresse() != null && !u.getAdresse().isEmpty() ? u.getAdresse() : "Non renseignée");
            uniCityLabel.setText(u.getVille() != null && !u.getVille().isEmpty() ? u.getVille() : "Non renseignée");
            uniPhoneLabel.setText(
                    u.getTelephone() != null && !u.getTelephone().isEmpty() ? u.getTelephone() : "Non renseigné");
            uniEmailLabel.setText(u.getEmail() != null && !u.getEmail().isEmpty() ? u.getEmail() : "Non renseigné");

            loadFilieres();
        }
    }

    @FXML
    private void handleShowLocation() {
        if (universite == null)
            return;

        try {
            Stage mapStage = new Stage();
            mapStage.setTitle("Localisation - " + universite.getNom());
            mapStage.initModality(Modality.APPLICATION_MODAL);
            mapStage.initOwner(getStage());

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Construct a robust query for Google Maps
            StringBuilder queryBuilder = new StringBuilder(universite.getNom());
            if (universite.getVille() != null && !universite.getVille().equalsIgnoreCase("Non renseignée")) {
                queryBuilder.append(", ").append(universite.getVille());
            }
            String fullQuery = queryBuilder.toString();

            // Google Maps is much more stable and precise for university names
            String mapUrl = "https://www.google.com/maps/search/?api=1&query=" + 
                            URLEncoder.encode(fullQuery, "UTF-8");

            webEngine.load(mapUrl);

            VBox layout = new VBox(webView);
            VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS);
            layout.setStyle("-fx-background-color: white;");

            Scene scene = new Scene(layout, 1000, 750);
            mapStage.setScene(scene);
            mapStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFilieres() {
        if (filieresContainer == null || universite == null)
            return;
        filieresContainer.getChildren().clear();

        try {
            List<Filiere> filieres = filiereDAO.findByUniversite(universite.getId());
            filieresCountLabel
                    .setText(filieres.size() + (filieres.size() > 1 ? " formations trouvées" : " formation trouvée"));

            if (filieres.isEmpty()) {
                Label noFiliere = new Label("Aucune formation n'est encore disponible pour cet établissement.");
                noFiliere.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 15px;");
                filieresContainer.getChildren().add(noFiliere);
            } else {
                for (Filiere f : filieres) {
                    VBox card = createFiliereCard(f);
                    filieresContainer.getChildren().add(card);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            filieresCountLabel.setText("Erreur de chargement");
        }
    }

    private VBox createFiliereCard(Filiere f) {
        VBox box = new VBox(15);
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 30; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1; -fx-pref-width: 350; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label fName = new Label(f.getNom() + " (" + f.getCode() + ")");
        fName.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");
        fName.setWrapText(true);

        Label fLevel = new Label("Niveau : " + f.getNiveau());
        fLevel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button applyBtn = new Button("Postuler en ligne ➔");
        applyBtn.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        applyBtn.setOnMouseEntered(ev -> applyBtn.setStyle(
                "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"));
        applyBtn.setOnMouseExited(ev -> applyBtn.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"));
        applyBtn.setOnAction(e -> openApplicationForm(f));

        HBox btnBox = new HBox(applyBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");

        box.setOnMouseClicked(ev -> openFiliereDetails(f));
        box.setOnMouseEntered(ev -> box.setStyle(
                "-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 30; -fx-border-color: #3b82f6; -fx-border-radius: 12; -fx-border-width: 1; -fx-pref-width: 350; -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.1), 15, 0, 0, 6); -fx-cursor: hand;"));
        box.setOnMouseExited(ev -> box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 30; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1; -fx-pref-width: 350; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);"));

        box.getChildren().addAll(fName, fLevel, spacer, btnBox);
        return box;
    }

    private void openFiliereDetails(Filiere f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visiteur/filiere_details.fxml"));
            Parent root = loader.load();
            FiliereDetailsController controller = loader.getController();
            controller.setFiliere(f);

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openApplicationForm(Filiere f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visiteur/candidature_form.fxml"));
            Parent root = loader.load();

            CandidatureFormController controller = loader.getController();
            controller.setFiliere(f);

            Stage stage = new Stage();
            stage.setTitle("Postuler - " + f.getNom());
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    @FXML
    private void goHome() {
        NavigationUtil.navigateTo(getStage(), "/fxml/visiteur/home.fxml", "Accueil");
    }

    @FXML
    private void goPrograms() {
    }

    @FXML
    private void goEvents() {
        NavigationUtil.navigateTo(getStage(), "/fxml/visiteur/events.fxml", "Événements");
    }

    @FXML
    private void goPartners() {
    }

    @FXML
    private void goLogin() {
        NavigationUtil.navigateTo(getStage(), "/fxml/auth/login.fxml", "Connexion");
    }
}
