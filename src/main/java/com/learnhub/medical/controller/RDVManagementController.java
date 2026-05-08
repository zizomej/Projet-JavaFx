package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.util.NavigationUtil;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class RDVManagementController {

    @FXML private TableView<RDV> tableRDV;
    @FXML private TableColumn<RDV, String> colDate;
    @FXML private TableColumn<RDV, String> colEtudiant;
    @FXML private TableColumn<RDV, String> colMotif;
    @FXML private TableColumn<RDV, String> colStatut;
    @FXML private TableColumn<RDV, String> colCreneau;
    @FXML private TableColumn<RDV, RDV> colActions;

    @FXML private Label lblTotal;
    @FXML private Label lblAujourdhui;
    @FXML private Label lblEnAttente;
    @FXML private Label lblSummary;
    @FXML private TextField txtSearch;

    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final ObservableList<RDV> masterData = FXCollections.observableArrayList();
    private List<Creneau> allCreneaux;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        colCreneau.setCellValueFactory(cell -> {
            int id = cell.getValue().getCreneauId();
            if (allCreneaux != null) {
                for (Creneau c : allCreneaux) {
                    if (c.getId() == id) return new SimpleStringProperty(c.getJour() + " " + c.getHeure());
                }
            }
            return new SimpleStringProperty("ID: " + id);
        });

        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    setText(item.toUpperCase());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: " + 
                        (item.toLowerCase().contains("confirm") ? "#059669" : 
                         item.toLowerCase().contains("attente") ? "#d97706" : 
                         item.toLowerCase().contains("annul") ? "#dc2626" : "#475569"));
                }
            }
        });

        colActions.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = createIconButton("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z", "#3b82f6");
            private final Button btnEdit = createIconButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "#f59e0b");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(8, btnView, btnEdit);
            { pane.setAlignment(javafx.geometry.Pos.CENTER); }

            @Override
            protected void updateItem(RDV rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) setGraphic(null);
                else {
                    btnView.setOnAction(e -> handleViewDetails(rdv));
                    btnEdit.setOnAction(e -> showDialog(rdv));
                    setGraphic(pane);
                }
            }
        });
    }

    private Button createIconButton(String svgPath, String color) {
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent(svgPath);
        icon.setFill(javafx.scene.paint.Color.web(color));
        icon.setScaleX(0.8); icon.setScaleY(0.8);
        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
        return btn;
    }

    private void loadData() {
        try {
            allCreneaux = creneauRepo.findAll();
            List<RDV> list = rdvRepo.findAllWithStudentNames();
            masterData.setAll(list);
            updateStats(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateStats(List<RDV> list) {
        long total = list.size();
        long today = list.stream().filter(r -> r.getDateDemande().equals(java.time.LocalDate.now())).count();
        long pending = list.stream().filter(r -> r.getStatut().toLowerCase().contains("attente")).count();

        lblTotal.setText(String.valueOf(total));
        lblAujourdhui.setText(String.valueOf(today));
        lblEnAttente.setText(String.valueOf(pending));
        lblSummary.setText("Docteur | " + total + " rendez-vous enregistrés");
    }

    private void setupSearch() {
        FilteredList<RDV> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(rdv -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return (rdv.getStudentName() != null && rdv.getStudentName().toLowerCase().contains(lower)) ||
                       rdv.getMotif().toLowerCase().contains(lower);
            });
        });
        tableRDV.setItems(filteredData);
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML private void handleExportPDF() {
        new Thread(() -> {
            try {
                String fileName = "Mes_RDV_" + System.currentTimeMillis() + ".pdf";
                String path = System.getProperty("user.home") + java.io.File.separator + fileName;
                com.learnhub.medical.util.PdfService.generateRDVReport(masterData, path);
                if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(new java.io.File(path));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleAdd() { showDialog(null); }

    private void showDialog(RDV rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/DoctorRDVDialog.fxml"));
            Parent root = loader.load();
            DoctorRDVDialogController controller = loader.getController();
            controller.setRDV(rdv);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (controller.isSaved()) loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleViewDetails(RDV rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/RDVDetails.fxml"));
            Parent root = loader.load();
            RDVDetailsController controller = loader.getController();
            controller.setRDV(rdv);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) tableRDV.getScene().getWindow();
        NavigationUtil.redirectByRole(stage);
    }
}
