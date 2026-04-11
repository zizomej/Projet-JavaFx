package com.learnhub.controller.professor;

import com.learnhub.dao.NoteDAO;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ProfessorNotesController {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private Label totalNotesLabel;
    @FXML private Label totalModulesLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Accordion modulesAccordion;

    private final NoteDAO noteDAO = new NoteDAO();
    private List<Note> allNotes = new ArrayList<>();

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText(user.getNomComplet());
        }

        loadData(user != null ? user.getId() : 0);
        searchField.textProperty().addListener((obs, oldV, newV) -> filterData(newV));
    }

    private void loadData(int profId) {
        try {
            allNotes = noteDAO.findByProfesseur(profId);
            refreshUI(allNotes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData(String query) {
        if (query == null || query.isBlank()) {
            refreshUI(allNotes);
            return;
        }
        String lowerQuery = query.toLowerCase();
        List<Note> filtered = allNotes.stream()
            .filter(n -> (n.getEtudiantNom() != null && n.getEtudiantNom().toLowerCase().contains(lowerQuery)) ||
                         (n.getModuleIntitule() != null && n.getModuleIntitule().toLowerCase().contains(lowerQuery)))
            .collect(Collectors.toList());
        refreshUI(filtered);
    }

    private void refreshUI(List<Note> notesToDisplay) {
        modulesAccordion.getPanes().clear();
        if (!modulesAccordion.getStyleClass().contains("notes-accordion")) {
            modulesAccordion.getStyleClass().add("notes-accordion");
        }

        // Stats
        long countModules = notesToDisplay.stream().map(Note::getModuleId).distinct().count();
        long countStudents = notesToDisplay.stream().map(Note::getEtudiantId).distinct().count();
        totalNotesLabel.setText(String.valueOf(notesToDisplay.size()));
        totalModulesLabel.setText(String.valueOf(countModules));
        totalStudentsLabel.setText(String.valueOf(countStudents));

        // Group by module title
        Map<String, List<Note>> notesByModule = notesToDisplay.stream()
                .collect(Collectors.groupingBy(Note::getModuleIntitule));

        for (Map.Entry<String, List<Note>> entry : notesByModule.entrySet()) {
            TitledPane tp = buildModulePane(entry.getKey(), entry.getValue());
            modulesAccordion.getPanes().add(tp);
        }
        
        if (!modulesAccordion.getPanes().isEmpty()) {
            modulesAccordion.setExpandedPane(modulesAccordion.getPanes().get(0));
        }
    }

    // Helper to build a "Pivot" table view for a module
    private TitledPane buildModulePane(String moduleName, List<Note> moduleNotes) {
        TableView<Map<String, Object>> pivotTable = new TableView<>();
        pivotTable.getStyleClass().add("notes-table");
        pivotTable.setStyle("-fx-border-color: transparent;");
        pivotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Types of notes in this module
        Set<String> noteTypes = moduleNotes.stream()
                .map(Note::getTypeNote)
                .collect(Collectors.toSet());

        // Setup columns
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("ÉTUDIANT");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("studentName")));
        nameCol.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        nameCol.setMinWidth(200);
        pivotTable.getColumns().add(nameCol);

        // Sort types (DS1, DS2, TP1, etc.)
        List<String> sortedTypes = new ArrayList<>(noteTypes);
        Collections.sort(sortedTypes);

        for (String type : sortedTypes) {
            TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>(type.toUpperCase());
            typeCol.setCellValueFactory(data -> {
                Note n = (Note) data.getValue().get(type);
                if (n == null) return new SimpleStringProperty("-");
                return new SimpleStringProperty(String.format(Locale.US, "%.2f", n.getValeur()));
            });
            
            // Cell styling like the screenshot
            typeCol.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setAlignment(Pos.CENTER);
                    if (empty || item == null || "-".equals(item)) {
                        setText("-");
                        setGraphic(null);
                        setStyle("-fx-text-fill: #d1d5db; -fx-alignment: center;");
                        setOnMouseClicked(null);
                    } else {
                        setText(null);
                        double val = Double.parseDouble(item);
                        Label lbl = new Label(item);
                        if (val >= 10) {
                            lbl.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 3 8; -fx-background-radius: 8; -fx-cursor: hand;");
                        } else {
                            lbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 3 8; -fx-background-radius: 8; -fx-cursor: hand;");
                        }
                        setStyle("-fx-alignment: center;");
                        setGraphic(lbl);
                        
                        lbl.setOnMouseClicked(evt -> {
                            Map<String, Object> map = getTableView().getItems().get(getIndex());
                            Note n = (Note) map.get(type);
                            showNoteDetails(n);
                        });
                    }
                }
            });
            pivotTable.getColumns().add(typeCol);
        }

        // Moyenne column
        TableColumn<Map<String, Object>, String> moyenneCol = new TableColumn<>("MOYENNE");
        moyenneCol.setCellValueFactory(data -> {
            double sum = 0;
            int count = 0;
            for (String t : sortedTypes) {
                Note n = (Note) data.getValue().get(t);
                if (n != null) {
                    sum += n.getValeur();
                    count++;
                }
            }
            if (count == 0) return new SimpleStringProperty("-");
            double avg = sum / count;
            return new SimpleStringProperty(String.format(Locale.US, "%.2f", avg));
        });
        moyenneCol.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || item == null || "-".equals(item)) {
                    setText("-");
                    setGraphic(null);
                    setStyle("-fx-text-fill: #d1d5db; -fx-alignment: center;");
                } else {
                    setText(null);
                    double val = Double.parseDouble(item);
                    Label lbl = new Label(item);
                    if (val >= 10) {
                        lbl.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 3 8; -fx-background-radius: 8;");
                    } else {
                        lbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 3 8; -fx-background-radius: 8;");
                    }
                    setStyle("-fx-alignment: center;");
                    setGraphic(lbl);
                }
            }
        });
        pivotTable.getColumns().add(moyenneCol);

        // Actions column
        TableColumn<Map<String, Object>, String> actionsCol = new TableColumn<>("ACTIONS");
        actionsCol.setMinWidth(100);
        actionsCol.setMaxWidth(100);
        actionsCol.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Button btn = new Button("👁");
                    btn.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
                    btn.setOnAction(evt -> {
                        Map<String, Object> map = getTableView().getItems().get(getIndex());
                        Note found = null;
                        for(Object val : map.values()) {
                            if (val instanceof Note) {
                                found = (Note) val;
                                break;
                            }
                        }
                        if (found != null) {
                            showNoteDetails(found);
                        }
                    });
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        pivotTable.getColumns().add(actionsCol);

        // Group rows per student
        Map<String, Map<String, Object>> rowsMap = new HashMap<>();
        for (Note n : moduleNotes) {
            String studentName = n.getEtudiantNom();
            rowsMap.putIfAbsent(studentName, new HashMap<>());
            Map<String, Object> row = rowsMap.get(studentName);
            row.put("studentName", studentName);
            row.put(n.getTypeNote(), n); // store Note object for exact value
            row.put("studentId", n.getEtudiantId());
        }

        ObservableList<Map<String, Object>> pivotData = FXCollections.observableArrayList(rowsMap.values());
        pivotTable.setItems(pivotData);
        pivotTable.setPrefHeight(200 + (pivotData.size() * 30));

        TitledPane pane = new TitledPane("📚 " + moduleName, pivotTable);
        pane.setAnimated(true);

        return pane;
    }

    @FXML
    private void handleAdd() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/professor/note_form.fxml"));
            javafx.scene.Parent root = loader.load();
            com.learnhub.controller.professor.NoteFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setScene(new javafx.scene.Scene(root));

            controller.setOnSuccess(() -> {
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadData(user.getId());
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger le formulaire de note.");
            alert.showAndWait();
        }
    }

    private void showNoteDetails(Note note) {
        if (note == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/professor/note_details.fxml"));
            javafx.scene.Parent root = loader.load();
            com.learnhub.controller.professor.NoteDetailsController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setScene(new javafx.scene.Scene(root));

            controller.setNote(note);
            controller.setOnSuccess(() -> {
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                if (user != null) loadData(user.getId());
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navigation methods
    @FXML private void goDashboard() { navigate("/fxml/professor/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/professor/modules.fxml", "Mes Modules"); }
    @FXML private void goSeances() { navigate("/fxml/professor/seances.fxml", "Mes Séances"); }
    @FXML private void goNotes() { navigate("/fxml/professor/notes.fxml", "Gestion des Notes"); }
    
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
