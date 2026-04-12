package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.RessourceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Ressource;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProfessorModulesController {

    @FXML private Label welcomeLabel;
    @FXML private VBox moduleListContainer;
    @FXML private VBox ressourcesListContainer;
    @FXML private Label selectedModuleLabel;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private Module currentModule = null;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) welcomeLabel.setText(user.getNomComplet());
        loadModules(user != null ? user.getId() : 0);
    }

    private void loadModules(int profId) {
        moduleListContainer.getChildren().clear();
        try {
            List<Module> modules = moduleDAO.findByProfesseur(profId);
            for (Module m : modules) moduleListContainer.getChildren().add(createModuleCard(m));
            if (!modules.isEmpty()) selectModule(modules.get(0));
            else if (selectedModuleLabel != null) selectedModuleLabel.setText("Aucun module assigné");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox createModuleCard(Module m) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-cursor: hand;");

        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(m.getCode());
        badge.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; -fx-cursor: hand; -fx-padding: 2;");
        editBtn.setOnAction(evt -> { evt.consume(); showModuleDialog(m); });

        Button delBtn = new Button("🗑️");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-padding: 2;");
        delBtn.setOnAction(evt -> { evt.consume(); handleDeleteModule(m); });

        topBox.getChildren().addAll(badge, spacer, editBtn, delBtn);

        Label title = new Label(m.getIntitule());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");

        Label info = new Label("Semestre " + m.getSemestre() + " • " + m.getCredits() + " crédits");
        info.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");

        Label resInfo = new Label("📚 " + countRessources(m.getId()) + " ressources");
        resInfo.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 10px; -fx-font-weight: bold;");

        card.getChildren().addAll(topBox, title, info, resInfo);
        card.setSpacing(4);

        card.setOnMouseClicked(e -> {
            for (javafx.scene.Node n : moduleListContainer.getChildren())
                n.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-cursor: hand;");
            card.setStyle("-fx-background-color: #fafafa; -fx-border-color: #1E3A8A; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 11; -fx-effect: dropshadow(gaussian, rgba(30,58,138,0.2), 8, 0, 0, 3); -fx-cursor: default;");
            selectModule(m);
        });

        return card;
    }

    private int countRessources(int modId) {
        try { return ressourceDAO.findByModule(modId).size(); } catch (SQLException e) { return 0; }
    }

    private void selectModule(Module m) {
        currentModule = m;
        if (selectedModuleLabel != null) selectedModuleLabel.setText(m.getCode() + " - " + m.getIntitule());
        loadRessourcesForModule(m.getId());
    }

    private void loadRessourcesForModule(int moduleId) {
        ressourcesListContainer.getChildren().clear();
        try {
            List<Ressource> ressources = ressourceDAO.findByModule(moduleId);
            if (ressources.isEmpty()) {
                Label empty = new Label("Aucune ressource pour ce module");
                empty.setStyle("-fx-padding: 20; -fx-text-fill: #9ca3af; -fx-background-color: white;");
                ressourcesListContainer.getChildren().add(empty);
                return;
            }
            for (Ressource r : ressources) ressourcesListContainer.getChildren().add(createRessourceCard(r));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HBox createRessourceCard(Ressource r) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: white; -fx-padding: 16 20; -fx-alignment: center-left; -fx-spacing: 15;");

        Label icon = new Label("📄");
        icon.setStyle("-fx-background-color: linear-gradient(to right, #FFC107, #f59e0b); -fx-text-fill: white; -fx-padding: 10 12; -fx-background-radius: 8; -fx-font-size: 18px;");

        VBox texts = new VBox();
        texts.setSpacing(2);
        Label title = new Label(r.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
        Label type = new Label(r.getType());
        type.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #0A1F44; -fx-font-weight: bold; -fx-font-size: 9px; -fx-padding: 1 6; -fx-background-radius: 10;");
        Label path = new Label("📄 " + r.getUrl());
        path.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px;");
        texts.getChildren().addAll(title, type, path);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button toggleBtn = new Button(r.isEstPublic() ? "Public" : "Privé");
        toggleBtn.setStyle(r.isEstPublic()
            ? "-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 10px;"
            : "-fx-background-color: #d1d5db; -fx-text-fill: #374151; -fx-background-radius: 12; -fx-font-size: 10px;");
        toggleBtn.setOnAction(e -> {
            r.setEstPublic(!r.isEstPublic());
            try { ressourceDAO.update(r); if (currentModule != null) loadRessourcesForModule(currentModule.getId()); } catch (Exception ex) {}
        });

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-cursor: hand; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> showRessourceDialog(r));

        Button delBtn = new Button("🗑️");
        delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-background-radius: 6;");
        delBtn.setOnAction(e -> handleDeleteRessource(r));

        HBox actions = new HBox(8, toggleBtn, editBtn, delBtn);
        row.getChildren().addAll(icon, texts, spacer, actions);
        return row;
    }

    @FXML
    private void handleRetour() {
        currentModule = null;
        if (selectedModuleLabel != null) selectedModuleLabel.setText("Sélectionnez un module");
        ressourcesListContainer.getChildren().clear();
        for (javafx.scene.Node n : moduleListContainer.getChildren())
            n.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-cursor: hand;");
    }

    private void handleDeleteModule(Module m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce module ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                moduleDAO.delete(m.getId());
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadModules(user.getId());
                handleRetour();
            } catch (SQLException e) { new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show(); }
        }
    }

    @FXML private void handleAddModule() { showModuleDialog(null); }

    @FXML
    private void handleAddRessource() {
        if (currentModule == null) { new Alert(Alert.AlertType.WARNING, "Sélectionnez d'abord un module.").show(); return; }
        showRessourceDialog(null);
    }

    private void handleDeleteRessource(Ressource r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                ressourceDAO.delete(r.getId());
                if (currentModule != null) loadRessourcesForModule(currentModule.getId());
            } catch (SQLException e) { new Alert(Alert.AlertType.ERROR, e.getMessage()).show(); }
        }
    }

    private void showRessourceDialog(Ressource ressource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professor/ressource_form.fxml"));
            Parent root = loader.load();
            RessourceFormController ctrl = loader.getController();
            ctrl.setModule(currentModule);
            ctrl.setRessource(ressource);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            ctrl.setOnSuccess(() -> { if (currentModule != null) loadRessourcesForModule(currentModule.getId()); });
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showModuleDialog(Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professor/module_form.fxml"));
            Parent root = loader.load();
            ModuleFormController ctrl = loader.getController();
            ctrl.setModule(module);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            ctrl.setOnSuccess(() -> {
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadModules(user.getId());
            });
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void goDashboard()  { navigate("/fxml/professor/dashboard.fxml",  "Tableau de bord"); }
    @FXML private void goModules()    { navigate("/fxml/professor/modules.fxml",    "Mes Modules"); }
    @FXML private void goSeances()    { navigate("/fxml/professor/seances.fxml",    "Mes Séances"); }
    @FXML private void goNotes()      { navigate("/fxml/professor/notes.fxml",      "Gestion des Notes"); }
    @FXML private void goPresences()  { navigate("/fxml/professor/presences.fxml",  "Présences"); }
    @FXML private void goEvenements() { navigate("/fxml/professor/evenements.fxml", "Événements"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
