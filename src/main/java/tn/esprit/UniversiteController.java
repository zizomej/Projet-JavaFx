package tn.esprit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class UniversiteController implements Initializable {

    @FXML private Label lblCountPubliques;
    @FXML private Label lblCountPrivees;
    @FXML private Label lblCountTotal;
    @FXML private Label lblTotalUniversites;

    @FXML private TextField tfSearchFilter;
    @FXML private ComboBox<String> cbTypeFilter;

    @FXML private TableView<Universite> tableView;
    @FXML private TableColumn<Universite, String> colNom;
    @FXML private TableColumn<Universite, String> colType;
    @FXML private TableColumn<Universite, String> colVille;
    @FXML private TableColumn<Universite, String> colTelephone;
    @FXML private TableColumn<Universite, String> colEmail;
    @FXML private TableColumn<Universite, Integer> colFilieres;
    @FXML private TableColumn<Universite, Universite> colActions;

    private ObservableList<Universite> univList = FXCollections.observableArrayList();

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_universitaire", "root", "");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        setupTableColumns();
        
        // Setup table if empty (graceful failure if DB table 'universite' missing)
        createTableIfNotExists();
        
        loadDataAndRefresh();
    }

    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS universite (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nom VARCHAR(255) NOT NULL," +
                "type VARCHAR(50)," +
                "ville VARCHAR(255)," +
                "telephone VARCHAR(50)," +
                "email VARCHAR(255)," +
                "filieres_count INT DEFAULT 0" +
                ")";
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            if (conn != null) st.execute(query);
        } catch(Exception e) {}
    }

    private void setupComboBoxes() {
        cbTypeFilter.setItems(FXCollections.observableArrayList("Tous les types", "Publique", "Privée"));
        cbTypeFilter.getSelectionModel().selectFirst();
        
        tfSearchFilter.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        cbTypeFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 10;");

        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colTelephone.setStyle("-fx-alignment: CENTER;");

        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setStyle("-fx-alignment: CENTER;");

        colFilieres.setCellValueFactory(new PropertyValueFactory<>("filieresCount"));
        colFilieres.setStyle("-fx-alignment: CENTER; -fx-text-fill: #17a563; -fx-font-weight: bold;");

        // Custom Type Column (Badge)
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(type);
                    if (type.toLowerCase().contains("publiqu")) {
                        lbl.getStyleClass().add("badge-publique");
                        lbl.setGraphic(new Label("🏫"));
                    } else {
                        lbl.getStyleClass().add("badge-privee");
                        lbl.setGraphic(new Label("🍷")); // Icon representation or empty
                    }
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Custom Ville Column (Icon + text)
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colVille.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String ville, boolean empty) {
                super.updateItem(ville, empty);
                if (empty || ville == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label("📍 " + ville);
                    lbl.setStyle("-fx-text-fill: #333;");
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Custom Actions Column
        colActions.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Universite univ, boolean empty) {
                super.updateItem(univ, empty);
                if (empty || univ == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER);

                    Button btnEdit = new Button("✎");
                    btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-font-size: 16px; -fx-cursor: hand;");
                    btnEdit.setOnAction(e -> openEditDialog(univ));

                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-size: 16px; -fx-cursor: hand;");
                    btnDelete.setOnAction(e -> deleteData(univ));

                    box.getChildren().addAll(btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadDataAndRefresh() {
        univList.clear();
        Connection conn = getConnection();
        if (conn == null) return;
        
        int publiques = 0, privees = 0;

        String query = "SELECT * FROM universite";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next()) {
                Universite u = new Universite(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("type"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getString("telephone"),
                    rs.getString("email"),
                    0
                );
                univList.add(u);

                if (u.getType() != null && u.getType().toLowerCase().contains("publiqu")) publiques++;
                else if (u.getType() != null && u.getType().toLowerCase().contains("priv")) privees++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tableView.setItems(univList);
        lblCountPubliques.setText(String.valueOf(publiques));
        lblCountPrivees.setText(String.valueOf(privees));
        lblCountTotal.setText(String.valueOf(univList.size()));
        lblTotalUniversites.setText(univList.size() + " établissements");
    }

    @FXML
    private void applyFilters() {
        String search = tfSearchFilter.getText() == null ? "" : tfSearchFilter.getText().toLowerCase().trim();
        String typeFilter = cbTypeFilter.getValue();
        
        ObservableList<Universite> filtered = FXCollections.observableArrayList();
        for (Universite u : univList) {
            boolean matchSearch = search.isEmpty() || 
                (u.getNom() != null && u.getNom().toLowerCase().contains(search)) ||
                (u.getVille() != null && u.getVille().toLowerCase().contains(search)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(search));
                
            boolean matchType = typeFilter == null || typeFilter.equals("Tous les types");
            if (!matchType && u.getType() != null) {
                 String dbType = u.getType().toLowerCase();
                 String fType = typeFilter.toLowerCase();
                 if (fType.contains("publiqu") && dbType.contains("publiqu")) matchType = true;
                 else if (fType.contains("priv") && dbType.contains("priv")) matchType = true;
            }
                
            if (matchSearch && matchType) {
                filtered.add(u);
            }
        }
        tableView.setItems(filtered);
        lblTotalUniversites.setText(filtered.size() + " établissements");
    }

    @FXML
    private void openAddDialog(ActionEvent event) {
        showDialog(null);
    }

    private void openEditDialog(Universite univ) {
        showDialog(univ);
    }

    private void deleteData(Universite univ) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cet établissement ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            String query = "DELETE FROM universite WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, univ.getId());
                ps.executeUpdate();
                loadDataAndRefresh();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showDialog(Universite existingData) {
        Dialog<Universite> dialog = new Dialog<>();
        dialog.setTitle(existingData == null ? "Ajouter une Université" : "Modifier une Université");
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField(); nom.setPromptText("Nom de l'université");
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Publique", "Privée"));
        type.getSelectionModel().selectFirst();
        TextField ville = new TextField(); ville.setPromptText("Ville");
        TextField adresse = new TextField(); adresse.setPromptText("Adresse");
        TextField telephone = new TextField(); telephone.setPromptText("Téléphone");
        TextField email = new TextField(); email.setPromptText("Email");
        TextField filieresCount = new TextField("0"); filieresCount.setPromptText("Nombre de filières");

        if (existingData != null) {
            nom.setText(existingData.getNom());
            type.setValue(existingData.getType());
            ville.setText(existingData.getVille());
            adresse.setText(existingData.getAdresse());
            telephone.setText(existingData.getTelephone());
            email.setText(existingData.getEmail());
            filieresCount.setText(String.valueOf(existingData.getFilieresCount()));
        }

        grid.add(new Label("Nom:"), 0, 0); grid.add(nom, 1, 0);
        grid.add(new Label("Type:"), 0, 1); grid.add(type, 1, 1);
        grid.add(new Label("Ville:"), 0, 2); grid.add(ville, 1, 2);
        grid.add(new Label("Adresse:"), 0, 3); grid.add(adresse, 1, 3);
        grid.add(new Label("Téléphone:"), 0, 4); grid.add(telephone, 1, 4);
        grid.add(new Label("Email:"), 0, 5); grid.add(email, 1, 5);
        grid.add(new Label("Nb Filières:"), 0, 6); grid.add(filieresCount, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == saveButtonType) {
                int count = 0;
                try { count = Integer.parseInt(filieresCount.getText()); } catch(Exception e){}
                return new Universite(existingData != null ? existingData.getId() : 0,
                        nom.getText(), type.getValue(), ville.getText(), adresse.getText(), telephone.getText(), email.getText(), count);
            }
            return null;
        });

        Optional<Universite> result = dialog.showAndWait();
        result.ifPresent(u -> {
            boolean isUpdate = (existingData != null);
            String q = isUpdate 
                ? "UPDATE universite SET nom=?, type=?, ville=?, adresse=?, telephone=?, email=? WHERE id=?"
                : "INSERT INTO universite (nom, type, ville, adresse, telephone, email) VALUES (?,?,?,?,?,?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setString(1, u.getNom());
                ps.setString(2, u.getType());
                ps.setString(3, u.getVille());
                ps.setString(4, u.getAdresse());
                ps.setString(5, u.getTelephone());
                ps.setString(6, u.getEmail());
                if(isUpdate) ps.setInt(7, u.getId());
                ps.executeUpdate();
                loadDataAndRefresh();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void goToUtilisateurs(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/utilisateur.fxml"));
            tableView.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/dashboard.fxml"));
            tableView.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
