package com.learnhub.controller.student;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.RdvDAO;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import com.learnhub.models.Notification;
import com.learnhub.dao.NotificationDAO;

import java.sql.SQLException;
import java.util.List;

public class StudentDashboardController {
    @FXML private Label topUserName;
    @FXML private Label welcomeTitleLabel;
    @FXML private Label emailBadge;

    @FXML private Label modulesLabel;
    @FXML private Label moyenneLabel;
    @FXML private Label absencesLabel;
    @FXML private Label stagesLabel;

    @FXML private VBox warningContainer;
    @FXML private Circle notifBadge;

    private final NoteDAO noteDAO = new NoteDAO();
    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final RdvDAO rdvDAO = new RdvDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            topUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            welcomeTitleLabel.setText("Bonjour " + currentUser.getPrenom() + " " + currentUser.getNom() + " ! 👋");
            emailBadge.setText("📧 " + currentUser.getEmail());
            loadStudentData(currentUser.getId());
            checkNotifications(currentUser.getId());
        }
    }

    private void checkNotifications(int userId) {
        try {
            int unread = notificationDAO.countUnread(userId);
            notifBadge.setVisible(unread > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotifications() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/student/notifications_popup.fxml"));
            javafx.scene.Parent root = loader.load();
            
            NotificationsPopupController controller = loader.getController();
            controller.setOnRefresh(() -> checkNotifications(SessionManager.getInstance().getCurrentUser().getId()));
            
            Stage popupStage = new Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Modern look without border
            
            // Add a simple shadow effect and rounded corners (via CSS in FXML)
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popupStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            popupStage.setScene(scene);
            
            // Center the popup relative to the main window
            Stage mainStage = (Stage) notifBadge.getScene().getWindow();
            popupStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - 225);
            popupStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - 275);
            
            popupStage.showAndWait();
            
            // Refresh badge after closing
            checkNotifications(SessionManager.getInstance().getCurrentUser().getId());

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadStudentData(int etudiantId) {
        try {
            List<Note> notes = noteDAO.findByEtudiant(etudiantId);
            double moyenne = calculateMoyenne(notes);
            moyenneLabel.setText(String.format("%.2f", moyenne));

            // Fetch all presences to check for eliminations
            List<com.learnhub.models.Presence> presences = presenceDAO.findByEtudiant(etudiantId);
            
            int totalSessions = presences.size();
            long totalPresents = presences.stream().filter(p -> "present".equalsIgnoreCase(p.getStatut())).count();
            int pourcentagePresence = totalSessions > 0 ? (int)(totalPresents * 100 / totalSessions) : 100;
            absencesLabel.setText(pourcentagePresence + "%");

            // Check for eliminations (absences >= 4 per module)
            warningContainer.getChildren().clear();
            java.util.Map<String, Long> absencesByModule = presences.stream()
                    .filter(p -> "absent".equalsIgnoreCase(p.getStatut()))
                    .collect(java.util.stream.Collectors.groupingBy(com.learnhub.models.Presence::getModuleNom, java.util.stream.Collectors.counting()));

            for (java.util.Map.Entry<String, Long> entry : absencesByModule.entrySet()) {
                if (entry.getValue() >= 4) {
                    addEliminationWarning(entry.getKey(), entry.getValue());
                } else if (entry.getValue() == 3) {
                    addSimpleWarning(entry.getKey(), entry.getValue());
                }
            }

            // For now, Demandes de Stage uses count of RDVs as a placeholder unless there's a DemandeStageDAO
            int rdvCount = rdvDAO.countByEtudiant(etudiantId);
            stagesLabel.setText(String.valueOf(rdvCount));

            modulesLabel.setText(String.valueOf(moduleDAO.count()));

        } catch (SQLException e) {
            e.printStackTrace();
            moyenneLabel.setText("--");
            absencesLabel.setText("--");
            stagesLabel.setText("0");
            modulesLabel.setText("0");
        }
    }

    private void addEliminationWarning(String moduleName, long count) {
        HBox warning = new HBox(15);
        warning.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        warning.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #ef4444; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 20;");
        
        Label icon = new Label("🚫");
        icon.setStyle("-fx-font-size: 20px;");
        
        VBox texts = new VBox(2);
        Label title = new Label("ÉLIMINATION CONFIRMÉE : " + moduleName);
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #991b1b; -fx-font-size: 14px;");
        Label desc = new Label("Vous avez atteint " + count + " absences. Vous ne pouvez plus passer les examens de ce module.");
        desc.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");
        
        texts.getChildren().addAll(title, desc);
        warning.getChildren().addAll(icon, texts);
        warningContainer.getChildren().add(warning);
    }

    private void addSimpleWarning(String moduleName, long count) {
        HBox warning = new HBox(15);
        warning.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        warning.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #f59e0b; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 20;");
        
        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 20px;");
        
        VBox texts = new VBox(2);
        Label title = new Label("ATTENTION : " + moduleName);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #92400e; -fx-font-size: 14px;");
        Label desc = new Label("Vous avez 3 absences. Une absence supplémentaire entraînera votre élimination.");
        desc.setStyle("-fx-text-fill: #b45309; -fx-font-size: 12px;");
        
        texts.getChildren().addAll(title, desc);
        warning.getChildren().addAll(icon, texts);
        warningContainer.getChildren().add(warning);
    }

    private double calculateMoyenne(List<Note> notes) {
        if (notes.isEmpty()) return 0;
        double total = 0;
        double coefTotal = 0;
        for (Note n : notes) {
            total += n.getValeur() * n.getCoefficient();
            coefTotal += n.getCoefficient();
        }
        return coefTotal > 0 ? total / coefTotal : 0;
    }

    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/student/modules.fxml", "Mes Modules"); }
    @FXML private void goNotes() { navigate("/fxml/student/notes.fxml", "Mes Notes"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml", "Mes Présences"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml", "Emploi du temps"); }
    @FXML private void goRdv() { navigate("/fxml/student/rdv.fxml", "Mes RDV"); }
    @FXML private void goStages() { navigate("/fxml/student/stages.fxml", "Stages"); }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) modulesLabel.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, title);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) modulesLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}