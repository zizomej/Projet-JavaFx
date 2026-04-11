package com.learnhub.controller.admin;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class PartenairesController {

    @FXML private TableView<Partenaire> partenaireTable;
    @FXML private Label totalPartenairesLabel;
    @FXML private TextField searchField;

    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private ObservableList<Partenaire> partenaireList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        loadStats();

        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void setupTable() {
        partenaireTable.getColumns().clear();

        TableColumn<Partenaire, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Partenaire, String> colNom = new TableColumn<>("Nom de l'université");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(200);

        TableColumn<Partenaire, String> colSecteur = new TableColumn<>("Type");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteur"));
        colSecteur.setPrefWidth(120);

        TableColumn<Partenaire, String> colVille = new TableColumn<>("Ville");
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colVille.setPrefWidth(100);
        
        TableColumn<Partenaire, String> colTelephone = new TableColumn<>("Téléphone");
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colTelephone.setPrefWidth(120);

        TableColumn<Partenaire, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        // Optional: Filières pseudo column mappings
        TableColumn<Partenaire, Integer> colFilieres = new TableColumn<>("Filières");
        // For now, mapping to ID just to show a number but this should be fetched from DAO. 
        // We'll leave it simple for visual completeness or omit it. Let's map it to placeholder 0.
        colFilieres.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else setText("0"); // placeholder
            }
        });
        colFilieres.setPrefWidth(80);

        TableColumn<Partenaire, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Partenaire p = getTableView().getItems().get(getIndex());
                    showFormDialog(p);
                });
                deleteBtn.setOnAction(e -> {
                    Partenaire p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        partenaireTable.getColumns().addAll(colNom, colSecteur, colVille, colTelephone, colEmail, colFilieres, colActions);
        partenaireTable.setItems(partenaireList);
    }

    private void loadData() {
        try {
            List<Partenaire> partenaires = partenaireDAO.findAll();
            partenaireList.setAll(partenaires);
            partenaireTable.setItems(partenaireList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try {
            totalPartenairesLabel.setText(partenaireDAO.count() + " établissements");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String search = searchField.getText().toLowerCase();
        ObservableList<Partenaire> filtered = FXCollections.observableArrayList();

        for (Partenaire p : partenaireList) {
            if (p.getNom().toLowerCase().contains(search) ||
                    p.getVille().toLowerCase().contains(search) ||
                    p.getEmail().toLowerCase().contains(search)) {
                filtered.add(p);
            }
        }
        partenaireTable.setItems(filtered);
    }

    @FXML
    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleDelete(Partenaire partenaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'université");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'université \"" + partenaire.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    partenaireDAO.delete(partenaire.getId());
                    loadData();
                    loadStats();
                    showAlert("Succès", "Université supprimée avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    private void showFormDialog(Partenaire partenaire) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin/universite_form.fxml"));
            javafx.scene.Parent root = loader.load();

            UniversiteFormController controller = loader.getController();
            controller.setPartenaire(partenaire);
            
            // Reload data after save
            controller.setOnSaveCallback(() -> {
                loadData();
                loadStats();
            });

            Stage stage = (Stage) partenaireTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() { filterData(); }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
        loadStats();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) partenaireTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/dashboard.fxml", "Dashboard");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
