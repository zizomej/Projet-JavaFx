package com.learnhub.controller.admin;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.UniversiteDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Universite;
import com.learnhub.util.NavigationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FilieresController {

    @FXML
    private TableView<Filiere> table;
    @FXML
    private TableColumn<Filiere, String> colCode;
    @FXML
    private TableColumn<Filiere, String> colNom;
    @FXML
    private TableColumn<Filiere, String> colNiveau;
    @FXML
    private TableColumn<Filiere, Integer> colDuree;
    @FXML
    private TableColumn<Filiere, Integer> colCapacite;
    @FXML
    private TableColumn<Filiere, String> colUniversite;
    @FXML
    private TableColumn<Filiere, Void> colActions;

    @FXML
    private Label totalFilieresLabel;
    @FXML
    private Label licenceLabel;
    @FXML
    private Label masterLabel;
    @FXML
    private Label doctoratLabel;
    @FXML
    private TextField searchField;

    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final UniversiteDAO universiteDAO = new UniversiteDAO();
    private ObservableList<Filiere> filiereList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        loadStats();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> filterData());
        }
    }

    private void setupTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeAnnees"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capaciteMax"));

        colUniversite.setCellValueFactory(cellData -> {
            try {
                Universite u = universiteDAO.findById(cellData.getValue().getUniversiteId());
                if (u != null)
                    return new SimpleStringProperty(u.getNom());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("-");
        });

        if (colActions == null) {
            colActions = new TableColumn<>("Actions");
            colActions.setPrefWidth(120);
            table.getColumns().add(colActions);
        }

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");

                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.setItems(filiereList);
    }

    private void loadData() {
        try {
            filiereList.setAll(filiereDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try {
            totalFilieresLabel.setText(filiereDAO.count() + " parcours");

            long licenceCount = filiereList.stream().filter(f -> f.getNiveau().equalsIgnoreCase("Licence")).count();
            long masterCount = filiereList.stream().filter(f -> f.getNiveau().equalsIgnoreCase("Master")).count();
            long doctoratCount = filiereList.stream().filter(f -> f.getNiveau().equalsIgnoreCase("Doctorat")).count();

            if (licenceLabel != null)
                licenceLabel.setText(String.valueOf(licenceCount));
            if (masterLabel != null)
                masterLabel.setText(String.valueOf(masterCount));
            if (doctoratLabel != null)
                doctoratLabel.setText(String.valueOf(doctoratCount));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        if (searchField == null)
            return;
        String search = searchField.getText().toLowerCase();
        try {
            List<Filiere> all = filiereDAO.findAll();
            List<Filiere> filtered = all.stream().filter(f -> f.getNom().toLowerCase().contains(search) ||
                    f.getCode().toLowerCase().contains(search)).collect(Collectors.toList());

            filiereList.setAll(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        NavigationUtil.navigateTo((Stage) table.getScene().getWindow(), "/fxml/admin/filiere_form.fxml",
                "Ajouter une filière");
    }

    private void handleEdit(Filiere filiere) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/admin/filiere_form.fxml"));
            javafx.scene.Parent root = loader.load();

            FiliereFormController controller = loader.getController();
            controller.setFiliere(filiere);

            Stage stage = (Stage) table.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Filiere filiere) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la filière");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer la filière \"" + filiere.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    filiereDAO.delete(filiere.getId());
                    loadData();
                    loadStats();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        filterData();
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null)
            searchField.clear();
        loadData();
        loadStats();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) table.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/admin/dashboard.fxml", "Dashboard");
    }
}
