package com.learnhub.controller.admin;

import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Partenaire;
import com.learnhub.models.Filiere;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OffresStageController {

    @FXML private TableView<OffreStage> offreTable;
    @FXML private Label offresActivesLabel;
    @FXML private Label totalCandidaturesLabel;
    @FXML private Label accepteesLabel;
    @FXML private ComboBox<String> filtreType;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private TextField searchField;

    private final OffreStageDAO offreStageDAO = new OffreStageDAO();
    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private ObservableList<OffreStage> offreList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
        loadStats();
    }

    private void setupTable() {
        offreTable.getColumns().clear();

        TableColumn<OffreStage, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<OffreStage, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(200);

        TableColumn<OffreStage, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("typeStage"));
        colType.setPrefWidth(120);

        TableColumn<OffreStage, Integer> colDuree = new TableColumn<>("Durée");
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        colDuree.setPrefWidth(80);

        TableColumn<OffreStage, String> colPartenaire = new TableColumn<>("Partenaire");
        colPartenaire.setCellValueFactory(new PropertyValueFactory<>("partenaireNom"));
        colPartenaire.setPrefWidth(180);

        TableColumn<OffreStage, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            {
                deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    OffreStage offre = getTableView().getItems().get(getIndex());
                    handleDelete(offre);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        offreTable.getColumns().addAll(colId, colTitre, colType, colDuree, colPartenaire, colActions);
        offreTable.setItems(offreList);
    }

    private void setupFilters() {
        filtreType.getItems().addAll("Tous", "pfe", "observation", "initiation");
        filtreType.setValue("Tous");
        filtreType.setOnAction(e -> filterData());

        filtreStatut.getItems().addAll("Tous", "active", "fermee");
        filtreStatut.setValue("Tous");
        filtreStatut.setOnAction(e -> filterData());

        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void loadData() {
        try {
            List<OffreStage> offres = offreStageDAO.findAll();
            offreList.setAll(offres);
            offreTable.setItems(offreList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try {
            offresActivesLabel.setText(String.valueOf(offreStageDAO.count()));
            totalCandidaturesLabel.setText("0");
            accepteesLabel.setText("0");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String search = searchField.getText().toLowerCase();
        String type = filtreType.getValue();

        ObservableList<OffreStage> filtered = FXCollections.observableArrayList();
        for (OffreStage o : offreList) {
            boolean matchSearch = search.isEmpty() || o.getTitre().toLowerCase().contains(search);
            boolean matchType = type.equals("Tous") || o.getTypeStage().equals(type);
            if (matchSearch && matchType) {
                filtered.add(o);
            }
        }
        offreTable.setItems(filtered);
    }

    @FXML
    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleDelete(OffreStage offre) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'offre");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'offre \"" + offre.getTitre() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    offreStageDAO.delete(offre.getId());
                    loadData();
                    loadStats();
                    showAlert("Succès", "Offre supprimée avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    private void showFormDialog(OffreStage offre) {
        Dialog<OffreStage> dialog = new Dialog<>();
        dialog.setTitle(offre == null ? "Ajouter une offre" : "Modifier l'offre");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("pfe", "observation", "initiation");
        TextField dureeField = new TextField();
        dureeField.setPromptText("Durée (mois)");
        ComboBox<Partenaire> partenaireCombo = new ComboBox<>();
        ComboBox<Filiere> filiereCombo = new ComboBox<>();

        try {
            partenaireCombo.setItems(FXCollections.observableArrayList(partenaireDAO.findAll()));
            filiereCombo.setItems(FXCollections.observableArrayList(filiereDAO.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (offre != null) {
            titreField.setText(offre.getTitre());
            descArea.setText(offre.getDescription());
            typeCombo.setValue(offre.getTypeStage());
            dureeField.setText(String.valueOf(offre.getDureeMois()));
        }

        content.getChildren().addAll(
                new Label("Titre:"), titreField,
                new Label("Description:"), descArea,
                new Label("Type:"), typeCombo,
                new Label("Durée (mois):"), dureeField,
                new Label("Partenaire:"), partenaireCombo,
                new Label("Filière:"), filiereCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                OffreStage newOffre = offre != null ? offre : new OffreStage();
                newOffre.setTitre(titreField.getText());
                newOffre.setDescription(descArea.getText());
                newOffre.setTypeStage(typeCombo.getValue());
                newOffre.setDureeMois(Integer.parseInt(dureeField.getText()));
                newOffre.setDatePublication(LocalDate.now());
                if (partenaireCombo.getValue() != null) {
                    newOffre.setPartenaireId(partenaireCombo.getValue().getId());
                    newOffre.setPartenaireNom(partenaireCombo.getValue().getNom());
                }
                if (filiereCombo.getValue() != null) {
                    newOffre.setFiliereId(filiereCombo.getValue().getId());
                    newOffre.setFiliereNom(filiereCombo.getValue().getNom());
                }
                newOffre.setStatut("active");
                return newOffre;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                if (offre == null) {
                    offreStageDAO.insert(result);
                } else {
                    offreStageDAO.update(result);
                }
                loadData();
                loadStats();
                showAlert("Succès", "Offre enregistrée avec succès!");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSearch() { filterData(); }

    @FXML
    private void goBack() {
        Stage stage = (Stage) offreTable.getScene().getWindow();
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
