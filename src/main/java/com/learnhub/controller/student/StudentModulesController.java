package com.learnhub.controller.student;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.RessourceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Ressource;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentModulesController {

    @FXML private Label topUserName;
    @FXML private Label lblTotalModules;
    @FXML private Label lblTotalRessources;
    @FXML private Accordion modulesAccordion;

    @FXML private javafx.scene.shape.Circle notifBadge;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private final com.learnhub.dao.NotificationDAO notificationDAO = new com.learnhub.dao.NotificationDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            topUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            loadModules();
            checkNotifications(currentUser.getId());
        }
    }

    private void checkNotifications(int userId) {
        try {
            int unread = notificationDAO.countUnread(userId);
            notifBadge.setVisible(unread > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotifications() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/student/notifications_popup.fxml"));
            javafx.scene.Parent root = loader.load();
            
            NotificationsPopupController controller = loader.getController();
            controller.setOnRefresh(() -> checkNotifications(SessionManager.getInstance().getCurrentUser().getId()));
            
            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popupStage.setScene(scene);
            
            Stage mainStage = (Stage) notifBadge.getScene().getWindow();
            popupStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - 225);
            popupStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - 275);
            
            popupStage.showAndWait();
            checkNotifications(SessionManager.getInstance().getCurrentUser().getId());

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void loadModules() {
        modulesAccordion.getPanes().clear();
        try {
            List<Module> modules = moduleDAO.findAll(); // Assuming student sees all available modules or adjust if there's a link mapping
            List<Ressource> allRessources = ressourceDAO.findAll();
            
            lblTotalModules.setText(String.valueOf(modules.size()));
            lblTotalRessources.setText(String.valueOf(allRessources.size()));

            for (Module m : modules) {
                // Get resources for this module
                List<Ressource> resList = allRessources.stream()
                        .filter(r -> r.getModuleId() == m.getId())
                        .collect(Collectors.toList());

                // Build Resource list view
                VBox contentBox = new VBox(0);
                
                if (resList.isEmpty()) {
                    Label noRes = new Label("Aucune ressource disponible pour ce module.");
                    noRes.setStyle("-fx-text-fill: #9ca3af; -fx-padding: 30; -fx-font-size: 14px;");
                    contentBox.getChildren().add(noRes);
                } else {
                    for (Ressource r : resList) {
                        contentBox.getChildren().add(createResourceRow(r));
                    }
                }

                TitledPane pane = new TitledPane();
                // Set custom title text
                pane.setText("📖 " + m.getCode() + "\n" + m.getIntitule());
                pane.setContent(contentBox);
                
                // Keep Accordion styling from css
                modulesAccordion.getPanes().add(pane);
            }

            if (!modulesAccordion.getPanes().isEmpty()) {
                modulesAccordion.setExpandedPane(modulesAccordion.getPanes().get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblTotalModules.setText("0");
            lblTotalRessources.setText("0");
        }
    }

    private HBox createResourceRow(Ressource r) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15 25; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-background-color: white;");

        // Icon
        Label icon = new Label("📄");
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #7e22ce; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 8; -fx-font-size: 16px;");

        // Title and Badges
        VBox texts = new VBox(6);
        Label title = new Label(r.getTitre());
        title.setStyle("-fx-font-weight: 900; -fx-font-size: 13px; -fx-text-fill: #111827;");
        
        HBox badges = new HBox(10);
        Label typeBadge = new Label(r.getType() != null ? r.getType().toUpperCase() : "PDF");
        typeBadge.setStyle("-fx-background-color: #1e3a8a; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");
        
        Label visBadge = new Label(r.isEstPublic() ? "Public" : "Privé");
        visBadge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");
        
        badges.getChildren().addAll(typeBadge, visBadge);
        texts.getChildren().addAll(title, badges);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDl = new Button("↓ Télécharger");
        btnDl.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: #111827; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 15; -fx-cursor: hand;");
        btnDl.setOnAction(e -> {
            String urlPath = r.getUrl();
            if (urlPath == null || urlPath.trim().isEmpty()) {
                showAlert("Erreur", "Le lien de cette ressource est vide ou invalide.", Alert.AlertType.WARNING);
                return;
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer la ressource : " + r.getTitre());
            
            // Generate a default file name
            String defaultName = r.getTitre().replaceAll("[^a-zA-Z0-9.-]", "_");
            if (urlPath.contains(".")) {
                String ext = urlPath.substring(urlPath.lastIndexOf("."));
                if (ext.length() <= 5) {
                    if (!defaultName.endsWith(ext)) defaultName += ext;
                } else {
                    defaultName += ".pdf"; // fallback
                }
            } else {
                defaultName += ".pdf"; // fallback
            }
            fileChooser.setInitialFileName(defaultName);

            Stage stage = (Stage) btnDl.getScene().getWindow();
            java.io.File dest = fileChooser.showSaveDialog(stage);

            if (dest != null) {
                // Background task to perform the download to not freeze the UI
                javafx.concurrent.Task<Void> downloadTask = new javafx.concurrent.Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (urlPath.startsWith("http://") || urlPath.startsWith("https://")) {
                            // Download from Web
                            try (java.io.InputStream in = new java.net.URL(urlPath).openStream()) {
                                java.nio.file.Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            }
                        } else {
                            // Copy local File
                            java.io.File src = new java.io.File(urlPath);
                            if (src.exists()) {
                                java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } else {
                                // Générer un fichier texte bouchon (mock) si le fichier n'existe pas en local
                                try (java.io.FileWriter writer = new java.io.FileWriter(dest)) {
                                    writer.write("---- LEARNHUB RESOURCE ----\n\n");
                                    writer.write("Le fichier d'origine de cette ressource est introuvable sur le disque.\n");
                                    writer.write("Chemin attendu : " + urlPath + "\n\n");
                                    writer.write("Ceci est une ressource de test générée automatiquement par la fonction de téléchargement.");
                                }
                            }
                        }
                        return null;
                    }
                };

                downloadTask.setOnSucceeded(ev -> {
                    showAlert("Succès", "La ressource a été téléchargée avec succès dans : \n" + dest.getAbsolutePath(), Alert.AlertType.INFORMATION);
                });

                downloadTask.setOnFailed(ev -> {
                    downloadTask.getException().printStackTrace();
                    showAlert("Erreur de téléchargement", "Impossible de télécharger la ressource : " + downloadTask.getException().getMessage(), Alert.AlertType.ERROR);
                });

                new Thread(downloadTask).start();
            }
        });

        Button btnAi = new Button("✨ analyser avec l'IA");
        btnAi.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 15; -fx-cursor: hand;");

        row.getChildren().addAll(icon, texts, spacer, btnDl, btnAi);
        return row;
    }

    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goNotes() { navigate("/fxml/student/notes.fxml", "Mes Notes"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml", "Mes Présences"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml", "Emploi du temps"); }
    @FXML private void goRdv() { navigate("/fxml/student/rdv.fxml", "Mes RDV"); }
    @FXML private void goStages() { navigate("/fxml/student/stages.fxml", "Stages"); }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) lblTotalModules.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, title);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) lblTotalModules.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
