package com.learnhub.controller.student;

import com.learnhub.dao.PresenceDAO;
import com.learnhub.models.Presence;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentPresencesController {

    @FXML private Label welcomeLabel;
    @FXML private Label presentsLabel;
    @FXML private Label absentsLabel;
    @FXML private Accordion presencesAccordion;

    @FXML private Circle notifBadge;

    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final com.learnhub.dao.NotificationDAO notificationDAO = new com.learnhub.dao.NotificationDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            loadPresences(currentUser.getId());
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
            popupStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            popupStage.setScene(scene);
            
            Stage mainStage = (Stage) notifBadge.getScene().getWindow();
            popupStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - 225);
            popupStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - 275);
            
            popupStage.showAndWait();
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

    private void loadPresences(int studentId) {
        presencesAccordion.getPanes().clear();
        try {
            List<Presence> presences = presenceDAO.findByEtudiant(studentId);
            
            long presents = presences.stream().filter(p -> "present".equalsIgnoreCase(p.getStatut())).count();
            long absentsAndRetards = presences.stream().filter(p -> !"present".equalsIgnoreCase(p.getStatut())).count();
            
            presentsLabel.setText(String.valueOf(presents));
            absentsLabel.setText(String.valueOf(absentsAndRetards));

            // Group by module
            Map<String, List<Presence>> grouped = presences.stream()
                    .filter(p -> p.getModuleNom() != null)
                    .collect(Collectors.groupingBy(Presence::getModuleNom));

            for (Map.Entry<String, List<Presence>> entry : grouped.entrySet()) {
                String moduleName = entry.getKey();
                List<Presence> modulePresences = entry.getValue();

                // Calculate absences for this module
                long moduleAbsences = modulePresences.stream()
                        .filter(p -> "absent".equalsIgnoreCase(p.getStatut()))
                        .count();

                VBox contentBox = new VBox(0);
                contentBox.setStyle("-fx-background-color: white;");
                
                for (Presence p : modulePresences) {
                    contentBox.getChildren().add(createPresenceRow(p));
                }

                TitledPane pane = new TitledPane();
                
                // Create header with name and potentially a badge
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Label nameLbl = new Label("📖 " + moduleName + " (" + modulePresences.size() + " séances)");
                nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                header.getChildren().add(nameLbl);

                if (moduleAbsences >= 4) {
                    Label eliminatedBadge = new Label("🚫 ÉLIMINÉ");
                    eliminatedBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: 900;");
                    header.getChildren().add(eliminatedBadge);
                } else if (moduleAbsences == 3) {
                    Label warningBadge = new Label("⚠️ DERNIÈRE CHANCE");
                    warningBadge.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");
                    header.getChildren().add(warningBadge);
                }

                pane.setGraphic(header);
                pane.setContent(contentBox);
                presencesAccordion.getPanes().add(pane);
            }

            if (!presencesAccordion.getPanes().isEmpty()) {
                presencesAccordion.setExpandedPane(presencesAccordion.getPanes().get(0));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createPresenceRow(Presence p) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15 25; -fx-border-color: transparent transparent #f3f4f6 transparent; -fx-background-color: white;");

        // Icon based on status
        Label icon = new Label();
        String iconStyle = "-fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 8; -fx-font-size: 16px; -fx-alignment: center;";
        
        if ("present".equalsIgnoreCase(p.getStatut())) {
            icon.setText("✅");
            icon.setStyle(iconStyle + "-fx-background-color: #d1fae5; -fx-text-fill: #059669;");
        } else if ("retard".equalsIgnoreCase(p.getStatut())) {
            icon.setText("🕒");
            icon.setStyle(iconStyle + "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;");
        } else {
            icon.setText("❌");
            icon.setStyle(iconStyle + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
        }

        VBox texts = new VBox(4);
        Label title = new Label("Séance du " + (p.getDateSeance() != null ? p.getDateSeance() : "Date inconnue"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        
        Label sub = new Label("ID Séance: #" + p.getSeanceId());
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        texts.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(p.getStatut().toUpperCase());
        String badgeBase = "-fx-font-size: 10px; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;";
        if ("present".equalsIgnoreCase(p.getStatut())) {
            statusBadge.setStyle(badgeBase + "-fx-background-color: #059669; -fx-text-fill: white;");
        } else if ("retard".equalsIgnoreCase(p.getStatut())) {
            statusBadge.setStyle(badgeBase + "-fx-background-color: #d97706; -fx-text-fill: white;");
        } else {
            statusBadge.setStyle(badgeBase + "-fx-background-color: #dc2626; -fx-text-fill: white;");
        }

        row.getChildren().addAll(icon, texts, spacer, statusBadge);
        return row;
    }

    // Navigation methods
    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/student/modules.fxml", "Mes Modules"); }
    @FXML private void goNotes() { navigate("/fxml/student/notes.fxml", "Mes Notes"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml", "Mes Présences"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml", "Emploi du temps"); }
    @FXML private void goRdv() { navigate("/fxml/student/rdv.fxml", "Mes RDV"); }
    @FXML private void goStages() { navigate("/fxml/student/stages.fxml", "Stages"); }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, title);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}
