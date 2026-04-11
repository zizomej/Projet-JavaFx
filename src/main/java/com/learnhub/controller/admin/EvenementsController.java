package com.learnhub.controller.admin;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
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
import java.time.LocalDate;
import java.util.Optional;

public class EvenementsController {

    @FXML private TableView<Evenement> table;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colType;
    @FXML private TableColumn<Evenement, String> colDate;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, String> colStatut;
    @FXML private TableColumn<Evenement, Void> colActions;

    @FXML private TextField searchField;
    @FXML private Label totalEventsLabel;
    @FXML private Label upcomingEventsLabel;

    private final EvenementDAO evenementDAO = new EvenementDAO();
    private final ObservableList<Evenement> eventList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date")); // Employs the getDate helper in model
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieuNom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color:transparent;-fx-cursor:hand;");
                deleteBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:red;-fx-cursor:hand;");
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

    private void loadData() {
        try {
            eventList.setAll(evenementDAO.findAll());
            table.setItems(eventList);
            totalEventsLabel.setText(eventList.size() + " événements");
            
            long upcoming = eventList.stream()
                .filter(e -> e.getDateDebut() != null && !e.getDateDebut().isBefore(LocalDate.now()))
                .count();
            upcomingEventsLabel.setText(String.valueOf(upcoming));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String filter = searchField.getText().toLowerCase();
        if (filter.isEmpty()) {
            table.setItems(eventList);
        } else {
            FilteredList<Evenement> filtered = new FilteredList<>(eventList, e -> 
                e.getTitre().toLowerCase().contains(filter) || 
                e.getTypeEvenement().toLowerCase().contains(filter) ||
                (e.getLieuNom() != null && e.getLieuNom().toLowerCase().contains(filter))
            );
            table.setItems(filtered);
        }
    }

    @FXML
    private void handleAdd() {
        showForm(null);
    }

    private void handleEdit(Evenement event) {
        showForm(event);
    }

    private void showForm(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/evenement_form.fxml"));
            Parent root = loader.load();
            EvenementFormController controller = loader.getController();
            controller.setEvenement(event);

            Stage stage = (Stage) table.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Evenement event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer l'événement ?");
        alert.setContentText("Cette action supprimera également les inscriptions associées.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                evenementDAO.delete(event.getId());
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), "/fxml/admin/dashboard.fxml", "Tableau de Bord");
    }
}
