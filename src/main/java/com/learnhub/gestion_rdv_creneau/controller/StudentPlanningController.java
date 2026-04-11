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
import com.learnhub.util.SessionManager;

public class StudentPlanningController {

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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        lblWeekRange.setText("Semaine du " + startOfWeek.format(dtf) + " au " + endOfWeek.format(dtf));

        for (int i = 0; i < 7; i++) {
            VBox header = new VBox();
            header.setAlignment(Pos.CENTER);
            header.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10; -fx-background-radius: 5;");
            
            Label lblDay = new Label(DAYS[i]);
            lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A8A;");
            
            Label lblDate = new Label(startOfWeek.plusDays(i).format(dtf));
            lblDate.setStyle("-fx-font-size: 11; -fx-opacity: 0.7;");
            
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
                if (jour.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    // Specific date
                    LocalDate slotDate = LocalDate.parse(jour);
                    for (int i = 0; i < 7; i++) {
                        if (startOfWeek.plusDays(i).equals(slotDate)) {
                            planningMap.get(DAYS[i]).add(c);
                        }
                    }
                } else {
                    // Recurring
                    if (planningMap.containsKey(jour)) {
                        planningMap.get(jour).add(c);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Render slots
        for (int i = 0; i < 7; i++) {
            VBox col = new VBox(10);
            col.setAlignment(Pos.TOP_CENTER);
            List<Creneau> daySlots = planningMap.get(DAYS[i]);
            
            if (daySlots.isEmpty()) {
                Label lbl = new Label("Fermé");
                lbl.setStyle("-fx-opacity: 0.3;");
                col.getChildren().add(lbl);
            } else {
                for (Creneau c : daySlots) {
                    col.getChildren().add(createSlotUI(c));
                }
            }
            gridPlanning.add(col, i, 1);
        }
    }

    private VBox createSlotUI(Creneau c) {
        VBox slot = new VBox(5);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefHeight(80);
        slot.setMinWidth(110);
        
        Label lblTime = new Label(c.getHeure().toString());
        lblTime.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        
        Label lblStatus = new Label(c.isDisponibilite() ? "Libre" : "Occupé");
        
        slot.getChildren().addAll(lblTime, lblStatus);
        
        if (c.isDisponibilite()) {
            slot.setStyle("-fx-background-color: #d1fae5; -fx-border-color: #10B981; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
            slot.setOnMouseClicked(e -> showBookingDialog(c));
        } else {
            slot.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #EF4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-opacity: 0.8;");
        }
        
        return slot;
    }

    private void showBookingDialog(Creneau c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Prendre rendez-vous");
        dialog.setHeaderText("Réserver le créneau : " + c.toString());

        VBox content = new VBox(15);
        TextField txtMotif = new TextField();
        txtMotif.setPromptText("Motif (ex: Consultation)");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Description (optionnel)");
        txtDesc.setPrefHeight(100);

        content.getChildren().addAll(new Label("Motif :"), txtMotif, new Label("Description :"), txtDesc);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                saveRDV(c, txtMotif.getText(), txtDesc.getText());
            }
        });
    }

    private void saveRDV(Creneau c, String motif, String desc) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Create RDV
            String sql = "INSERT INTO rdv (motif, description, date_demande, statut, creneau_id, etudiant_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, motif);
            pstmt.setString(2, desc);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setString(4, "Confirmé"); // Simulation simplified
            pstmt.setInt(5, c.getId());
            pstmt.setInt(6, SessionManager.getInstance().getCurrentUserId());
            pstmt.executeUpdate();

            // 2. Mark creneau as unavailable
            String sql2 = "UPDATE creneau SET disponibilite = 0 WHERE id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, c.getId());
            pstmt2.executeUpdate();

            refreshPlanning();
            showAlert("Succès", "Votre rendez-vous a été enregistré !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la réservation : " + e.getMessage());
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



