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
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class HomeController {

    @FXML
    private Text statEtudiants;
    @FXML
    private Text statProfesseurs;
    @FXML
    private Text statModules;
    @FXML
    private Text statPartenaires;
    @FXML
    private VBox universitesContainer;

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

            if (statEtudiants != null)
                statEtudiants.setText(String.valueOf(totalEtudiants));
            if (statProfesseurs != null)
                statProfesseurs.setText(String.valueOf(totalProfesseurs));
            if (statModules != null)
                statModules.setText(String.valueOf(totalModules));
            if (statPartenaires != null)
                statPartenaires.setText(String.valueOf(totalPartenaires));

        } catch (SQLException e) {
            e.printStackTrace();
            if (statEtudiants != null)
                statEtudiants.setText("--");
            if (statProfesseurs != null)
                statProfesseurs.setText("--");
            if (statModules != null)
                statModules.setText("--");
            if (statPartenaires != null)
                statPartenaires.setText("--");
        }
    }

    private void loadUniversites() {
        if (universitesContainer == null)
            return;
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
        VBox card = new VBox(20);
        String defaultStyle = "-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #e5e7eb; -fx-border-radius: 20; -fx-border-width: 1; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5);";
        String hoverStyle = "-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #1E3A8A; -fx-border-radius: 20; -fx-border-width: 1; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(30,58,138,0.15), 25, 0, 0, 8); -fx-cursor: hand;";

        card.setStyle(defaultStyle);
        card.setOnMouseEntered(ev -> card.setStyle(hoverStyle));
        card.setOnMouseExited(ev -> card.setStyle(defaultStyle));
        card.setOnMouseClicked(ev -> openUniversiteDetails(u));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🏛️");
        icon.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e2e8f0); -fx-text-fill: #1E3A8A; -fx-background-radius: 16; -fx-min-width: 64px; -fx-max-width: 64px; -fx-min-height: 64px; -fx-max-height: 64px; -fx-font-size: 32px; -fx-alignment: CENTER; -fx-border-color: #cbd5e1; -fx-border-radius: 16; -fx-border-width: 1;");

        VBox titleBox = new VBox(8);
        Label name = new Label(u.getNom());
        name.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(u.getType());
        typeBadge.setStyle(
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold;");
        if (u.getType() != null && u.getType().toLowerCase().contains("priv")) {
            typeBadge.setStyle(
                    "-fx-background-color: #fce7f3; -fx-text-fill: #be185d; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        Label locationInfo = new Label("📍 " + u.getVille());
        locationInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: 600;");

        infoBox.getChildren().addAll(typeBadge, locationInfo);
        titleBox.getChildren().addAll(name, infoBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button discoverBtn = new Button("Voir les détails ➔");
        String btnDefault = "-fx-background-color: transparent; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand; -fx-border-color: #1E3A8A; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 8 20;";
        String btnHover = "-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand; -fx-border-color: #1E3A8A; -fx-border-radius: 8; -fx-border-width: 2; -fx-padding: 8 20;";

        discoverBtn.setStyle(btnDefault);
        discoverBtn.setOnMouseEntered(ev -> discoverBtn.setStyle(btnHover));
        discoverBtn.setOnMouseExited(ev -> discoverBtn.setStyle(btnDefault));
        discoverBtn.setOnAction(e -> {
            e.consume(); // prevent triggering card click
            openUniversiteDetails(u);
        });

        Button excelBtn = new Button("📊 Excel");
        String excelBtnDefault = "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 8 16;";
        String excelBtnHover = "-fx-background-color: #e5e7eb; -fx-text-fill: #111827; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand; -fx-border-color: #9ca3af; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 8 16;";
        
        excelBtn.setStyle(excelBtnDefault);
        excelBtn.setOnMouseEntered(ev -> excelBtn.setStyle(excelBtnHover));
        excelBtn.setOnMouseExited(ev -> excelBtn.setStyle(excelBtnDefault));
        excelBtn.setOnAction(e -> {
            e.consume();
            handleIndividualExportExcel(u);
        });

        HBox actions = new HBox(12, excelBtn, discoverBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(icon, titleBox, spacer, actions);

        // Sub section for Filieres Tags
        List<Filiere> filieres = filiereDAO.findByUniversite(u.getId());
        if (filieres != null && !filieres.isEmpty()) {
            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
            sep.setStyle("-fx-padding: 5 0;");

            FlowPane tagsPane = new FlowPane();
            tagsPane.setHgap(12);
            tagsPane.setVgap(12);

            for (int i = 0; i < Math.min(filieres.size(), 5); i++) {
                Label tag = new Label("🎓 " + filieres.get(i).getNom());
                tag.setStyle(
                        "-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-padding: 8 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: 600; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
                tagsPane.getChildren().add(tag);
            }
            if (filieres.size() > 5) {
                Label moreTag = new Label("+" + (filieres.size() - 5) + " programmes");
                moreTag.setStyle(
                        "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-padding: 8 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-border-color: #bfdbfe; -fx-border-radius: 8;");
                tagsPane.getChildren().add(moreTag);
            }
            card.getChildren().addAll(header, sep, tagsPane);
        } else {
            card.getChildren().add(header);
        }

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

    private void handleIndividualExportExcel(Universite u) {
        try {
            List<Filiere> filieres = filiereDAO.findByUniversite(u.getId());
            
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer la fiche Excel - " + u.getNom());
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            fileChooser.setInitialFileName("Info_" + u.getNom().replace(" ", "_") + ".xlsx");

            java.io.File file = fileChooser.showSaveDialog(universitesContainer.getScene().getWindow());
            if (file != null) {
                com.learnhub.service.UniversiteExcelService.exportSingleUniversite(u, filieres, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export réussi", "La fiche de l'université a été exportée avec succès vers :\n" + file.getAbsolutePath());
                
                try {
                    java.awt.Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    System.out.println("Could not open file automatically: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'exportation", "Une erreur est survenue lors de la génération du fichier Excel : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goHome() {
        navigateTo("/fxml/visiteur/home.fxml", "Accueil");
    }

    @FXML
    private void goPrograms() {
        navigateTo("/fxml/visiteur/programs.fxml", "Programmes");
    }

    @FXML
    private void goEvents() {
        navigateTo("/fxml/visiteur/events.fxml", "Événements");
    }

    @FXML
    private void goPartners() {
        navigateTo("/fxml/visiteur/partners.fxml", "Partenaires");
    }

    @FXML
    private void goLogin() {
        navigateTo("/fxml/auth/login.fxml", "Connexion");
    }

    @FXML
    private void openChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visiteur/chatbot.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("LearnBot - Assistant");
            stage.setScene(new Scene(root));
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            
            // Position bottom right
            stage.setOnShown(e -> {
                Stage mainStage = (Stage) universitesContainer.getScene().getWindow();
                stage.setX(mainStage.getX() + mainStage.getWidth() - 420);
                stage.setY(mainStage.getY() + mainStage.getHeight() - 560);
            });
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) universitesContainer.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxmlPath, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
