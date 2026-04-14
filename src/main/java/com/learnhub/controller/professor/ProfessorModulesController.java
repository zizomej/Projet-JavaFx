package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.RessourceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Ressource;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import com.learnhub.util.DialogUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import java.io.IOException;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProfessorModulesController {

    @FXML private Label welcomeLabel;
    @FXML private VBox mainModulesContainer;
    @FXML private VBox moduleListContainer;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private Module selectedModule = null;

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText(user.getNomComplet());
        }

        loadModules(user != null ? user.getId() : 0);
    }

    private void loadModules(int profId) {
        if (mainModulesContainer != null) mainModulesContainer.getChildren().clear();
        if (moduleListContainer != null) moduleListContainer.getChildren().clear();
        
        try {
            List<Module> modules = moduleDAO.findByProfesseur(profId);
            for (Module m : modules) {
                if (mainModulesContainer != null) {
                    VBox moduleSection = createModuleSection(m);
                    mainModulesContainer.getChildren().add(moduleSection);
                }
                if (moduleListContainer != null) {
                    VBox smallCard = createLeftModuleCard(m);
                    moduleListContainer.getChildren().add(smallCard);
                }
            }
            if (modules.isEmpty()) {
                Label empty = new Label("Aucun module assigné");
                empty.setStyle("-fx-padding: 20; -fx-text-fill: #9ca3af; -fx-background-color: white;");
                if (mainModulesContainer != null) mainModulesContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createLeftModuleCard(Module m) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 2); -fx-border-radius: 12; -fx-border-color: #f3f4f6; -fx-border-width: 1;");
        card.setSpacing(8);

        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(m.getCode());
        badge.setStyle("-fx-background-color: #1a233a; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand; -fx-padding: 2; -fx-font-size: 10px; -fx-font-family: 'Segoe UI Emoji';");
        editBtn.setOnAction(evt -> {
            evt.consume();
            showModuleDialog(m);
        });

        Button delBtn = new Button("🗑️");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand; -fx-padding: 2; -fx-font-size: 10px; -fx-font-family: 'Segoe UI Emoji';");
        delBtn.setOnAction(evt -> {
            evt.consume();
            handleDeleteModule(m);
        });

        // Add subtle hover effects to buttons
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; -fx-cursor: hand; -fx-padding: 2; -fx-font-size: 10px; -fx-font-family: 'Segoe UI Emoji';"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand; -fx-padding: 2; -fx-font-size: 10px; -fx-font-family: 'Segoe UI Emoji';"));
        delBtn.setOnMouseEntered(e -> delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-padding: 2; -fx-font-size: 10px; -fx-font-family: 'Segoe UI Emoji';"));
        delBtn.setOnMouseExited(e -> delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand; -fx-padding: 2; -fx-font-size: 10px; -fx-font-family: 'Segoe UI Emoji';"));

        topBox.getChildren().addAll(badge, spacer, editBtn, delBtn);

        Label title = new Label(m.getIntitule());
        title.setStyle("-fx-font-weight: 900; -fx-font-size: 13px; -fx-text-fill: #111827; -fx-padding: 5 0 0 0; text-transform: uppercase;");

        HBox statsBox = new HBox(8);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Since Module doesn't have getNiveau, use a generic display like 'Filière' or 'Cycle'
        Label cycle = new Label("🎓 Cycle Ingénieur");
        cycle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 9px;");
        
        Label sem = new Label("📘 Semestre " + m.getSemestre());
        sem.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 9px;");
        
        Label creds = new Label("⭐ " + m.getCredits() + " crédits");
        creds.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 9px;");
        
        statsBox.getChildren().addAll(cycle, sem, creds);

        HBox resBox = new HBox(5);
        resBox.setAlignment(Pos.CENTER_LEFT);
        Label squareIcon = new Label("🟨");
        squareIcon.setStyle("-fx-font-size: 10px;");
        int count = countRessources(m.getId());
        String resText = count <= 1 ? count + " ressource" : count + " ressources";
        Label resLabel = new Label(resText);
        resLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");
        resBox.getChildren().addAll(squareIcon, resLabel);

        card.getChildren().addAll(topBox, title, statsBox, resBox);
        
        // Clic sur la carte pour filtrer
        card.setOnMouseClicked(e -> {
            if (selectedModule != null && selectedModule.getId() == m.getId()) {
                selectedModule = null; // Désélectionner
                filterRightSide(null);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 2); -fx-border-radius: 12; -fx-border-color: #f3f4f6; -fx-border-width: 1;");
            } else {
                selectedModule = m;
                // Visuellement désélectionner toutes les autres cartes (simple réinitialisation)
                for (javafx.scene.Node n : moduleListContainer.getChildren()) {
                    n.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 2); -fx-border-radius: 12; -fx-border-color: #f3f4f6; -fx-border-width: 1;");
                }
                card.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 2); -fx-border-radius: 12; -fx-border-color: #3b82f6; -fx-border-width: 1;");
                filterRightSide(m);
            }
        });
        
        return card;
    }

    private void filterRightSide(Module m) {
        if (mainModulesContainer == null) return;
        mainModulesContainer.getChildren().clear();
        if (m == null) {
            Utilisateur user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                try {
                    List<Module> modules = moduleDAO.findByProfesseur(user.getId());
                    for (Module mod : modules) {
                        mainModulesContainer.getChildren().add(createModuleSection(mod));
                    }
                } catch (SQLException e) { e.printStackTrace(); }
            }
        } else {
            mainModulesContainer.getChildren().add(createModuleSection(m));
        }
    }

    private VBox createModuleSection(Module m) {
        VBox section = new VBox();
        section.setSpacing(15);
        section.setStyle("-fx-background-color: transparent;");

        // Header: Dark blue banner
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a233a; -fx-padding: 12 24; -fx-background-radius: 8;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(m.getCode() + " - " + m.getIntitule());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int count = countRessources(m.getId());
        String resText = count <= 1 ? count + " ressource" : count + " ressources";
        Label resBadge = new Label(resText);
        resBadge.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-weight: bold;");

        // Bouton Ajouter Ressource comme l'ancien code
        Button addResBtn = new Button("➕");
        addResBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 14px;");
        addResBtn.setOnAction(e -> handleAddRessource(m));

        header.getChildren().addAll(title, spacer, resBadge, addResBtn);

        // Cards Container
        VBox cardsContainer = new VBox();
        cardsContainer.setSpacing(15);
        cardsContainer.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        try {
            List<Ressource> ressources = ressourceDAO.findByModule(m.getId());
            for (Ressource r : ressources) {
                cardsContainer.getChildren().add(createRessourceCard(r, m));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        section.getChildren().addAll(header, cardsContainer);
        return section;
    }

    private int countRessources(int modId) {
        try { return ressourceDAO.findByModule(modId).size(); }
        catch (SQLException e) { return 0; }
    }

    private VBox createRessourceCard(Ressource r, Module currentModule) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 15, 0, 0, 4);");
        card.setSpacing(20);

        // TOP HBOX
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.TOP_LEFT);

        // Left Content (VBox)
        VBox leftContent = new VBox();
        leftContent.setSpacing(10);
        leftContent.setAlignment(Pos.TOP_LEFT);

        // Folder Icon (Orange square, rounded corners)
        Label folderIcon = new Label("📂"); // Using emoji or just text. Actually a simple style gives a great feel
        folderIcon.setStyle("-fx-font-size: 32px; -fx-text-fill: #f59e0b; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffedd5, #ffb347); -fx-background-radius: 12; -fx-padding: 8 16;");

        Label title = new Label(r.getTitre());
        title.setStyle("-fx-font-weight: 900; -fx-font-size: 16px; -fx-text-fill: #0f172a; -fx-text-transform: uppercase;");

        Label typePill = new Label(r.getType() != null && !r.getType().isEmpty() ? r.getType() : "PDF");
        typePill.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #000000; -fx-font-weight: 900; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 16;");

        HBox attachBox = new HBox(6);
        attachBox.setAlignment(Pos.CENTER_LEFT);
        Label clip = new Label("📎");
        clip.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        Label fileName = new Label(r.getUrl() != null ? r.getUrl() : "Ressource_File.pdf");
        fileName.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px; -fx-font-weight: normal; -fx-cursor: hand;");
        attachBox.getChildren().addAll(clip, fileName);

        leftContent.getChildren().addAll(folderIcon, title, typePill, attachBox);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.TOP_RIGHT);
        
        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px; -fx-font-family: 'Segoe UI Emoji';");
        editBtn.setOnAction(e -> handleEditRessource(r, currentModule));

        Button delBtn = new Button("🗑️");
        delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 14px; -fx-font-family: 'Segoe UI Emoji';");
        delBtn.setOnAction(e -> handleDeleteRessource(r, currentModule));

        actions.getChildren().addAll(editBtn, delBtn);

        topBox.getChildren().addAll(leftContent, spacer1, actions);

        // BOTTOM HBOX (Visibility)
        HBox bottomBox = new HBox(12);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        Label visLabel = new Label("Visible pour tous :");
        visLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: 600;");

        // Custom iOS-like Toggle
        StackPane toggleSwitch = createToggleSwitch(r.isEstPublic(), r);

        bottomBox.getChildren().addAll(visLabel, toggleSwitch);

        card.getChildren().addAll(topBox, bottomBox);
        return card;
    }

    private StackPane createToggleSwitch(boolean isPublic, Ressource r) {
        StackPane toggle = new StackPane();
        toggle.setPrefSize(44, 22);
        toggle.setMinSize(44, 22);
        toggle.setMaxSize(44, 22);
        toggle.setStyle("-fx-cursor: hand;");

        Rectangle bg = new Rectangle(44, 22);
        bg.setArcWidth(22);
        bg.setArcHeight(22);
        bg.setFill(isPublic ? Color.valueOf("#10b981") : Color.valueOf("#e2e8f0"));

        Circle knob = new Circle(9);
        knob.setFill(Color.WHITE);
        DropShadow ds = new DropShadow();
        ds.setRadius(3);
        ds.setOffsetY(1);
        ds.setColor(Color.rgb(0,0,0,0.2));
        knob.setEffect(ds);

        StackPane.setAlignment(knob, isPublic ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(knob, new javafx.geometry.Insets(0, 2, 0, 2));

        toggle.getChildren().addAll(bg, knob);

        toggle.setOnMouseClicked(e -> {
            boolean newState = !r.isEstPublic();
            r.setEstPublic(newState);
            bg.setFill(newState ? Color.valueOf("#10b981") : Color.valueOf("#e2e8f0"));
            StackPane.setAlignment(knob, newState ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            try { ressourceDAO.update(r); } catch(Exception ex) { ex.printStackTrace(); }
        });

        return toggle;
    }

    @FXML
    private void handleRetour() {
        // Implement return logic or simply do nothing since this is full view
        goDashboard();
    }

    private void handleDeleteModule(Module m) {
        boolean confirmed = DialogUtil.showDeleteConfirmation("le module et TOUTES ses ressources", m.getIntitule());
        if (confirmed) {
            try {
                // Suppression de toutes les ressources
                List<Ressource> res = ressourceDAO.findByModule(m.getId());
                for (Ressource r : res) {
                    ressourceDAO.delete(r.getId());
                }

                // Suppression manuelle en cascade pour gérer les clés étrangères de `seance`, `presence` et `note`
                try (java.sql.PreparedStatement ps1 = com.learnhub.util.DatabaseConnection.getInstance().prepareStatement("DELETE FROM presence WHERE seance_id IN (SELECT id FROM seance WHERE module_id=?)")) {
                    ps1.setInt(1, m.getId()); 
                    ps1.executeUpdate();
                }
                try (java.sql.PreparedStatement ps2 = com.learnhub.util.DatabaseConnection.getInstance().prepareStatement("DELETE FROM seance WHERE module_id=?")) {
                    ps2.setInt(1, m.getId()); 
                    ps2.executeUpdate();
                }
                try (java.sql.PreparedStatement ps3 = com.learnhub.util.DatabaseConnection.getInstance().prepareStatement("DELETE FROM note WHERE module_id=?")) {
                    ps3.setInt(1, m.getId()); 
                    ps3.executeUpdate();
                }

                // Finalement, on supprime le module !
                moduleDAO.delete(m.getId());
                
                selectedModule = null; // Reset selection si existante
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadModules(user.getId());
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddModule() {
        showModuleDialog(null);
    }

    // Since add resource is not directly in the FXML as a global button, 
    // it could be called via the module header if you add a button there.
    private void handleAddRessource(Module currentModule) {
        if (currentModule == null) {
            showAlert("Action impossible", "Sélectionnez d'abord un module.");
            return;
        }
        showRessourceDialog(null, currentModule);
    }

    private void handleEditRessource(Ressource r, Module currentModule) {
        showRessourceDialog(r, currentModule);
    }

    private void handleDeleteRessource(Ressource r, Module currentModule) {
        boolean confirmed = DialogUtil.showDeleteConfirmation("la ressource", r.getTitre());
        if (confirmed) {
            try {
                ressourceDAO.delete(r.getId());
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadModules(user.getId());
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    private void showRessourceDialog(Ressource ressource, Module currentModule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professor/ressource_form.fxml"));
            Parent root = loader.load();
            RessourceFormController controller = loader.getController();
            controller.setModule(currentModule);
            controller.setRessource(ressource);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));

            controller.setOnSuccess(() -> {
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadModules(user.getId());
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de ressource.");
        }
    }

    private void showModuleDialog(Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professor/module_form.fxml"));
            Parent root = loader.load();
            ModuleFormController controller = loader.getController();
            controller.setModule(module);


            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));

            controller.setOnSuccess(() -> {
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadModules(user.getId());
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de module.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // NAVIGATION METHODS
    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Mes Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Mes Séances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Gestion des Notes"); }
    @FXML private void goPresences() { navigate("/fxml/professor/presences.fxml", "Présences"); }

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

