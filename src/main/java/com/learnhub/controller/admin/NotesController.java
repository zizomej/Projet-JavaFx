package com.learnhub.controller.admin;

import com.learnhub.dao.NoteDAO;
import com.learnhub.models.Note;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class NotesController {

    @FXML private TableView<Note> noteTable;
    @FXML private TableColumn<Note, String> colModule;
    @FXML private TableColumn<Note, String> colEtudiant;
    @FXML private TableColumn<Note, String> colType;
    @FXML private TableColumn<Note, Double> colNote;
    @FXML private TableColumn<Note, Double> colCoefficient;
    @FXML private TableColumn<Note, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboModule;
    @FXML private ComboBox<String> comboEtudiant;
    @FXML private ComboBox<String> comboType;

    @FXML private Label lblTotalNotes;
    @FXML private Label lblMoyenne;
    @FXML private Label lblTaux;

    private final NoteDAO noteDAO = new NoteDAO();
    private ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private ObservableList<Note> filteredNotes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        // Listeners for filters
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        comboModule.valueProperty().addListener((obs, old, val) -> applyFilters());
        comboEtudiant.valueProperty().addListener((obs, old, val) -> applyFilters());
        comboType.valueProperty().addListener((obs, old, val) -> applyFilters());
    }

    private void setupTable() {
        noteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colModule.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getModuleIntitule() != null ? data.getValue().getModuleIntitule() : "N/A"
        ));
        colModule.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A8A;");

        colEtudiant.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEtudiantNom() != null ? data.getValue().getEtudiantNom() : "N/A"
        ));
        colEtudiant.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        colCoefficient.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        colCoefficient.setStyle("-fx-font-weight: bold; -fx-alignment: center-left; -fx-text-fill: #111827;");

        // Styling the Note column (e.g. 1/20) with coloring based on score
        colNote.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        colNote.setCellFactory(column -> new TableCell<Note, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format(Locale.US, "%.2f/20", item));
                    if (item >= 10.0) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: 900; -fx-alignment: center-left;"); // Green
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 900; -fx-alignment: center-left;"); // Red
                    }
                }
            }
        });

        // Styling the Type column as a colored Pill
        colType.setCellValueFactory(new PropertyValueFactory<>("typeNote"));
        colType.setCellFactory(column -> new TableCell<Note, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label("📁 " + item.toUpperCase());
                    // Assign generic nice colors based on type text
                    if (item.toLowerCase().contains("exam") || item.toLowerCase().contains("ds")) {
                        lbl.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 900;");
                    } else if (item.toLowerCase().contains("projet")) {
                        lbl.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #7e22ce; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 900;");
                    } else {
                        lbl.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 900;");
                    }
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER_LEFT);
                }
                setText(null);
            }
        });

        // Setup the Actions column with a Trash bin
        colActions.setCellFactory(column -> new TableCell<Note, Void>() {
            private final Button btnDel = new Button("🗑");
            {
                btnDel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
                btnDel.setOnAction(e -> {
                    Note note = getTableView().getItems().get(getIndex());
                    deleteNote(note);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDel);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        noteTable.setItems(filteredNotes);
    }

    private void loadData() {
        try {
            List<Note> notes = noteDAO.findAll();
            allNotes.setAll(notes);
            
            // Populate ComboBoxes
            comboModule.getItems().clear();
            comboModule.getItems().add("📚 Tous les modules");
            allNotes.stream().map(Note::getModuleIntitule).filter(s -> s != null).distinct().forEach(comboModule.getItems()::add);
            comboModule.setValue("📚 Tous les modules");

            comboEtudiant.getItems().clear();
            comboEtudiant.getItems().add("🎓 Tous les étudiants");
            allNotes.stream().map(Note::getEtudiantNom).filter(s -> s != null).distinct().forEach(comboEtudiant.getItems()::add);
            comboEtudiant.setValue("🎓 Tous les étudiants");

            comboType.getItems().clear();
            comboType.getItems().add("📝 Tous les types");
            allNotes.stream().map(Note::getTypeNote).filter(s -> s != null).distinct().forEach(comboType.getItems()::add);
            comboType.setValue("📝 Tous les types");

            applyFilters();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        filteredNotes.clear();
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String modFilter = comboModule.getValue() == null ? "" : comboModule.getValue();
        String etuFilter = comboEtudiant.getValue() == null ? "" : comboEtudiant.getValue();
        String typeFilter = comboType.getValue() == null ? "" : comboType.getValue();

        for (Note n : allNotes) {
            boolean matchSearch = n.getEtudiantNom().toLowerCase().contains(search) || n.getModuleIntitule().toLowerCase().contains(search);
            boolean matchMod = modFilter.startsWith("📚") || modFilter.equals(n.getModuleIntitule());
            boolean matchEtu = etuFilter.startsWith("🎓") || etuFilter.equals(n.getEtudiantNom());
            boolean matchType = typeFilter.startsWith("📝") || typeFilter.equals(n.getTypeNote());

            if (matchSearch && matchMod && matchEtu && matchType) {
                filteredNotes.add(n);
            }
        }
        
        updateStats();
    }

    private void updateStats() {
        int total = filteredNotes.size();
        lblTotalNotes.setText(String.valueOf(total));

        if (total == 0) {
            lblMoyenne.setText("0.00");
            lblTaux.setText("0%");
            return;
        }

        double sum = 0;
        int successCount = 0;
        for (Note n : filteredNotes) {
            sum += n.getValeur();
            if (n.getValeur() >= 10.0) {
                successCount++;
            }
        }

        double moy = sum / total;
        double taux = (double) successCount / total * 100.0;

        lblMoyenne.setText(String.format(Locale.US, "%.2f", moy));
        lblTaux.setText(String.format(Locale.US, "%.1f%%", taux));
    }

    private void deleteNote(Note note) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette note ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                noteDAO.delete(note.getId());
                loadData();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML private void goDashboard() { navigate("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/admin/modules.fxml", "Gestion des Modules"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) searchField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) searchField.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
