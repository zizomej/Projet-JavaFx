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
import javafx.scene.layout.HBox;
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
            private final ToggleButton btnPresent = new ToggleButton("Présent");
            private final ToggleButton btnAbsent = new ToggleButton("Absent");
            private final ToggleButton btnRetard = new ToggleButton("Retard");
            private final ToggleGroup group = new ToggleGroup();
            private final HBox pane = new HBox(8, btnPresent, btnAbsent, btnRetard);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                btnPresent.setToggleGroup(group);
                btnAbsent.setToggleGroup(group);
                btnRetard.setToggleGroup(group);

                btnPresent.getStyleClass().addAll("attendance-chip", "attendance-chip-present");
                btnAbsent.getStyleClass().addAll("attendance-chip", "attendance-chip-absent");
                btnRetard.getStyleClass().addAll("attendance-chip", "attendance-chip-retard");

                btnPresent.setMinWidth(80);
                btnAbsent.setMinWidth(80);
                btnRetard.setMinWidth(80);

                btnPresent.setOnAction(e -> updateStatut("present"));
                btnAbsent.setOnAction(e -> updateStatut("absent"));
                btnRetard.setOnAction(e -> updateStatut("retard"));
            }

            private void updateStatut(String statut) {
                if (getTableRow() != null && getTableRow().getItem() != null) {
                    getTableRow().getItem().setStatut(statut);
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Presence p = getTableRow().getItem();
                    if (p != null) {
                        String s = p.getStatut();
                        if ("present".equals(s)) btnPresent.setSelected(true);
                        else if ("absent".equals(s)) btnAbsent.setSelected(true);
                        else if ("retard".equals(s)) btnRetard.setSelected(true);
                    }
                    setGraphic(pane);
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
            // Load ALL students for the module
            List<Utilisateur> allStudents = presenceDAO.findStudentsByModule(currentSeance.getModuleId());
            
            // Load existing presences for this specific session
            List<Presence> existing = presenceDAO.findBySeance(currentSeance.getId());
            
            // Create a map of studentId -> Presence for easy lookup
            java.util.Map<Integer, Presence> existingMap = existing.stream()
                    .collect(java.util.stream.Collectors.toMap(Presence::getEtudiantId, p -> p));
            
            attendanceList.clear();
            
            for (Utilisateur s : allStudents) {
                if (existingMap.containsKey(s.getId())) {
                    // Use existing record
                    attendanceList.add(existingMap.get(s.getId()));
                } else {
                    // Create new default record for this student
                    Presence p = new Presence();
                    p.setSeanceId(currentSeance.getId());
                    p.setEtudiantId(s.getId());
                    p.setEtudiantNom(s.getNomComplet());
                    p.setStatut("present"); // Default
                    attendanceList.add(p);
                }
            }
            presencesTable.setItems(attendanceList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de données", "Impossible de charger les étudiants : " + e.getMessage());
        }
    }

    private final com.learnhub.dao.NotificationDAO notificationDAO = new com.learnhub.dao.NotificationDAO();

    @FXML
    private void handleSave() {
        try {
            com.learnhub.dao.UtilisateurDAO userDAO = new com.learnhub.dao.UtilisateurDAO();
            
            for (Presence p : attendanceList) {
                presenceDAO.save(p);
                
                // If student is absent, check for alerts
                if ("absent".equalsIgnoreCase(p.getStatut())) {
                    int absenceCount = presenceDAO.getAbsenceCountByModule(p.getEtudiantId(), currentSeance.getModuleId());
                    
                    // Trigger alert for significant thresholds (3 or 4)
                    if (absenceCount == 3 || absenceCount == 4) {
                        Utilisateur student = userDAO.findById(p.getEtudiantId());
                        if (student != null) {
                            // 1. Send Email (Simulation)
                            com.learnhub.util.EmailService.sendEliminationAlert(student, currentSeance.getModuleTitre(), absenceCount);
                            
                            // 2. Create In-App Notification
                            com.learnhub.models.Notification note = new com.learnhub.models.Notification();
                            note.setUtilisateurId(student.getId());
                            note.setType(absenceCount >= 4 ? "ELIMINATION" : "WARNING");
                            note.setMessage(absenceCount >= 4 
                                ? "🚫 Vous avez été ÉLIMINÉ du module " + currentSeance.getModuleTitre() + " (" + absenceCount + " absences)."
                                : "⚠️ Attention ! Vous avez 3 absences dans le module " + currentSeance.getModuleTitre() + ". Dernière chance !");
                            notificationDAO.insert(note);
                        }
                    }
                }
            }
            showAlert("Succès", "Les présences ont été enregistrées. Les notifications (E-mail et In-App) ont été envoyées.");
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
