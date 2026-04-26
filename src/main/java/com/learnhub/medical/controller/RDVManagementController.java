package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RDVManagementController {

    @FXML
    private TableView<RDV> tableRDV;
    @FXML
    private TableColumn<RDV, LocalDate> colDate;
    @FXML
    private TableColumn<RDV, String> colEtudiant;
    @FXML
    private TableColumn<RDV, String> colMotif;
    @FXML
    private TableColumn<RDV, String> colStatut;
    @FXML
    private TableColumn<RDV, String> colCreneau;
    @FXML
    private TableColumn<RDV, RDV> colActions;

    @FXML
    private Label lblResultCount;
    @FXML
    private TextField txtSearch;

    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final com.learnhub.medical.util.PenaltyService penaltyService = new com.learnhub.medical.util.PenaltyService();
    private final ObservableList<RDV> masterData = FXCollections.observableArrayList();
    private List<Creneau> allCreneaux;

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        // Search logic
        FilteredList<RDV> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(rdv -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (rdv.getMotif().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (rdv.getStudentName() != null && rdv.getStudentName().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (rdv.getStatut().toLowerCase().contains(lowerCaseFilter))
                    return true;
                return false;
            });
            updateResultCount(filteredData.size());
        });
        tableRDV.setItems(filteredData);
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Custom cell for Status Badge
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().clear();
                    String status = item.toLowerCase();

                    if (status.contains("confirm") || status.contains("accept")) {
                        badge.setText("✅ CONFIRMÉ");
                        badge.getStyleClass().add("badge-confirmed");
                    } else if (status.contains("attente")) {
                        badge.setText("⏳ EN ATTENTE");
                        badge.getStyleClass().add("badge-pending");
                    } else if (status.contains("termin")) {
                        badge.setText("🏥 TERMINÉ");
                        badge.getStyleClass().add("badge-finished");
                    } else if (status.contains("annul")) {
                        badge.setText("❌ ANNULÉ");
                        badge.getStyleClass().add("badge-cancelled");
                    } else {
                        badge.setText(item.toUpperCase());
                        badge.setStyle(
                                "-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-padding: 4 12; -fx-background-radius: 8; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Custom cell for Creneau
        colCreneau.setCellValueFactory(cell -> {
            int id = cell.getValue().getCreneauId();
            if (allCreneaux != null) {
                for (Creneau c : allCreneaux) {
                    if (c.getId() == id)
                        return new SimpleStringProperty(c.getJour() + " " + c.getHeure());
                }
            }
            return new SimpleStringProperty("Créneau #" + id);
        });

        // Actions Column with Buttons
        colActions.setCellValueFactory(
                cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁 Voir");
            private final Button btnEdit = new Button("📝 Modifier");
            private final Button btnCancel = new Button("❌ Annuler");
            private final HBox pane = new HBox(12, btnView, btnEdit, btnCancel);

            {
                btnView.getStyleClass().addAll("button", "btn-action-view");
                btnEdit.getStyleClass().addAll("button", "btn-action-edit");
                btnCancel.getStyleClass().addAll("button", "btn-action-delete"); // Red style
                pane.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(RDV rdvItem, boolean empty) {
                super.updateItem(rdvItem, empty);
                if (empty || rdvItem == null) {
                    setGraphic(null);
                } else {
                    btnView.setOnAction(event -> {
                        System.out.println("[DEBUG] Clic sur VOIR pour RDV ID: " + rdvItem.getId());
                        handleView(rdvItem);
                    });
                    btnEdit.setOnAction(event -> {
                        System.out.println("[DEBUG] Clic sur MODIFIER pour RDV ID: " + rdvItem.getId());
                        handleEdit(rdvItem);
                    });
                    btnCancel.setOnAction(event -> {
                        handleCancel(rdvItem);
                    });
                    setGraphic(pane);
                }
            }
        });
    }

    @FXML
    public void handleRefresh() {
        loadData();
    }

    private void loadData() {
        try {
            allCreneaux = creneauRepo.findAll();
            List<RDV> rdvs = rdvRepo.findAllWithStudentNames();
            masterData.setAll(rdvs);
            updateResultCount(rdvs.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlertSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void updateResultCount(int count) {
        lblResultCount.setText(count + " résultat(s)");
    }

    private void handleCancel(RDV rdv) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment annuler ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    String msg = penaltyService.processCancellation(rdv.getId());
                    showAlertSuccess(msg);
                    loadData();
                } catch (Exception e) {
                    showAlertError(e.getMessage());
                }
            }
        });
    }

    private void showAlertError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleAdd() {
        showDialog(null);
    }

    private void handleView(RDV rdv) {
        System.out.println("[DEBUG] Tentative d'ouverture de RDVDetails.fxml...");
        try {
            java.net.URL fxmlUrl = getClass().getResource("/com/learnhub/medical/view/RDVDetails.fxml");
            if (fxmlUrl == null) {
                System.err.println("[ERROR] Fichier RDVDetails.fxml INTROUVABLE dans les ressources !");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            RDVDetailsController controller = loader.getController();
            controller.setRDV(rdv);

            Stage stage = new Stage();
            stage.setTitle("Détails du Rendez-vous");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            System.out.println("[DEBUG] Fenêtre Détails prête, affichage...");
            stage.showAndWait();

            if (controller.isEditRequested())
                handleEdit(rdv);
            else
                loadData();
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur lors de l'ouverture des détails : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEdit(RDV rdv) {
        showDialog(rdv);
    }

    private void showDialog(RDV rdv) {
        System.out.println("[DEBUG] Tentative d'ouverture de DoctorRDVDialog.fxml...");
        try {
            java.net.URL fxmlUrl = getClass().getResource("/com/learnhub/medical/view/DoctorRDVDialog.fxml");
            if (fxmlUrl == null) {
                System.err.println("[ERROR] Fichier DoctorRDVDialog.fxml INTROUVABLE !");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            DoctorRDVDialogController controller = loader.getController();
            controller.setRDV(rdv);

            Stage stage = new Stage();
            stage.setTitle(rdv == null ? "Nouveau RDV" : "Modifier RDV");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            System.out.println("[DEBUG] Fenêtre Dialogue prête, affichage...");
            stage.showAndWait();

            if (controller.isSaved())
                loadData();
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur lors de l'ouverture du dialogue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
