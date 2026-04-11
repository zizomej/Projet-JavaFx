package com.learnhub.controller.admin;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Partenaire;
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
import java.util.List;

public class PartenairesController {

    @FXML private TableView<Partenaire> partenaireTable;
    @FXML private Label totalPartenairesLabel;
    @FXML private Label actifsLabel;
    @FXML private Label inactifsLabel;
    @FXML private TextField searchField;

    private final PartenaireDAO partenaireDAO = new PartenaireDAO();
    private ObservableList<Partenaire> partenaireList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        loadStats();

        searchField.textProperty().addListener((obs, old, val) -> filterData());
    }

    private void setupTable() {
        partenaireTable.getColumns().clear();

        TableColumn<Partenaire, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Partenaire, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(200);

        TableColumn<Partenaire, String> colSecteur = new TableColumn<>("Secteur");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteur"));
        colSecteur.setPrefWidth(150);

        TableColumn<Partenaire, String> colVille = new TableColumn<>("Ville");
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colVille.setPrefWidth(150);

        TableColumn<Partenaire, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<Partenaire, String> colTelephone = new TableColumn<>("Téléphone");
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colTelephone.setPrefWidth(150);

        TableColumn<Partenaire, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);

        TableColumn<Partenaire, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            {
                deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    Partenaire p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        partenaireTable.getColumns().addAll(colId, colNom, colSecteur, colVille, colEmail, colTelephone, colStatut, colActions);
        partenaireTable.setItems(partenaireList);
    }

    private void loadData() {
        try {
            List<Partenaire> partenaires = partenaireDAO.findAll();
            partenaireList.setAll(partenaires);
            partenaireTable.setItems(partenaireList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try {
            totalPartenairesLabel.setText(String.valueOf(partenaireDAO.count()));
            actifsLabel.setText(String.valueOf(partenaireDAO.countByStatut("actif")));
            inactifsLabel.setText(String.valueOf(partenaireDAO.countByStatut("inactif")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String search = searchField.getText().toLowerCase();
        ObservableList<Partenaire> filtered = FXCollections.observableArrayList();

        for (Partenaire p : partenaireList) {
            if (p.getNom().toLowerCase().contains(search) ||
                    p.getVille().toLowerCase().contains(search) ||
                    p.getEmail().toLowerCase().contains(search)) {
                filtered.add(p);
            }
        }
        partenaireTable.setItems(filtered);
    }

    @FXML
    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleDelete(Partenaire partenaire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le partenaire");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + partenaire.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    partenaireDAO.delete(partenaire.getId());
                    loadData();
                    loadStats();
                    showAlert("Succès", "Partenaire supprimé avec succès!");
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    private void showFormDialog(Partenaire partenaire) {
        Dialog<Partenaire> dialog = new Dialog<>();
        dialog.setTitle(partenaire == null ? "Ajouter un partenaire" : "Modifier le partenaire");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField secteurField = new TextField();
        secteurField.setPromptText("Secteur");
        TextField villeField = new TextField();
        villeField.setPromptText("Ville");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("actif", "inactif");

        if (partenaire != null) {
            nomField.setText(partenaire.getNom());
            secteurField.setText(partenaire.getSecteur());
            villeField.setText(partenaire.getVille());
            emailField.setText(partenaire.getEmail());
            telephoneField.setText(partenaire.getTelephone());
            statutCombo.setValue(partenaire.getStatut());
        } else {
            statutCombo.setValue("actif");
        }

        content.getChildren().addAll(
                new Label("Nom:"), nomField,
                new Label("Secteur:"), secteurField,
                new Label("Ville:"), villeField,
                new Label("Email:"), emailField,
                new Label("Téléphone:"), telephoneField,
                new Label("Statut:"), statutCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                Partenaire p = partenaire != null ? partenaire : new Partenaire();
                p.setNom(nomField.getText());
                p.setSecteur(secteurField.getText());
                p.setVille(villeField.getText());
                p.setEmail(emailField.getText());
                p.setTelephone(telephoneField.getText());
                p.setStatut(statutCombo.getValue());
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                if (partenaire == null) {
                    partenaireDAO.insert(result);
                } else {
                    partenaireDAO.update(result);
                }
                loadData();
                loadStats();
                showAlert("Succès", "Partenaire enregistré avec succès!");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSearch() { filterData(); }

    @FXML
    private void goBack() {
        Stage stage = (Stage) partenaireTable.getScene().getWindow();
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
