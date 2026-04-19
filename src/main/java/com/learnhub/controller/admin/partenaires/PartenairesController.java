package com.learnhub.controller.admin.partenaires;

import com.learnhub.controller.admin.DeleteConfirmationController;
import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PartenairesController {

    @FXML private Label totalPartenairesLabel;
    @FXML private Label actifsLabel;
    @FXML private Label inactifsLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label tableTitleLabel;
    @FXML private TableView<Partenaire> partenaireTable;
    @FXML private TextField searchField;
    @FXML private TextField navbarSearchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterSecteur;
    @FXML private VBox secteurChartContainer;
    @FXML private VBox paysChartContainer;

    private final PartenaireDAO dao = new PartenaireDAO();
    private final OffreStageDAO offreDao = new OffreStageDAO();
    private final DemandeStageDAO demandeDao = new DemandeStageDAO();
    private final ObservableList<Partenaire> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (filterStatut != null) filterStatut.getItems().addAll("", "actif", "inactif", "en_attente");
        if (filterSecteur != null) filterSecteur.getItems().addAll("", "Technologie", "Finance", "Commerce", "Sante", "Education", "General");
        setupColumns();
        partenaireTable.setItems(data);
        if (navbarSearchField != null && searchField != null) {
            navbarSearchField.textProperty().bindBidirectional(searchField.textProperty());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        if (filterStatut != null) {
            filterStatut.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        if (filterSecteur != null) {
            filterSecteur.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        }
        loadData();
    }

    private void setupColumns() {
        partenaireTable.getColumns().clear();

        TableColumn<Partenaire, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(55);

        TableColumn<Partenaire, String> colNom = new TableColumn<>("Partenaire");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(190);

        TableColumn<Partenaire, String> colPays = new TableColumn<>("Pays");
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
        colPays.setPrefWidth(120);
        colPays.setCellFactory(col -> createMutedCell("Non specifie"));

        TableColumn<Partenaire, String> colSecteur = new TableColumn<>("Secteur");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteur"));
        colSecteur.setPrefWidth(140);
        colSecteur.setCellFactory(col -> createMutedCell("General"));

        TableColumn<Partenaire, String> colContact = new TableColumn<>("Contact");
        colContact.setCellValueFactory(cell -> {
            Partenaire p = cell.getValue();
            String email = safe(p.getEmail());
            String phone = safe(p.getTelephone());
            return new ReadOnlyStringWrapper(email + (phone.isBlank() ? "" : "\n" + phone));
        });
        colContact.setPrefWidth(210);

        TableColumn<Partenaire, String> colWebsite = new TableColumn<>("Website");
        colWebsite.setCellValueFactory(new PropertyValueFactory<>("website"));
        colWebsite.setPrefWidth(180);
        colWebsite.setCellFactory(col -> createMutedCell("-"));

        TableColumn<Partenaire, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(110);
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if ("actif".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill:#059669;-fx-font-weight:700;");
                } else if ("inactif".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill:#dc2626;-fx-font-weight:700;");
                } else {
                    setStyle("-fx-text-fill:#b45309;-fx-font-weight:700;");
                }
            }
        });

        TableColumn<Partenaire, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✏ Modif");
            private final Button deleteButton = new Button("🗑 Suppr");

            {
                editButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4;");
                deleteButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4;");
                editButton.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteItem(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox box = new HBox(8, editButton, deleteButton);
                setGraphic(box);
            }
        });

        partenaireTable.getColumns().addAll(colId, colNom, colPays, colSecteur, colStatut, colActions);
    }

    private TableCell<Partenaire, String> createMutedCell(String fallback) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                    return;
                }
                String value = item == null || item.isBlank() ? fallback : item;
                setText(value);
                setStyle(item == null || item.isBlank()
                        ? "-fx-text-fill:#94a3b8;"
                        : "-fx-text-fill:#334155;");
            }
        };
    }

    private void loadData() {
        try {
            List<Partenaire> list = dao.findAll();
            data.setAll(list);
            int total = dao.count();
            totalPartenairesLabel.setText(String.valueOf(total));
            actifsLabel.setText(String.valueOf(dao.countByStatut("actif")));
            inactifsLabel.setText(String.valueOf(dao.countByStatut("inactif")));
            if (enAttenteLabel != null) enAttenteLabel.setText(String.valueOf(dao.countByStatut("en_attente")));
            if (tableTitleLabel != null) tableTitleLabel.setText("Liste des Partenaires (" + total + ")");
            buildCharts(list);
        } catch (SQLException e) {
            showError("Impossible de charger les partenaires : " + e.getMessage());
        }
    }

    private void buildCharts(List<Partenaire> list) {
        if (secteurChartContainer == null || paysChartContainer == null) return;

        Map<String, Long> bySecteur = list.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getSecteur() != null && !p.getSecteur().isBlank() ? p.getSecteur() : "Non spécifié",
                        java.util.stream.Collectors.counting()
                ));
        secteurChartContainer.getChildren().clear();
        int totalSecteur = list.size();
        bySecteur.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> secteurChartContainer.getChildren().add(
                        buildBarRow(e.getKey(), e.getValue(), totalSecteur, "#3b82f6")));

        Map<String, Long> byPays = list.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getPays() != null && !p.getPays().isBlank() ? p.getPays() : "Non spécifié",
                        java.util.stream.Collectors.counting()
                ));
        paysChartContainer.getChildren().clear();
        int totalPays = list.size();
        byPays.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> paysChartContainer.getChildren().add(
                        buildBarRow(e.getKey(), e.getValue(), totalPays, "#10b981")));
    }

    private javafx.scene.Node buildBarRow(String label, long count, int total, String color) {
        double pct = total == 0 ? 0 : (count * 100.0 / total);

        Label lblName = new Label(label);
        lblName.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        lblName.setPrefWidth(130);
        lblName.setMinWidth(130);

        javafx.scene.layout.Region bar = new javafx.scene.layout.Region();
        bar.setPrefHeight(18);
        bar.setPrefWidth(Math.max(4, pct * 2.2));
        bar.setMaxWidth(220);
        bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        Label lblPct = new Label(String.format("%.0f%%  (%d)", pct, count));
        lblPct.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        HBox row = new HBox(10, lblName, bar, lblPct);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        String secteur = (filterSecteur != null) ? filterSecteur.getValue() : null;
        String statut = (filterStatut != null) ? filterStatut.getValue() : null;
        try {
            data.setAll(dao.search(query, secteur, statut));
        } catch (SQLException e) {
            showError("Recherche impossible : " + e.getMessage());
        }
    }

    @FXML private void handleAdd() { showDialog(new Partenaire(), false); }
    @FXML private void handleRefresh() { loadData(); }

    @FXML
    private void handleEdit() {
        Partenaire selected = partenaireTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez un partenaire.");
            return;
        }
        showDialog(selected, true);
    }

    @FXML
    private void handleDelete() {
        Partenaire selected = partenaireTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectionnez un partenaire.");
            return;
        }
        handleDeleteItem(selected);
    }

    private void handleEdit(Partenaire partenaire) {
        showDialog(partenaire, true);
    }

    private void handleDeleteItem(Partenaire partenaire) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin/delete_confirmation.fxml"));
            javafx.scene.Parent root = loader.load();
            DeleteConfirmationController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Confirmation");
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            root.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5); -fx-background-radius: 12; -fx-background-color: transparent;");
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            java.util.Map<String, String> details = new java.util.LinkedHashMap<>();
            details.put("Entreprise", safe(partenaire.getNom()));
            details.put("Secteur", safe(partenaire.getSecteur()));
            details.put("Email", safe(partenaire.getEmail()));
            details.put("Statut", safe(partenaire.getStatut()));

            controller.initData(stage, "partenaire", "Êtes-vous sûr de vouloir supprimer l'entreprise \"" + partenaire.getNom() + "\" ?", details);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                try {
                    List<OffreStage> offres = offreDao.findByPartenaire(partenaire.getId());
                    for (OffreStage o : offres) {
                        demandeDao.deleteByOffre(o.getId());
                    }
                    offreDao.deleteByPartenaire(partenaire.getId());
                } catch (Exception cascadeEx) {
                    cascadeEx.printStackTrace();
                }
                dao.delete(partenaire.getId());
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void showDialog(Partenaire partenaire, boolean edit) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin/partenaires/partenaire_form.fxml"));
            javafx.scene.Parent root = loader.load();
            PartenaireFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(edit ? "Modifier un partenaire" : "Nouveau partenaire");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            controller.initData(partenaire, edit, stage, this::loadData);

            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire : " + e.getMessage());
        }
    }

    private TextField createTextField(String value, String prompt) {
        TextField field = new TextField(safe(value));
        field.setPromptText(prompt);
        field.setPrefWidth(340);
        return field;
    }

    private TextArea createTextArea(String value, String prompt) {
        TextArea area = new TextArea(safe(value));
        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefRowCount(3);
        area.setPrefWidth(340);
        return area;
    }

    private String valueOf(ComboBox<String> comboBox) {
        String value = comboBox.getEditor().getText();
        if (value == null || value.isBlank()) {
            value = comboBox.getValue();
        }
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    @FXML private void goDashboard() { navigate("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs() { navigate("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules() { navigate("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances() { navigate("/fxml/admin/seances.fxml", "Seances"); }
    @FXML private void goNotes() { navigate("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences() { navigate("/fxml/admin/presences.fxml", "Presences"); }
    @FXML private void goFilieres() { navigate("/fxml/admin/filieres.fxml", "Filieres"); }
    @FXML private void goEvenements() { navigate("/fxml/admin/evenements.fxml", "Evenements"); }
    @FXML private void goRdv() { navigate("/fxml/admin/rdv.fxml", "RDV medicaux"); }
    @FXML private void goCreneaux() { navigate("/fxml/admin/creneaux.fxml", "Creneaux"); }
    @FXML private void goPartenaires() { navigate("/fxml/admin/partenaires/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage() { navigate("/fxml/admin/offrestage/offres_stage.fxml", "Offres de stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de stage"); }
    @FXML private void goMailing() { navigate("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing Partenaires"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) partenaireTable.getScene().getWindow();
        NavigationUtil.navigateToFixed(stage, "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) partenaireTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
