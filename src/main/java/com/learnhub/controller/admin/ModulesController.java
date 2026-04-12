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

        if (searchField != null) searchField.textProperty().addListener((obs, old, val) -> buildUI());
        if (comboSemestre != null) comboSemestre.valueProperty().addListener((obs, old, val) -> buildUI());
    }

    private void loadData() {
        try {
            allModules = moduleDAO.findAll();
            allRessources = ressourceDAO.findAll();
            buildUI();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void buildUI() {
        modulesContainer.getChildren().clear();
        resourcesContainer.getChildren().clear();

        String search = searchField == null || searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String semFilter = comboSemestre == null ? "Tous les semestres" : comboSemestre.getValue();

        List<Module> filtered = new ArrayList<>();
        for (Module m : allModules) {
            boolean matchSearch = m.getIntitule().toLowerCase().contains(search) || m.getCode().toLowerCase().contains(search);
            boolean matchSem = semFilter == null || semFilter.equals("Tous les semestres") || ("Semestre " + m.getSemestre()).equals(semFilter);
            if (matchSearch && matchSem) filtered.add(m);
        }

        // Left: Module cards
        for (Module m : filtered) {
            long countRes = allRessources.stream().filter(r -> r.getModuleId() == m.getId()).count();

            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");

            Label codeLabel = new Label(m.getCode());
            codeLabel.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 10; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label titleLabel = new Label(m.getIntitule());
            titleLabel.setStyle("-fx-font-weight: 900; -fx-font-size: 15px; -fx-text-fill: #0A1F44; -fx-wrap-text: true;");

            HBox infoBox = new HBox(12);
            Label semestreLabel = new Label("📅 Semestre " + m.getSemestre());
            semestreLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
            Label creditsLabel = new Label("⭐ " + m.getCredits() + " crédits");
            creditsLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 11px; -fx-font-weight: bold;");
            infoBox.getChildren().addAll(semestreLabel, creditsLabel);

            Label resCountLabel = new Label("📁 " + countRes + " ressources");
            resCountLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

            HBox actions = new HBox(8);
            Button delBtn = new Button("🗑 Supprimer");
            delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
            delBtn.setOnAction(e -> deleteModule(m.getId()));
            actions.getChildren().add(delBtn);

            card.getChildren().addAll(codeLabel, titleLabel, infoBox, resCountLabel, actions);
            modulesContainer.getChildren().add(card);
        }

        // Right: Resources grouped by module
        Map<Integer, List<Ressource>> resByModuleId = allRessources.stream().collect(Collectors.groupingBy(Ressource::getModuleId));

        for (Module m : filtered) {
            List<Ressource> resList = resByModuleId.getOrDefault(m.getId(), new ArrayList<>());
            if (resList.isEmpty()) continue;

            VBox group = new VBox(15);
            group.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-padding: 0 0 15 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");

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

            VBox resItems = new VBox(10);
            resItems.setStyle("-fx-padding: 15 20;");
            for (Ressource r : resList) {
                HBox resCard = new HBox(15);
                resCard.setAlignment(Pos.CENTER_LEFT);
                resCard.setStyle("-fx-border-color: transparent transparent #e5e7eb transparent; -fx-padding: 0 0 15 0;");

                Label icon = new Label("📄");
                icon.setAlignment(Pos.CENTER);
                icon.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFC107, #f59e0b); -fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 10; -fx-font-size: 20px;");

                VBox details = new VBox(6);
                Label resTitle = new Label(r.getTitre());
                resTitle.setStyle("-fx-font-weight: 900; -fx-text-fill: #1E3A8A; -fx-font-size: 14px;");
                HBox tags = new HBox(10);
                Label typeTag = new Label(r.getType() != null ? r.getType().toUpperCase() : "PDF");
                typeTag.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 2 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold;");
                Label visBadge = new Label(r.isEstPublic() ? "Public" : "Privé");
                visBadge.setStyle(r.isEstPublic()
                    ? "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                tags.getChildren().addAll(typeTag, visBadge);
                details.getChildren().addAll(resTitle, tags);

                Region rSpacer = new Region();
                HBox.setHgrow(rSpacer, Priority.ALWAYS);

                Button btnDel = new Button("🗑");
                btnDel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand;");
                btnDel.setOnAction(e -> deleteResource(r.getId()));

                resCard.getChildren().addAll(icon, details, rSpacer, btnDel);
                resItems.getChildren().add(resCard);
            }
            group.getChildren().add(resItems);
            resourcesContainer.getChildren().add(group);
        }

        if (resourcesContainer.getChildren().isEmpty()) {
            Label noRes = new Label("📭 Aucune ressource pour les modules actuels.");
            noRes.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-padding: 30;");
            resourcesContainer.getChildren().add(noRes);
        }
    }

    private void deleteModule(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce module ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try { moduleDAO.delete(id); loadData(); }
            catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
        }
    }

    private void deleteResource(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try { ressourceDAO.delete(id); loadData(); }
            catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
        }
    }

    @FXML private void goDashboard()      { nav("/fxml/admin/dashboard.fxml",       "Tableau de bord"); }
    @FXML private void goUtilisateurs()   { nav("/fxml/admin/utilisateurs.fxml",    "Utilisateurs"); }
    @FXML private void goModules()        { loadData(); }
    @FXML private void goSeances()        { nav("/fxml/admin/seances.fxml",         "Séances"); }
    @FXML private void goNotes()          { nav("/fxml/admin/notes.fxml",           "Notes"); }
    @FXML private void goPresences()      { nav("/fxml/admin/presences.fxml",       "Présences"); }
    @FXML private void goFilieres()       { nav("/fxml/admin/filieres.fxml",        "Filières"); }
    @FXML private void goEvenements()     { nav("/fxml/admin/evenements.fxml",      "Événements"); }
    @FXML private void goRdv()            { nav("/fxml/admin/rdv.fxml",             "RDV Médicaux"); }
    @FXML private void goCreneaux()       { nav("/fxml/admin/creneaux.fxml",        "Créneaux"); }
    @FXML private void goPartenaires()    { nav("/fxml/admin/partenaires.fxml",     "Partenaires"); }
    @FXML private void goOffresStage()    { nav("/fxml/admin/offres_stage.fxml",    "Offres de Stage"); }
    @FXML private void goDemandesStage()  { nav("/fxml/admin/demandes_stage.fxml",  "Demandes de Stage"); }
    @FXML private void goBack()           { nav("/fxml/admin/dashboard.fxml",       "Tableau de bord"); }

    private void nav(String fxml, String title) {
        NavigationUtil.navigateTo((Stage) searchField.getScene().getWindow(), fxml, title);
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.navigateToFixed((Stage) searchField.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void showAlert(String title, String content) {
        new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK).showAndWait();
    }
}
