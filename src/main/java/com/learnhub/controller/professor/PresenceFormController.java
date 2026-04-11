package com.learnhub.controller.professor;

import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.SeanceDAO;
import com.learnhub.models.Presence;
import com.learnhub.models.Seance;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class PresenceFormController {

    @FXML private Label titleLabel;
    @FXML private TableView<Presence> presencesTable;
    @FXML private TableColumn<Presence, String> colNom;
    @FXML private TableColumn<Presence, String> colStatut;

    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private Seance currentSeance;
    private final ObservableList<Presence> attendanceList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
    }

    private void setupTable() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("etudiantNom"));
        colStatut.setCellFactory(param -> new TableCell<>() {
            private final ChoiceBox<String> choice = new ChoiceBox<>(FXCollections.observableArrayList("present", "absent", "retard"));
            {
                choice.setMaxWidth(Double.MAX_VALUE);
                choice.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setStatut(choice.getValue());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Presence p = getTableRow().getItem();
                    if (p != null) {
                        choice.setValue(p.getStatut());
                    }
                    setGraphic(choice);
                }
            }
        });
    }

    public void setSeance(Seance seance) {
        this.currentSeance = seance;
        if (seance != null) {
            titleLabel.setText("Appel : " + seance.getModuleTitre() + " (" + seance.getDate() + ")");
            loadStudents();
        }
    }

    private void loadStudents() {
        try {
            // Load existing presences
            List<Presence> existing = presenceDAO.findBySeance(currentSeance.getId());
            if (existing.isEmpty()) {
                // Initialize for the first time
                List<Utilisateur> students = presenceDAO.findStudentsByModule(currentSeance.getModuleId());
                for (Utilisateur s : students) {
                    Presence p = new Presence();
                    p.setSeanceId(currentSeance.getId());
                    p.setEtudiantId(s.getId());
                    p.setEtudiantNom(s.getNomComplet());
                    p.setStatut("present"); // Default
                    attendanceList.add(p);
                }
            } else {
                attendanceList.setAll(existing);
            }
            presencesTable.setItems(attendanceList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try {
            for (Presence p : attendanceList) {
                presenceDAO.save(p);
            }
            showAlert("Succès", "Les présences ont été enregistrées avec succès.");
            goBack();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        NavigationUtil.navigateTo((Stage) presencesTable.getScene().getWindow(), "/fxml/professor/seances.fxml", "Mes Séances");
    }
}
