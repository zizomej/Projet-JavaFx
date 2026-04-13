package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StudentPlanningController {

    @FXML private Label    lblWeekRange;
    @FXML private Label    lblMonthYear;
    @FXML private GridPane gridPlanning;

    private final CreneauRepository creneauRepo = new CreneauRepository();
    private LocalDate startOfWeek;
    private static final String[] DAYS = {"Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"};

    @FXML
    public void initialize() {
        startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        refreshPlanning();
    }

    @FXML private void handlePrevWeek() { startOfWeek = startOfWeek.minusWeeks(1); refreshPlanning(); }
    @FXML private void handleNextWeek() { startOfWeek = startOfWeek.plusWeeks(1);  refreshPlanning(); }

    @FXML
    public void refreshPlanning() {
        gridPlanning.getChildren().clear();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter dtfMonth = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        
        LocalDate end = startOfWeek.plusDays(6);
        lblWeekRange.setText("Semaine du " + startOfWeek.format(dtf) + " au " + end.format(dtf));
        lblMonthYear.setText(startOfWeek.format(dtfMonth).toUpperCase());

        // Create Day Headers
        for (int i = 0; i < 7; i++) {
            VBox header = new VBox(4);
            header.setAlignment(Pos.CENTER);
            header.getStyleClass().add("day-header");
            Label lDay  = new Label(DAYS[i]);
            lDay.getStyleClass().add("day-name");
            Label lDate = new Label(startOfWeek.plusDays(i).format(dtf));
            lDate.getStyleClass().add("day-date");
            header.getChildren().addAll(lDay, lDate);
            gridPlanning.add(header, i, 0);
        }

        // Fetch data
        Map<String, List<Creneau>> map = new HashMap<>();
        for (String d : DAYS) map.put(d, new ArrayList<>());

        try {
            for (Creneau c : creneauRepo.findAll()) {
                String jour = c.getJour();
                if (jour != null && jour.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate date = LocalDate.parse(jour);
                    for (int i = 0; i < 7; i++)
                        if (startOfWeek.plusDays(i).equals(date))
                            map.get(DAYS[i]).add(c);
                } else if (jour != null && map.containsKey(jour)) {
                    map.get(jour).add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fill columns
        for (int i = 0; i < 7; i++) {
            VBox col = new VBox(10);
            col.setAlignment(Pos.TOP_CENTER);
            col.setStyle("-fx-padding: 5;");
            List<Creneau> slots = map.get(DAYS[i]);
            if (slots.isEmpty()) {
                VBox card = createEmptyCard("Aucun créneau");
                col.getChildren().add(card);
            } else {
                for (Creneau c : slots) {
                    col.getChildren().add(createSlotUI(c));
                }
                // Add "Fermé" placeholder if few slots (following screenshot style)
                if (slots.size() < 2) {
                    col.getChildren().add(createEmptyCard("Fermé"));
                }
            }
            gridPlanning.add(col, i, 1);
        }
    }

    private VBox createEmptyCard(String text) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-padding: 15; -fx-border-style: dashed;");
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13; -fx-font-weight: bold;");
        Label icon = new Label("⚪");
        icon.setStyle("-fx-opacity: 0.3;");
        card.getChildren().addAll(icon, l);
        return card;
    }

    private VBox createSlotUI(Creneau c) {
        VBox slot = new VBox(8);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefHeight(90);
        
        Label lTime = new Label(c.getHeure().toString());
        lTime.setStyle("-fx-font-weight: 900; -fx-font-size: 16; -fx-text-fill: #1E293B;");
        
        Label lStatus = new Label();
        String style = "-fx-background-radius: 12; -fx-padding: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);";
        
        if (c.isDisponibilite()) {
            lStatus.setText("✓ RÉSERVER");
            lStatus.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 10; -fx-letter-spacing: 0.5px; -fx-background-color: #10B981; -fx-background-radius: 20; -fx-padding: 4 12;");
            slot.setStyle(style + "-fx-background-color: #F0FDF4; -fx-border-color: #86EFAC; -fx-border-width: 1.5;");
            slot.setOnMouseClicked(e -> showBookingDialog(c));
        } else {
            lStatus.setText("INDISPONIBLE");
            lStatus.setStyle("-fx-text-fill: #991B1B; -fx-font-weight: 900; -fx-font-size: 10; -fx-letter-spacing: 0.5px;");
            slot.setStyle(style + "-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-border-width: 1.5; -fx-opacity: 0.8;");
        }

        slot.getChildren().addAll(lTime, lStatus);
        
        // Hover effects
        slot.setOnMouseEntered(e -> {
            if (c.isDisponibilite()) slot.setStyle(style + "-fx-background-color: #DCFCE7; -fx-border-color: #22C55E; -fx-translate-y: -2;");
        });
        slot.setOnMouseExited(e -> {
            if (c.isDisponibilite()) slot.setStyle(style + "-fx-background-color: #F0FDF4; -fx-border-color: #86EFAC; -fx-translate-y: 0;");
        });

        return slot;
    }

    private void showBookingDialog(Creneau c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/AddRDVDialog.fxml"));
            Parent root = loader.load();
            AddRDVController ctrl = loader.getController();
            ctrl.setSessionData(this, c);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Prendre RDV");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le formulaire : " + e.getMessage());
            a.show();
        }
    }
}
