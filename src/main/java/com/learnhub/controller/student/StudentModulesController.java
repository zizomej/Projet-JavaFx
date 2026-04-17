package com.learnhub.controller.student;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.RessourceDAO;
import com.learnhub.dao.QuizDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Ressource;
import com.learnhub.models.Quiz;
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

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private final QuizDAO quizDAO = new QuizDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            topUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            loadModules();
        }
    }

    private void loadModules() {
        modulesAccordion.getPanes().clear();
        try {
            List<Module> modules = moduleDAO.findAll(); 
            List<Ressource> allRessources = ressourceDAO.findAll();
            
            lblTotalModules.setText(String.valueOf(modules.size()));
            
            int totalResEtQuizzes = allRessources.size();

            for (Module m : modules) {
                // Get resources for this module
                List<Ressource> resList = allRessources.stream()
                        .filter(r -> r.getModuleId() == m.getId() && r.isEstPublic())
                        .collect(Collectors.toList());
                        
                // Get quizzes for this module using proper method
                List<Quiz> quizList = quizDAO.getVisibleQuizzesByModule(m.getId());
                totalResEtQuizzes += quizList.size();

                // Build Resource list view
                VBox contentBox = new VBox(0);
                
                if (resList.isEmpty() && quizList.isEmpty()) {
                    Label noRes = new Label("Aucune ressource ou quiz disponible pour ce module.");
                    noRes.setStyle("-fx-text-fill: #9ca3af; -fx-padding: 30; -fx-font-size: 14px;");
                    contentBox.getChildren().add(noRes);
                } else {
                    for (Ressource r : resList) {
                        contentBox.getChildren().add(createResourceRow(r));
                    }
                    for (Quiz q : quizList) {
                        contentBox.getChildren().add(createQuizRow(q));
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
            
            lblTotalRessources.setText(String.valueOf(totalResEtQuizzes));

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
        btnAi.setOnAction(e -> openAiDialog());

        row.getChildren().addAll(icon, texts, spacer, btnDl, btnAi);
        return row;
    }

    private HBox createQuizRow(Quiz q) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15 25; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-background-color: #f8fafc;");

        // Icon
        Label icon = new Label("📝");
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 8; -fx-font-size: 16px;");

        // Title and Badges
        VBox texts = new VBox(6);
        Label title = new Label(q.getTitre());
        title.setStyle("-fx-font-weight: 900; -fx-font-size: 13px; -fx-text-fill: #111827;");
        
        HBox badges = new HBox(10);
        Label typeBadge = new Label("QUIZ");
        typeBadge.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");
        
        Label deadlineBadge = new Label(q.getDeadline() != null ? "Due: " + q.getDeadline().toLocalDate().toString() : "Aucune date");
        deadlineBadge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");
        
        badges.getChildren().addAll(typeBadge, deadlineBadge);
        texts.getChildren().addAll(title, badges);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTake = new Button("▶ Passer ce Quiz");
        btnTake.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 6 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3), 8, 0, 0, 2);");
        btnTake.setOnAction(e -> handleTakeQuiz(q));

        row.getChildren().addAll(icon, texts, spacer, btnTake);
        return row;
    }

    private void handleTakeQuiz(Quiz q) {
        // Rediriger vers l'espace de passage de quiz ou afficher un dialog
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/student/quiz_take.fxml"));
            javafx.scene.Parent root = loader.load();
            com.learnhub.controller.student.QuizTakeController controller = loader.getController();
            controller.initQuiz(q);

            Stage stage = new Stage();
            stage.setTitle("Passer le Quiz : " + q.getTitre());
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface du quiz.", Alert.AlertType.ERROR);
        }
    }

    private void openAiDialog() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/student/ai_analysis_dialog.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de l'Assistant IA.", Alert.AlertType.ERROR);
        }
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
