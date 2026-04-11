package com.learnhub.controller.admin;

import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Seance;
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
import java.util.Optional;

public class SeancesController {

    @FXML private TableView<Seance> table;
    @FXML private TableColumn<Seance, String> colDate;
    @FXML private TableColumn<Seance, String> colHeure;
    @FXML private TableColumn<Seance, String> colModule;
    @FXML private TableColumn<Seance, String> colSalle;
    @FXML private TableColumn<Seance, String> colType;
    @FXML private TableColumn<Seance, Void> colActions;

    @FXML private TextField searchField;
    @FXML private Label totalSeancesLabel;
    @FXML private Label todaySeancesLabel;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ObservableList<Seance> seanceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        
        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleTitre"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

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
            seanceList.setAll(seanceDAO.findAll());
            table.setItems(seanceList);
            totalSeancesLabel.setText(seanceList.size() + " séances");
            
            long today = seanceList.stream()
                .filter(s -> s.getDate().equals(java.time.LocalDate.now().toString()))
                .count();
            todaySeancesLabel.setText(String.valueOf(today));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String filter = searchField.getText().toLowerCase();
        if (filter.isEmpty()) {
            table.setItems(seanceList);
        } else {
            FilteredList<Seance> filtered = new FilteredList<>(seanceList, s -> 
                s.getModuleTitre().toLowerCase().contains(filter) || 
                s.getSalle().toLowerCase().contains(filter)
            );
            table.setItems(filtered);
        }
    }

    @FXML
    private void handleAdd() {
        showForm(null);
    }

    private void handleEdit(Seance seance) {
        showForm(seance);
    }

    private void showForm(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/seance_form.fxml"));
            Parent root = loader.load();
            SeanceFormController controller = loader.getController();
            controller.setSeance(seance);

            Stage stage = (Stage) table.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Seance seance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer la séance ?");
        alert.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                seanceDAO.delete(seance.getId());
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
