package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AdminCreneauManagementController implements ICreneauRefreshing {

    @FXML private PieChart pieChartStatus;
    @FXML private Label lblTotalSlots;
    @FXML private Label lblResultCount;
    @FXML private TextField txtSearch;
    @FXML private TableView<Creneau> tableCreneaux;
    @FXML private TableColumn<Creneau, String> colJour;
    @FXML private TableColumn<Creneau, String> colHeure;
    @FXML private TableColumn<Creneau, String> colRecurrence;
    @FXML private TableColumn<Creneau, Boolean> colStatut;
    @FXML private TableColumn<Creneau, Creneau> colActions;

    private final CreneauRepository repo = new CreneauRepository();
    private final ObservableList<Creneau> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        refreshPlanning();
    }

    private void setupTable() {
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colRecurrence.setCellValueFactory(new PropertyValueFactory<>("recurrence"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
        
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) { setGraphic(null); }
                else {
                    Label badge = new Label(available ? "Disponible" : "Réservé");
                    if (available) {
                        badge.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #166534; " +
                                     "-fx-padding: 4 15; -fx-background-radius: 12; " +
                                     "-fx-border-color: #DCFCE7; -fx-border-radius: 12; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #991B1B; " +
                                     "-fx-padding: 4 15; -fx-background-radius: 12; " +
                                     "-fx-border-color: #FEE2E2; -fx-border-radius: 12; -fx-font-weight: bold;");
                    }
                    setGraphic(badge); setAlignment(Pos.CENTER);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDel = new Button("🗑");
            private final HBox pane = new HBox(8, btnEdit, btnDel);
            { 
                pane.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-action-view"); 
                btnDel.getStyleClass().add("btn-action-edit");
            }
            @Override
            protected void updateItem(Creneau item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); }
                else {
                    Creneau c = getTableRow().getItem();
                    btnEdit.setOnAction(e -> showEditDialog(c));
                    btnDel.setOnAction(e -> handleDelete(c));
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Creneau> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(c -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return c.getJour().toLowerCase().contains(lower) ||
                       c.getHeure().toString().contains(lower) ||
                       c.getRecurrence().toLowerCase().contains(lower);
            });
            lblResultCount.setText(filteredData.size() + " résultat(s)");
            updateChart(filteredData);
        });
        tableCreneaux.setItems(filteredData);
    }

    @Override
    public void refreshPlanning() {
        try {
            List<Creneau> list = repo.findAll();
            masterData.setAll(list);
            lblResultCount.setText(list.size() + " résultat(s)");
            updateChart(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateChart(List<Creneau> list) {
        long available = list.stream().filter(Creneau::isDisponibilite).count();
        long reserved = list.size() - available;

        PieChart.Data d1 = new PieChart.Data("Libres", available);
        PieChart.Data d2 = new PieChart.Data("Réservés", reserved);
        pieChartStatus.setData(FXCollections.observableArrayList(d1, d2));
        lblTotalSlots.setText(String.valueOf(list.size()));
        
        // Custom colors matching the dashboard aesthetic
        d1.getNode().setStyle("-fx-pie-color: #10B981;");
        d2.getNode().setStyle("-fx-pie-color: #EF4444;");
    }

    @FXML private void showAddDialog() { openDialog(null); }

    private void showEditDialog(Creneau c) { openDialog(c); }

    private void openDialog(Creneau c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/AddCreneauDialog.fxml"));
            Parent root = loader.load();
            AddCreneauController ctrl = loader.getController();
            ctrl.setParentController(this);
            if (c != null) ctrl.setEditData(c);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshPlanning();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleExportPDF() {
        new Thread(() -> {
            try {
                String fileName = "Planning_Medical_" + System.currentTimeMillis() + ".pdf";
                String path = System.getProperty("user.home") + java.io.File.separator + fileName;
                java.io.File file = new java.io.File(path);

                com.learnhub.medical.util.PdfService.generatePlanning(masterData, path);
                
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Réussi");
                    alert.setHeaderText("Planning généré !");
                    alert.setContentText("Le document a été ouvert et enregistré dans votre dossier utilisateur sous :\n" + fileName);
                    alert.show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Erreur lors de la génération : " + e.getMessage());
                    alert.show();
                });
            }
        }).start();
    }

    private void handleDelete(Creneau c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce créneau ?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { repo.delete(c.getId()); refreshPlanning(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
