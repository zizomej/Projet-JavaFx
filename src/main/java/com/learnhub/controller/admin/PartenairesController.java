package com.learnhub.controller.admin;

import com.learnhub.dao.UniversiteDAO;
import com.learnhub.models.Universite;
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

    @FXML private TableView<Universite> partenaireTable;
    @FXML private Label publiqueLabel;
    @FXML private Label priveLabel;
    @FXML private TextField searchField;

    private final UniversiteDAO universiteDAO = new UniversiteDAO();
    private ObservableList<Universite> universiteList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        loadStats();

        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void setupTable() {
        partenaireTable.getColumns().clear();

        TableColumn<Universite, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Universite, String> colNom = new TableColumn<>("Nom de l'université");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(220);
        colNom.setStyle("-fx-alignment: CENTER;");

        TableColumn<Universite, String> colSecteur = new TableColumn<>("Type");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSecteur.setPrefWidth(120);
        colSecteur.setStyle("-fx-alignment: CENTER;");

        TableColumn<Universite, String> colVille = new TableColumn<>("Ville");
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colVille.setPrefWidth(100);
        colVille.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Universite, String> colTelephone = new TableColumn<>("Téléphone");
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colTelephone.setPrefWidth(150);
        colTelephone.setStyle("-fx-alignment: CENTER;");

        TableColumn<Universite, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(220);
        colEmail.setStyle("-fx-alignment: CENTER;");

        TableColumn<Universite, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setStyle("-fx-alignment: CENTER;");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    Universite u = getTableView().getItems().get(getIndex());
                    showFormDialog(u);
                });
                deleteBtn.setOnAction(e -> {
                    Universite u = getTableView().getItems().get(getIndex());
                    handleDelete(u);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        partenaireTable.getColumns().addAll(colNom, colSecteur, colVille, colTelephone, colEmail, colActions);
        partenaireTable.setItems(universiteList);
    }

    private void loadData() {
        try {
            List<Universite> universites = universiteDAO.findAll();
            universiteList.setAll(universites);
            partenaireTable.setItems(universiteList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStats() {
        long publiqueCount = universiteList.stream()
                .filter(u -> "Publique".equalsIgnoreCase(u.getType()))
                .count();
        long priveCount = universiteList.stream()
                .filter(u -> "Privée".equalsIgnoreCase(u.getType()))
                .count();
                
        publiqueLabel.setText(String.valueOf(publiqueCount));
        priveLabel.setText(String.valueOf(priveCount));
    }

    private void filterData() {
        String search = searchField.getText().toLowerCase();
        ObservableList<Universite> filtered = FXCollections.observableArrayList();

        for (Universite u : universiteList) {
            String nom = u.getNom() != null ? u.getNom().toLowerCase() : "";
            String ville = u.getVille() != null ? u.getVille().toLowerCase() : "";
            String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";

            if (nom.contains(search) || ville.contains(search) || email.contains(search)) {
                filtered.add(u);
            }
        }
        partenaireTable.setItems(filtered);
    }

    @FXML
    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleDelete(Universite universite) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'université");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'université \"" + universite.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    universiteDAO.delete(universite.getId());
                    loadData();
                    loadStats();
                    showAlert("Succès", "Université supprimée avec succès!");
                } catch (SQLException e) {
                    if (e.getMessage().toLowerCase().contains("foreign key") || e.getMessage().contains("constraint fails")) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur de suppression");
                        errorAlert.setHeaderText("Suppression impossible");
                        errorAlert.setContentText("Cet établissement ne peut pas être supprimé car d'autres éléments (comme des offres de stage ou demandes) y sont liés.");
                        errorAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Impossible de supprimer: " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            }
        });
    }

    private void showFormDialog(Universite universite) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/admin/universite_form.fxml"));
            javafx.scene.Parent root = loader.load();

            UniversiteFormController controller = loader.getController();
            controller.setUniversite(universite);
            
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
