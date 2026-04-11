package com.learnhub.gestion_rdv_creneau.controller;

import com.learnhub.gestion_rdv_creneau.entity.Creneau;
import com.learnhub.gestion_rdv_creneau.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CreneauManagementController {

    @FXML private Label lblWeekRange;
    @FXML private GridPane gridPlanning;

    private LocalDate startOfWeek;
    private static final String[] DAYS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

    @FXML
    public void initialize() {
        startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        refreshPlanning();
    }

    @FXML
    private void handlePrevWeek() {
        startOfWeek = startOfWeek.minusWeeks(1);
        refreshPlanning();
    }

    @FXML
    private void handleNextWeek() {
        startOfWeek = startOfWeek.plusWeeks(1);
        refreshPlanning();
    }

    @FXML
    public void refreshPlanning() {
        gridPlanning.getChildren().clear();
        updateHeaderLabels();
        loadSlots();
    }

    private void updateHeaderLabels() {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM");
        lblWeekRange.setText(startOfWeek.format(dtf).toUpperCase() + " - " + endOfWeek.format(dtf).toUpperCase() + " " + startOfWeek.getYear());

        for (int i = 0; i < 7; i++) {
            VBox header = new VBox(5);
            header.getStyleClass().add("day-header");
            
            Label lblDay = new Label(DAYS[i]);
            lblDay.getStyleClass().add("day-name");
            
            Label lblDate = new Label(startOfWeek.plusDays(i).format(DateTimeFormatter.ofPattern("dd/MM")));
            lblDate.getStyleClass().add("day-date");
            
            header.getChildren().addAll(lblDay, lblDate);
            gridPlanning.add(header, i, 0);
        }
    }

    private void loadSlots() {
        Map<String, List<Creneau>> planningMap = new HashMap<>();
        for (String day : DAYS) planningMap.put(day, new ArrayList<>());

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM creneau")) {

            while (rs.next()) {
                Creneau c = new Creneau(
                    rs.getInt("id"),
                    rs.getString("jour"),
                    rs.getTime("heure").toLocalTime(),
                    rs.getString("recurrence"),
                    rs.getBoolean("disponibilite")
                );
                
                String jour = c.getJour();
                if (jour != null && jour.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate slotDate = LocalDate.parse(jour);
                    for (int i = 0; i < 7; i++) {
                        if (startOfWeek.plusDays(i).equals(slotDate)) {
                            planningMap.get(DAYS[i]).add(c);
                        }
                    }
                } else if (jour != null && planningMap.containsKey(jour)) {
                    planningMap.get(jour).add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Render slots
        for (int i = 0; i < 7; i++) {
            VBox col = new VBox(15);
            col.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
            col.setAlignment(Pos.TOP_CENTER);
            List<Creneau> daySlots = planningMap.get(DAYS[i]);
            
            if (daySlots.isEmpty()) {
                Label lbl = new Label("Aucun créneau");
                lbl.setStyle("-fx-opacity: 0.3; -fx-font-style: italic; -fx-padding: 20 0 0 0;");
                col.getChildren().add(lbl);
            } else {
                for (Creneau c : daySlots) {
                    col.getChildren().add(createSlotCard(c));
                }
            }
            gridPlanning.add(col, i, 1);
        }
    }

    private VBox createSlotCard(Creneau c) {
        VBox card = new VBox(8);
        card.getStyleClass().add("slot-card");
        if (c.isDisponibilite()) {
            card.getStyleClass().add("slot-card-available");
        } else {
            card.getStyleClass().add("slot-card-reserved");
        }
        
        Label lblTime = new Label(c.getHeure().toString());
        lblTime.getStyleClass().add("slot-time");
        
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(c.isDisponibilite() ? "✓" : "✘");
        Label lblStatus = new Label(c.isDisponibilite() ? "Disponible" : "Réservé");
        lblStatus.getStyleClass().add("slot-status");
        lblStatus.getStyleClass().add(c.isDisponibilite() ? "status-available" : "status-reserved");
        icon.getStyleClass().add(c.isDisponibilite() ? "status-available" : "status-reserved");
        
        statusBox.getChildren().addAll(icon, lblStatus);
        
        card.getChildren().addAll(lblTime, statusBox);
        
        // Context menu for delete
        ContextMenu menu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Supprimer le créneau");
        deleteItem.setOnAction(e -> deleteCreneau(c));
        menu.getItems().add(deleteItem);
        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));
        
        return card;
    }

    private void deleteCreneau(Creneau c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer ce créneau ?");
        alert.setContentText("Cette action est irréversible.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM creneau WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, c.getId());
                pstmt.executeUpdate();
                refreshPlanning();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/gestion_rdv_creneau/view/AddCreneauDialog.fxml"));
            Parent root = loader.load();
            
            AddCreneauController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouveau Créneau");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
