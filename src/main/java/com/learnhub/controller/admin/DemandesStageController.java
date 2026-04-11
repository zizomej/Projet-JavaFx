package com.learnhub.controller.admin;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.models.DemandeStage;
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

public class DemandesStageController {

    @FXML private TableView<DemandeStage> demandeTable;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label refuseesLabel;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private TextField searchField;

    private final DemandeStageDAO demandeStageDAO = new DemandeStageDAO();
    private ObservableList<DemandeStage> demandeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
        loadStats();
    }

    private void setupTable() {
        demandeTable.getColumns().clear();

        TableColumn<DemandeStage, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<DemandeStage, String> colEtudiant = new TableColumn<>("Étudiant");
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("etudiantFullName"));
        colEtudiant.setPrefWidth(180);

        TableColumn<DemandeStage, String> colOffre = new TableColumn<>("Offre");
        colOffre.setCellValueFactory(new PropertyValueFactory<>("offreTitre"));
        colOffre.setPrefWidth(200);

        TableColumn<DemandeStage, String> colDate = new TableColumn<>("Date Demande");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colDate.setPrefWidth(120);

        TableColumn<DemandeStage, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);

        TableColumn<DemandeStage, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button accepterBtn = new Button("✅ Accepter");
            private final Button refuserBtn = new Button("❌ Refuser");
            private final HBox buttons = new HBox(5, accepterBtn, refuserBtn);

            {
                accepterBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand;");
                refuserBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");

                accepterBtn.setOnAction(e -> {
                    DemandeStage demande = getTableView().getItems().get(getIndex());
                    updateStatut(demande, "acceptee");
                });

                refuserBtn.setOnAction(e -> {
                    DemandeStage demande = getTableView().getItems().get(getIndex());
                    updateStatut(demande, "refusee");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        demandeTable.getColumns().addAll(colId, colEtudiant, colOffre, colDate, colStatut, colActions);
        demandeTable.setItems(demandeList);
    }

    private void setupFilters() {
        filtreStatut.getItems().addAll("Tous", "en_attente", "acceptee", "refusee");
        filtreStatut.setValue("Tous");
        filtreStatut.setOnAction(e -> filterData());

        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void loadData() {
        try {
            List<DemandeStage> demandes = demandeStageDAO.findAll();
            demandeList.setAll(demandes);
            demandeTable.setItems(demandeList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try {
            enAttenteLabel.setText(String.valueOf(demandeStageDAO.countByStatut("en_attente")));
            accepteesLabel.setText(String.valueOf(demandeStageDAO.countByStatut("acceptee")));
            refuseesLabel.setText(String.valueOf(demandeStageDAO.countByStatut("refusee")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String search = searchField.getText().toLowerCase();
        String statut = filtreStatut.getValue();

        ObservableList<DemandeStage> filtered = FXCollections.observableArrayList();
        for (DemandeStage d : demandeList) {
            boolean matchSearch = search.isEmpty() ||
                    (d.getEtudiantFullName() != null && d.getEtudiantFullName().toLowerCase().contains(search)) ||
                    (d.getOffreTitre() != null && d.getOffreTitre().toLowerCase().contains(search));
            boolean matchStatut = statut.equals("Tous") || d.getStatut().equals(statut);

            if (matchSearch && matchStatut) {
                filtered.add(d);
            }
        }
        demandeTable.setItems(filtered);
    }

    private void updateStatut(DemandeStage demande, String nouveauStatut) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(nouveauStatut.equals("acceptee") ? "Accepter la demande" : "Refuser la demande");
        confirm.setContentText("Êtes-vous sûr de vouloir " +
                (nouveauStatut.equals("acceptee") ? "accepter" : "refuser") +
                " cette demande de stage ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    demandeStageDAO.updateStatut(demande.getId(), nouveauStatut);
                    loadData();
                    loadStats();
                    showAlert("Succès", "Demande " +
                            (nouveauStatut.equals("acceptee") ? "acceptée" : "refusée") + " avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de mettre à jour: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSearch() { filterData(); }

    @FXML
    private void goBack() {
        Stage stage = (Stage) demandeTable.getScene().getWindow();
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
