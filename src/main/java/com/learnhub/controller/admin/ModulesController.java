package com.learnhub.controller.admin;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.RessourceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Ressource;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModulesController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboSemestre;
    @FXML private ComboBox<String> comboTrier;
    @FXML private VBox modulesContainer;
    @FXML private VBox resourcesContainer;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();

    private List<Module> allModules = new ArrayList<>();
    private List<Ressource> allRessources = new ArrayList<>();

    @FXML
    public void initialize() {
        comboSemestre.getItems().addAll("Tous les semestres", "Semestre 1", "Semestre 2", "Semestre 3", "Semestre 4");
        comboSemestre.setValue("Tous les semestres");

        comboTrier.getItems().addAll("Trier par: Semestre", "Trier par: Crédits", "Trier par: Nom");
        comboTrier.setValue("Trier par: Semestre");

        loadData();

        searchField.textProperty().addListener((obs, old, val) -> buildUI());
        comboSemestre.valueProperty().addListener((obs, old, val) -> buildUI());
    }

    private void loadData() {
        try {
            allModules = moduleDAO.findAll();
            allRessources = ressourceDAO.findAll();
            buildUI();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buildUI() {
        modulesContainer.getChildren().clear();
        resourcesContainer.getChildren().clear();

        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String semFilter = comboSemestre.getValue();

        List<Module> filteredModules = new ArrayList<>();
        for (Module m : allModules) {
            boolean matchesSearch = m.getIntitule().toLowerCase().contains(search) || m.getCode().toLowerCase().contains(search);
            boolean matchesSemestre = semFilter.equals("Tous les semestres") || ("Semestre " + m.getSemestre()).equals(semFilter);
            if (matchesSearch && matchesSemestre) {
                filteredModules.add(m);
            }
        }

        // --- Left Panel : Modules ---
        for (Module m : filteredModules) {
            long countRes = allRessources.stream().filter(r -> r.getModuleId() == m.getId()).count();

            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");

            Label codeLabel = new Label(m.getCode());
            codeLabel.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 10; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label titleLabel = new Label(m.getIntitule());
            titleLabel.setStyle("-fx-font-weight: 900; -fx-font-size: 15px; -fx-text-fill: #0A1F44; -fx-wrap-text: true;");

            HBox infoBox = new HBox(12);
            Label niveauLabel = new Label("🎓 Master");
            niveauLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
            
            Label semestreLabel = new Label("📅 Semestre " + m.getSemestre());
            semestreLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
            
            Label creditsLabel = new Label("⭐ " + m.getCredits() + " crédits");
            creditsLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            infoBox.getChildren().addAll(niveauLabel, semestreLabel, creditsLabel);

            Label resCountLabel = new Label("📁 " + countRes + " ressources");
            resCountLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

            card.getChildren().addAll(codeLabel, titleLabel, infoBox, resCountLabel);
            modulesContainer.getChildren().add(card);
        }

        // --- Right Panel : Ressources by Module ---
        Map<Integer, List<Ressource>> resByModuleId = allRessources.stream().collect(Collectors.groupingBy(Ressource::getModuleId));

        for (Module m : filteredModules) {
            List<Ressource> resList = resByModuleId.getOrDefault(m.getId(), new ArrayList<>());
            if (resList.isEmpty()) continue; // Skip displaying modules with no resources

            VBox group = new VBox(15);
            group.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-padding: 0 0 15 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");

            // Header for module resources
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle("-fx-background-color: #0A1F44; -fx-padding: 12 20; -fx-background-radius: 11 11 0 0;");
            
            Label groupTitle = new Label(m.getCode() + " - " + m.getIntitule());
            groupTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label countBadge = new Label(resList.size() + " ressources");
            countBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            header.getChildren().addAll(groupTitle, spacer, countBadge);
            group.getChildren().add(header);

            // Resources
            VBox resItems = new VBox(10);
            resItems.setStyle("-fx-padding: 15 20;");
            
            for(Ressource r : resList) {
                HBox resCard = new HBox(15);
                resCard.setAlignment(Pos.CENTER_LEFT);
                resCard.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #e5e7eb transparent; -fx-padding: 0 0 15 0;");

                // Icon Background
                Label icon = new Label("📄");
                icon.setAlignment(Pos.CENTER);
                icon.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFC107, #f59e0b); -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 10; -fx-font-size: 20px; -fx-text-fill: white;");

                VBox details = new VBox(6);
                Label resTitle = new Label(r.getTitre());
                resTitle.setStyle("-fx-font-weight: 900; -fx-text-fill: #1E3A8A; -fx-font-size: 14px;");
                
                HBox tags = new HBox(10);
                tags.setAlignment(Pos.CENTER_LEFT);
                Label typeTag = new Label(r.getType() != null ? r.getType().toUpperCase() : "PDF");
                typeTag.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold;");
                
                Label fileTag = new Label("🔗 " + r.getUrl());
                fileTag.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
                tags.getChildren().addAll(typeTag, fileTag);

                HBox accessBox = new HBox(10);
                accessBox.setAlignment(Pos.CENTER_LEFT);
                Label visPrefix = new Label("Visibilité :");
                visPrefix.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 11px;");
                
                Label visBadge = new Label(r.isEstPublic() ? "Public" : "Privé");
                visBadge.setStyle(r.isEstPublic() ? "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;" : "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                accessBox.getChildren().addAll(visPrefix, visBadge);

                details.getChildren().addAll(resTitle, tags, accessBox);
                
                Region rSpacer = new Region();
                HBox.setHgrow(rSpacer, Priority.ALWAYS);

                Button btnDel = new Button("🗑");
                btnDel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 14px;");
                btnDel.setOnAction(e -> deleteResource(r.getId()));

                resCard.getChildren().addAll(icon, details, rSpacer, btnDel);
                resItems.getChildren().add(resCard);
            }
            
            group.getChildren().add(resItems);
            resourcesContainer.getChildren().add(group);
        }
        
        if (resourcesContainer.getChildren().isEmpty()) {
            Label noRes = new Label("📭 Aucune ressource trouvée pour les modules actuels.");
            noRes.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-padding: 30;");
            resourcesContainer.getChildren().add(noRes);
        }
    }

    private void deleteResource(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                ressourceDAO.delete(id);
                loadData();
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la ressource.");
                e.printStackTrace();
            }
        }
    }

    @FXML private void goDashboard() { navigate("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goNotes() { navigate("/fxml/admin/notes.fxml", "Gestion des Notes"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) searchField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String file, String title) {
        Stage stage = (Stage) searchField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, file, title);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
