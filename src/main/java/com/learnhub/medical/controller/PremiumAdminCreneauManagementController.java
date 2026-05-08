package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

public class PremiumAdminCreneauManagementController implements ICreneauRefreshing {

    @FXML private TableView<Creneau> tableCreneaux;
    @FXML private TableColumn<Creneau, String> colJour;
    @FXML private TableColumn<Creneau, String> colHeure;
    @FXML private TableColumn<Creneau, String> colRecurrence;
    @FXML private TableColumn<Creneau, String> colStatut;
    @FXML private TableColumn<Creneau, Creneau> colActions;

    @FXML private Label lblTotal;
    @FXML private Label lblDisponibles;
    @FXML private Label lblReserves;
    @FXML private Label lblSummary;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboStatut;

    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final ObservableList<Creneau> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        comboStatut.setItems(FXCollections.observableArrayList("Tous", "Disponible", "Réservé"));
        comboStatut.setValue("Tous");

        FilteredList<Creneau> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, val) -> applyFilters(filteredData));
        comboStatut.valueProperty().addListener((obs, old, val) -> applyFilters(filteredData));

        tableCreneaux.setItems(filteredData);
    }

    private void applyFilters(FilteredList<Creneau> filteredData) {
        filteredData.setPredicate(c -> {
            boolean matchesSearch = true;
            if (txtSearch.getText() != null && !txtSearch.getText().isEmpty()) {
                String search = txtSearch.getText().toLowerCase();
                matchesSearch = c.getJour().toLowerCase().contains(search) || 
                                c.getHeure().toString().contains(search);
            }
            boolean matchesStatus = true;
            if (!comboStatut.getValue().equals("Tous")) {
                String status = c.isDisponibilite() ? "Disponible" : "Réservé";
                matchesStatus = status.equalsIgnoreCase(comboStatut.getValue());
            }
            return matchesSearch && matchesStatus;
        });
    }

    private void setupTable() {
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colRecurrence.setCellValueFactory(new PropertyValueFactory<>("recurrence"));
        
        colStatut.setCellValueFactory(cell -> {
            boolean isDispo = cell.getValue().isDisponibilite();
            return new SimpleStringProperty(isDispo ? "Disponible" : "Réservé");
        });

        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    setText(item.toUpperCase());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: " + 
                        (item.equalsIgnoreCase("disponible") ? "#10b981" : "#ef4444"));
                }
            }
        });

        colActions.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = createIconButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "#f59e0b");
            private final Button btnDel = createIconButton("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z", "#ef4444");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(10, btnEdit, btnDel);
            { 
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                btnEdit.setOnAction(e -> handleEdit(getTableRow().getItem()));
                btnDel.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Creneau item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else setGraphic(pane);
            }
        });
    }

    private Button createIconButton(String svgPath, String color) {
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent(svgPath);
        icon.setFill(javafx.scene.paint.Color.web(color));
        icon.setScaleX(0.7); icon.setScaleY(0.7);
        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
        return btn;
    }

    private void loadData() {
        try {
            List<Creneau> list = creneauRepo.findAll();
            masterData.setAll(list);
            updateStats(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void refreshPlanning() { loadData(); }

    private void updateStats(List<Creneau> list) {
        long total = list.size();
        long dispo = list.stream().filter(c -> c.isDisponibilite()).count();
        long reserve = total - dispo;

        lblTotal.setText(String.valueOf(total));
        lblDisponibles.setText(String.valueOf(dispo));
        lblReserves.setText(String.valueOf(reserve));
        lblSummary.setText("Administrateur | " + total + " créneaux gérés au total");
    }

    @FXML private void handleAdd() { openDialog(null); }
    private void handleEdit(Creneau c) { openDialog(c); }

    private void openDialog(Creneau c) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/AddCreneauDialog.fxml"));
            javafx.scene.Parent root = loader.load();
            AddCreneauController ctrl = loader.getController();
            ctrl.setParentController(this);
            if (c != null) ctrl.setEditData(c);
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            loadData();
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
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Planning généré et ouvert : " + fileName);
                    a.show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Erreur PDF : " + e.getMessage());
                    a.show();
                });
            }
        }).start();
    }

    private void handleDelete(Creneau c) {
        if (c == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce créneau ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    creneauRepo.delete(c.getId());
                    loadData();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML private void handleRefresh() { loadData(); }
}
