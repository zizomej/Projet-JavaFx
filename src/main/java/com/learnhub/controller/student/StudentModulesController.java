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

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (topUserName != null) topUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            loadModules();
        }
    }

    private void loadModules() {
        modulesAccordion.getPanes().clear();
        try {
            List<Module> modules = moduleDAO.findAll();
            List<Ressource> allRessources = ressourceDAO.findAll();

            if (lblTotalModules != null) lblTotalModules.setText(String.valueOf(modules.size()));
            if (lblTotalRessources != null) lblTotalRessources.setText(String.valueOf(allRessources.size()));

            for (Module m : modules) {
                List<Ressource> resList = allRessources.stream()
                    .filter(r -> r.getModuleId() == m.getId())
                    .collect(Collectors.toList());

                VBox contentBox = new VBox(0);
                if (resList.isEmpty()) {
                    Label noRes = new Label("Aucune ressource disponible pour ce module.");
                    noRes.setStyle("-fx-text-fill: #9ca3af; -fx-padding: 30; -fx-font-size: 14px;");
                    contentBox.getChildren().add(noRes);
                } else {
                    for (Ressource r : resList) contentBox.getChildren().add(createResourceRow(r));
                }

                TitledPane pane = new TitledPane();
                pane.setText("📖 " + m.getCode() + " — " + m.getIntitule());
                pane.setContent(contentBox);
                modulesAccordion.getPanes().add(pane);
            }

            if (!modulesAccordion.getPanes().isEmpty())
                modulesAccordion.setExpandedPane(modulesAccordion.getPanes().get(0));

        } catch (SQLException e) {
            e.printStackTrace();
            if (lblTotalModules != null) lblTotalModules.setText("0");
            if (lblTotalRessources != null) lblTotalRessources.setText("0");
        }
    }

    private HBox createResourceRow(Ressource r) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15 25; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-background-color: white;");

        Label icon = new Label("📄");
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #7e22ce; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 8; -fx-font-size: 16px;");

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
        btnDl.setOnAction(e -> downloadRessource(r, btnDl));

        row.getChildren().addAll(icon, texts, spacer, btnDl);
        return row;
    }

    private void downloadRessource(Ressource r, javafx.scene.Node node) {
        String urlPath = r.getUrl();
        if (urlPath == null || urlPath.trim().isEmpty()) {
            showAlert("Erreur", "Le lien de cette ressource est vide.", Alert.AlertType.WARNING);
            return;
        }
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer la ressource : " + r.getTitre());
        String defaultName = r.getTitre().replaceAll("[^a-zA-Z0-9.-]", "_");
        if (urlPath.contains(".")) {
            String ext = urlPath.substring(urlPath.lastIndexOf("."));
            if (ext.length() <= 5 && !defaultName.endsWith(ext)) defaultName += ext;
        } else defaultName += ".pdf";
        fileChooser.setInitialFileName(defaultName);
        Stage stage = (Stage) node.getScene().getWindow();
        java.io.File dest = fileChooser.showSaveDialog(stage);
        if (dest != null) {
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override protected Void call() throws Exception {
                    if (urlPath.startsWith("http://") || urlPath.startsWith("https://")) {
                        try (java.io.InputStream in = new java.net.URL(urlPath).openStream()) {
                            java.nio.file.Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } else {
                        java.io.File src = new java.io.File(urlPath);
                        if (src.exists()) java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    return null;
                }
            };
            task.setOnSucceeded(ev -> showAlert("Succès", "Téléchargé : " + dest.getAbsolutePath(), Alert.AlertType.INFORMATION));
            task.setOnFailed(ev -> showAlert("Erreur", "Téléchargement impossible.", Alert.AlertType.ERROR));
            new Thread(task).start();
        }
    }

    @FXML private void goDashboard()     { navigate("/fxml/student/dashboard.fxml",     "Tableau de bord"); }
    @FXML private void goModules()       { navigate("/fxml/student/modules.fxml",        "Mes Modules"); }
    @FXML private void goNotes()         { navigate("/fxml/student/notes.fxml",          "Mes Notes"); }
    @FXML private void goEmploi()        { navigate("/fxml/student/emploi.fxml",         "Emploi du temps"); }
    @FXML private void goPresences()     { navigate("/fxml/student/presences.fxml",      "Mes Présences"); }
    @FXML private void goRdv()           { navigate("/fxml/student/rdv.fxml",            "Mes RDV"); }
    @FXML private void goEvenements()    { navigate("/fxml/student/evenements.fxml",     "Événements"); }
    @FXML private void goOffresStage()   { navigate("/fxml/student/offres_stage.fxml",   "Offres de Stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/student/demandes_stage.fxml", "Mes Candidatures"); }
    @FXML private void goStages()        { navigate("/fxml/student/offres_stage.fxml",   "Stages"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) modulesAccordion.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) modulesAccordion.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, title);
        } catch (Exception e) { e.printStackTrace(); }
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
