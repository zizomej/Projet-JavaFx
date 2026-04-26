package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class MedecinDashboardController {

    @FXML private Label lblPendingCount;
    @FXML private Label lblConfirmedCount;
    @FXML private Label lblTodayCount;
    @FXML private Label lblMonthCount;

    @FXML private TableView<RDV> tableRDV;
    @FXML private TableColumn<RDV, String>  colStudent;
    @FXML private TableColumn<RDV, String>  colMotif;
    @FXML private TableColumn<RDV, LocalDate> colDate;
    @FXML private TableColumn<RDV, String>  colStatut;
    @FXML private TableColumn<RDV, String>  colCreneau;
    @FXML private TableColumn<RDV, Void>    colActions;

    @FXML private TextField    txtSearch;
    @FXML private ComboBox<String> comboSort;

    private final RDVRepository    rdvRepo    = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final com.learnhub.medical.util.PenaltyService penaltyService = new com.learnhub.medical.util.PenaltyService();
    private final ObservableList<RDV> masterList = FXCollections.observableArrayList();
    private FilteredList<RDV> filteredList;
    private SortedList<RDV> sortedList;
    private List<Creneau> allCreneaux;

    @FXML
    public void initialize() {
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colMotif  .setCellValueFactory(new PropertyValueFactory<>("motif"));
        colDate   .setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut .setCellValueFactory(new PropertyValueFactory<>("statut"));

        colCreneau.setCellValueFactory(cell -> {
            int id = cell.getValue().getCreneauId();
            if (allCreneaux != null) {
                for (Creneau c : allCreneaux) {
                    if (c.getId() == id)
                        return new SimpleStringProperty(c.getJour() + " à " + c.getHeure());
                }
            }
            return new SimpleStringProperty("Créneau #" + id);
        });

        comboSort.setItems(FXCollections.observableArrayList(
            "Date (Nouveau)", "Date (Ancien)", "Nom Étudiant", "Motif"));
        comboSort.setOnAction(e -> applySorting());

        filteredList = new FilteredList<>(masterList, p -> true);
        txtSearch.textProperty().addListener((obs, o, newVal) ->
            filteredList.setPredicate(rdv -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String low = newVal.toLowerCase();
                return rdv.getMotif().toLowerCase().contains(low)
                    || (rdv.getStudentName() != null &&
                        rdv.getStudentName().toLowerCase().contains(low));
            })
        );

        sortedList = new SortedList<>(filteredList);
        // Link the sorted list to the table view
        tableRDV.setItems(sortedList);
        // Allow the table columns to still sort the list
        sortedList.comparatorProperty().bind(tableRDV.comparatorProperty());

        addActionsColumn();
        refreshData();
    }

    @FXML
    public void refreshData() {
        try {
            allCreneaux = creneauRepo.findAll();
            java.util.Map<String, Integer> stats = rdvRepo.getStats();
            lblPendingCount  .setText(String.valueOf(stats.getOrDefault("pending",   0)));
            lblConfirmedCount.setText(String.valueOf(stats.getOrDefault("confirmed", 0)));
            lblTodayCount    .setText(String.valueOf(stats.getOrDefault("today",     0)));
            lblMonthCount    .setText(String.valueOf(stats.getOrDefault("month",     0)));
            masterList.setAll(rdvRepo.findAllWithStudentNames());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void applySorting() {
        String s = comboSort.getValue();
        if (s == null) return;
        Comparator<RDV> cmp = switch (s) {
            case "Date (Nouveau)"  -> Comparator.comparing(RDV::getDateDemande,
                                       Comparator.nullsLast(Comparator.reverseOrder()));
            case "Date (Ancien)"   -> Comparator.comparing(RDV::getDateDemande,
                                       Comparator.nullsLast(Comparator.naturalOrder()));
            case "Nom Étudiant"    -> Comparator.comparing(
                                       r -> r.getStudentName() != null ? r.getStudentName().toLowerCase() : "");
            case "Motif"           -> Comparator.comparing(r -> r.getMotif().toLowerCase());
            default -> null;
        };
        if (cmp != null) {
            // Unbind to allow the SortedList to follow the masterList order
            sortedList.comparatorProperty().unbind();
            sortedList.setComparator(null); 
            
            // Clear table sort arrows
            tableRDV.getSortOrder().clear(); 
            
            // Sort the actual data
            masterList.sort(cmp);
            
            // Re-bind so header clicks still work afterwards
            sortedList.comparatorProperty().bind(tableRDV.comparatorProperty());
        }
    }

    private void addActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir   = new Button("👁 Voir");
            private final Button btnAccept = new Button("✔ Accepter");
            private final Button btnRefuse = new Button("✘ Refuser");
            private final HBox   pane      = new HBox(8, btnVoir, btnAccept, btnRefuse);

            {
                btnVoir.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                btnAccept.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                btnRefuse.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                pane.setAlignment(javafx.geometry.Pos.CENTER);

                btnVoir.setOnAction(e -> handleView(getIndex()));
                btnAccept.setOnAction(e -> handleAction(getIndex(), "Confirmé"));
                btnRefuse.setOnAction(e -> handleCancel(getIndex()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void handleView(int index) {
        if (index < 0 || index >= tableRDV.getItems().size()) return;
        RDV rdv = tableRDV.getItems().get(index);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/learnhub/medical/view/RDVDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            RDVDetailsController ctrl = loader.getController();
            ctrl.setRDV(rdv);
            ctrl.setViewOnlyMode(true); // Médecin peut voir mais modification via dashboard principal si besoin
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Détails du Rendez-vous");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCancel(int index) {
        if (index < 0 || index >= tableRDV.getItems().size()) return;
        RDV rdv = tableRDV.getItems().get(index);
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment refuser/annuler ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    String msg = penaltyService.processCancellation(rdv.getId());
                    showInfo("Succès", msg);
                    refreshData();
                } catch (Exception e) {
                    showAlert("Règle de Gestion", e.getMessage());
                }
            }
        });
    }

    private void handleAction(int index, String status) {
        if (index < 0 || index >= tableRDV.getItems().size()) return;
        RDV rdv = tableRDV.getItems().get(index);
        try {
            rdvRepo.updateStatus(rdv.getId(), status);
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de mettre à jour le statut");
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
