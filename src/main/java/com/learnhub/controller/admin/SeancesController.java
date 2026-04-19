package com.learnhub.controller.admin;

import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Seance;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class SeancesController {

    @FXML private TableView<Seance> table;
    @FXML private TableColumn<Seance, String> colDate;
    @FXML private TableColumn<Seance, String> colHeure;
    @FXML private TableColumn<Seance, String> colModule;
    @FXML private TableColumn<Seance, String> colSalle;
    @FXML private TableColumn<Seance, String> colType;
    @FXML private TableColumn<Seance, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboModule;
    @FXML private ComboBox<String> comboSalle;
    @FXML private ComboBox<String> comboType;

    @FXML private Label totalSeancesLabel;
    @FXML private Label lblSallesOccupies;
    @FXML private Label lblTauxOccupation;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final com.learnhub.dao.ModuleDAO moduleDAO = new com.learnhub.dao.ModuleDAO();
    private final ObservableList<Seance> seanceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupFilters();
        
        searchField.textProperty().addListener((obs, old, val) -> filterData());
        comboModule.valueProperty().addListener((obs, old, val) -> filterData());
        comboSalle.valueProperty().addListener((obs, old, val) -> filterData());
        comboType.valueProperty().addListener((obs, old, val) -> filterData());
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    String styleClass = switch (item.toUpperCase().trim()) {
                        case "CM", "COURS" -> "badge-info";
                        case "TD" -> "badge-warning";
                        case "TP" -> "badge-success";
                        case "EXAM", "DS", "PROJET" -> "badge-purple";
                        default -> "badge-secondary";
                    };
                    badge.getStyleClass().add(styleClass);
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final Button iaBtn = new Button("🧠");
            private final HBox pane = new HBox(12, iaBtn, editBtn, deleteBtn);
            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                editBtn.getStyleClass().addAll("action-btn-edit");
                deleteBtn.getStyleClass().addAll("action-btn-delete");
                iaBtn.getStyleClass().addAll("action-btn-ia"); // Style à vérifier

                iaBtn.setOnAction(e -> handleIA(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        try {
            // Populate Modules
            ObservableList<String> modules = FXCollections.observableArrayList("📚 Tous les modules");
            moduleDAO.findAll().forEach(m -> modules.add(m.getIntitule()));
            comboModule.setItems(modules);
            comboModule.getSelectionModel().select(0);

            // Populate Salles (unique from current list)
            ObservableList<String> salles = FXCollections.observableArrayList("🏫 Toutes les salles");
            seanceList.stream().map(Seance::getSalle).distinct().forEach(salles::add);
            comboSalle.setItems(salles);
            comboSalle.getSelectionModel().select(0);

            // Populate Types
            comboType.setItems(FXCollections.observableArrayList("📝 Tous les types", "CM", "TD", "TP", "EXAM", "PROJET"));
            comboType.getSelectionModel().select(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            seanceList.setAll(seanceDAO.findAll());
            table.setItems(seanceList);
            
            // Update Stats
            int total = seanceList.size();
            totalSeancesLabel.setText(String.valueOf(total));
            
            long uniqueSalles = seanceList.stream().map(Seance::getSalle).distinct().count();
            lblSallesOccupies.setText(String.valueOf(uniqueSalles));
            
            double taux = (uniqueSalles / 20.0) * 100; // Mock calculation
            lblTauxOccupation.setText(String.format("%.1f%%", Math.min(taux, 100)));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter() {
        filterData();
    }

    private void filterData() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String modFilter = comboModule.getValue() == null || comboModule.getSelectionModel().getSelectedIndex() == 0 ? "" : comboModule.getValue().toLowerCase();
        String salleFilter = comboSalle.getValue() == null || comboSalle.getSelectionModel().getSelectedIndex() == 0 ? "" : comboSalle.getValue().toLowerCase();
        String typeFilter = comboType.getValue() == null || comboType.getSelectionModel().getSelectedIndex() == 0 ? "" : comboType.getValue().toLowerCase();

        FilteredList<Seance> filtered = new FilteredList<>(seanceList, s -> {
            boolean matchesSearch = search.isEmpty() || 
                (s.getModuleTitre() != null && s.getModuleTitre().toLowerCase().contains(search)) ||
                (s.getSalle() != null && s.getSalle().toLowerCase().contains(search));
            
            boolean matchesModule = modFilter.isEmpty() || 
                (s.getModuleTitre() != null && s.getModuleTitre().toLowerCase().equals(modFilter));
            
            boolean matchesSalle = salleFilter.isEmpty() || 
                (s.getSalle() != null && s.getSalle().toLowerCase().equals(salleFilter));
            
            boolean matchesType = typeFilter.isEmpty() || 
                (s.getType() != null && s.getType().toLowerCase().equals(typeFilter));

            return matchesSearch && matchesModule && matchesSalle && matchesType;
        });
        
        table.setItems(filtered);
    }


    @FXML
    private void handleAdd() {
        showForm(null);
    }

    private void handleEdit(Seance seance) {
        showForm(seance);
    }

    private void showForm(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/seance_form.fxml"));
            Parent root = loader.load();
            SeanceFormController controller = loader.getController();
            controller.setSeance(seance);

            Stage stage = (Stage) table.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Seance seance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer la séance ?");
        alert.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                seanceDAO.delete(seance.getId());
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erreur de suppression");
                err.setHeaderText("Impossible de supprimer la séance");
                err.setContentText("Cette séance contient fort probablement une liste de présences attachée. Veuillez supprimer ses dépendances avant.");
                err.showAndWait();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void handleIA(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ia_sessions_modal.fxml"));
            Parent root = loader.load();
            IASessionsController controller = loader.getController();
            controller.setSeance(seance);

            Stage stage = new Stage();
            stage.setTitle("Assistant IA LearnHub");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(table.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goMetiers() {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), "/fxml/admin/metiers_avances.fxml",
                "Outils Avancés");
    }

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), "/fxml/admin/dashboard.fxml", "Tableau de Bord");
    }
}
