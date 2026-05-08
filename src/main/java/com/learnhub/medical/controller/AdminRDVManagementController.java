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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdminRDVManagementController {

    @FXML private TableView<RDV> tableRDV;
    @FXML private TableColumn<RDV, LocalDate> colDate;
    @FXML private TableColumn<RDV, String> colEtudiant;
    @FXML private TableColumn<RDV, String> colMotif;
    @FXML private TableColumn<RDV, String> colStatut;
    @FXML private TableColumn<RDV, String> colCreneau;
    @FXML private TableColumn<RDV, RDV> colActions;

    @FXML private Label lblTotal;
    @FXML private Label lblEnAttente;
    @FXML private Label lblConfirmes;
    @FXML private Label lblAnnules;
    @FXML private Label lblSummary;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboStatut;

    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final com.learnhub.medical.util.PenaltyService penaltyService = new com.learnhub.medical.util.PenaltyService();
    private final ObservableList<RDV> masterData = FXCollections.observableArrayList();
    private List<Creneau> allCreneaux;

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        comboStatut.setItems(FXCollections.observableArrayList("Tous", "En Attente", "Confirmé", "Annulé", "Payé"));
        comboStatut.setValue("Tous");

        FilteredList<RDV> filteredData = new FilteredList<>(masterData, p -> true);
        
        txtSearch.textProperty().addListener((obs, old, val) -> applyFilters(filteredData));
        comboStatut.valueProperty().addListener((obs, old, val) -> applyFilters(filteredData));

        tableRDV.setItems(filteredData);
    }

    private void applyFilters(FilteredList<RDV> filteredData) {
        filteredData.setPredicate(rdv -> {
            boolean matchesSearch = true;
            if (txtSearch.getText() != null && !txtSearch.getText().isEmpty()) {
                String search = txtSearch.getText().toLowerCase();
                matchesSearch = rdv.getMotif().toLowerCase().contains(search) ||
                                (rdv.getStudentName() != null && rdv.getStudentName().toLowerCase().contains(search));
            }

            boolean matchesStatus = true;
            if (!comboStatut.getValue().equals("Tous")) {
                matchesStatus = rdv.getStatut().toLowerCase().contains(comboStatut.getValue().toLowerCase());
            }

            return matchesSearch && matchesStatus;
        });
    }

    private void setupTable() {
        colDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateDemande"));
        colEtudiant.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("studentName"));
        colMotif.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("motif"));
        colStatut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("statut"));
        
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
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
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
            private final Button btnCancel = createIconButton("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z", "#ef4444");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(8, btnView, btnEdit, btnCancel);
            { pane.setAlignment(javafx.geometry.Pos.CENTER); }

            @Override
            protected void updateItem(RDV rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) setGraphic(null);
                else {
                    btnView.setOnAction(e -> handleView(rdv));
                    btnEdit.setOnAction(e -> handleEdit(rdv));
                    btnCancel.setOnAction(e -> handleCancel(rdv));
                    setGraphic(pane);
                }
            }
        });
    }

    private Button createIconButton(String svgPath, String color) {
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent(svgPath);
        icon.setFill(javafx.scene.paint.Color.web(color));
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);

        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setCursor(javafx.scene.Cursor.HAND);
        String baseStyle = "-fx-background-color: transparent; -fx-padding: 5; -fx-background-radius: 5;";
        btn.setStyle(baseStyle);
        
        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle + "-fx-background-color: rgba(0,0,0,0.05);"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        return btn;
    }

    private void loadData() {
        try {
            allCreneaux = creneauRepo.findAll();
            masterData.setAll(rdvRepo.findAllWithStudentNames());
            updateStats(masterData);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateStats(List<RDV> rdvs) {
        long total = rdvs.size();
        long enAttente = rdvs.stream().filter(r -> r.getStatut().toLowerCase().contains("attente")).count();
        long confirmes = rdvs.stream().filter(r -> r.getStatut().toLowerCase().contains("confirm") || r.getStatut().toLowerCase().contains("pay")).count();
        long annules = rdvs.stream().filter(r -> r.getStatut().toLowerCase().contains("annul")).count();

        lblTotal.setText(String.valueOf(total));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblConfirmes.setText(String.valueOf(confirmes));
        lblAnnules.setText(String.valueOf(annules));
        lblSummary.setText("Administrateur | " + total + " rendez-vous au total");
    }

    @FXML private void handleAdd() { showDialog(null); }
    private void handleEdit(RDV rdv) { showDialog(rdv); }
    
    private void handleView(RDV rdv) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/RDVDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            RDVDetailsController controller = loader.getController();
            controller.setRDV(rdv);
            Stage stage = new Stage();
            stage.setTitle("Détails");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleCancel(RDV rdv) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Annuler ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    penaltyService.processCancellation(rdv.getId());
                    loadData();
                } catch (Exception e) { showAlertError(e.getMessage()); }
            }
        });
    }

    private void showDialog(RDV rdv) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/DoctorRDVDialog.fxml"));
            javafx.scene.Parent root = loader.load();
            DoctorRDVDialogController controller = loader.getController();
            controller.setRDV(rdv);
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (controller.isSaved()) loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlertError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.showAndWait();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) tableRDV.getScene().getWindow();
        NavigationUtil.redirectByRole(stage);
    }
    
    @FXML
    private void handleRefresh() { loadData(); }

    @FXML
    private void handleExportPDF() {
        new Thread(() -> {
            try {
                String fileName = "Rapport_RDV_Medical_" + System.currentTimeMillis() + ".pdf";
                String path = System.getProperty("user.home") + java.io.File.separator + fileName;
                java.io.File file = new java.io.File(path);
                com.learnhub.medical.util.PdfService.generateRDVReport(masterData, path);
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Rapport des rendez-vous généré et ouvert !");
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
}
