package com.learnhub.controller.visiteur;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.dao.UniversiteDAO;
import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.Universite;
import com.learnhub.models.Filiere;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class HomeController {

    @FXML private Text statEtudiants;
    @FXML private Text statProfesseurs;
    @FXML private Text statModules;
    @FXML private Text statPartenaires;
    @FXML private VBox universitesContainer;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private final UniversiteDAO universiteDAO = new UniversiteDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();

    @FXML
    public void initialize() {
        loadStatistics();
        loadUniversites();
    }

    private void loadStatistics() {
        try {
            int totalEtudiants = utilisateurDAO.countByRole("etudiant");
            int totalProfesseurs = utilisateurDAO.countByRole("professeur");
            int totalModules = moduleDAO.count();
            int totalPartenaires = partenaireDAO.count();

            if (statEtudiants != null) statEtudiants.setText(String.valueOf(totalEtudiants));
            if (statProfesseurs != null) statProfesseurs.setText(String.valueOf(totalProfesseurs));
            if (statModules != null) statModules.setText(String.valueOf(totalModules));
            if (statPartenaires != null) statPartenaires.setText(String.valueOf(totalPartenaires));

        } catch (SQLException e) {
            e.printStackTrace();
            if (statEtudiants != null) statEtudiants.setText("--");
            if (statProfesseurs != null) statProfesseurs.setText("--");
            if (statModules != null) statModules.setText("--");
            if (statPartenaires != null) statPartenaires.setText("--");
        }
    }

    private void loadUniversites() {
        if (universitesContainer == null) return;
        universitesContainer.getChildren().clear();

        try {
            List<Universite> universites = universiteDAO.findAll();
            for (Universite u : universites) {
                VBox uniBox = createUniversiteCard(u);
                universitesContainer.getChildren().add(uniBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createUniversiteCard(Universite u) throws SQLException {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-border-width: 2; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);");
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🏛️");
        icon.setStyle("-fx-background-color: linear-gradient(to bottom right, #1E3A8A, #0A1F44); -fx-background-radius: 12; -fx-min-width: 50px; -fx-max-width: 50px; -fx-min-height: 50px; -fx-max-height: 50px; -fx-font-size: 24px; -fx-alignment: CENTER;");
        
        VBox titleBox = new VBox(5);
        Label name = new Label(u.getNom());
        name.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #0A1F44;");
        Label subInfo = new Label(u.getType() + " • " + u.getVille());
        subInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        
        titleBox.getChildren().addAll(name, subInfo);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button discoverBtn = new Button("Découvrir les formations ➔");
        discoverBtn.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        discoverBtn.setOnMouseEntered(ev -> discoverBtn.setStyle("-fx-background-color: #1E40AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"));
        discoverBtn.setOnMouseExited(ev -> discoverBtn.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"));
        discoverBtn.setOnAction(e -> openUniversiteDetails(u));
        
        header.getChildren().addAll(icon, titleBox, spacer, discoverBtn);

        card.getChildren().add(header);
        return card;
    }

    private void openUniversiteDetails(Universite u) {
        try {
            FXMLLoader loader = NavigationUtil.loadFXML("/fxml/visiteur/universite_details.fxml");
            Parent root = loader.load();
            UniversiteDetailsController controller = loader.getController();
            controller.setUniversite(u);
            Stage stage = (Stage) universitesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void goHome() { navigateTo("/fxml/visiteur/home.fxml", "Accueil"); }
    @FXML private void goPrograms() { navigateTo("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goEvents() { navigateTo("/fxml/visiteur/events.fxml", "Événements"); }
    @FXML private void goPartners() { navigateTo("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goLogin() { navigateTo("/fxml/auth/login.fxml", "Connexion"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) javafx.stage.Window.getWindows().get(0).getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxmlPath, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
