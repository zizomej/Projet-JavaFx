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

public class CreneauManagementController {

    @FXML private Label     lblWeekRange;
    @FXML private GridPane  gridPlanning;

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
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        lblWeekRange.setText("Semaine du " + startOfWeek.format(dtf) + " au " + endOfWeek.format(dtf));

        // Premium Day Headers
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

        // Fetch Slots
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
            showAlert("Erreur", "Impossible de charger les créneaux : " + e.getMessage());
        }

        // Fill Weekly Grid
        for (int i = 0; i < 7; i++) {
            VBox col = new VBox(10);
            col.setAlignment(Pos.TOP_CENTER);
            col.setStyle("-fx-padding: 5;");
            List<Creneau> slots = map.get(DAYS[i]);
            if (slots.isEmpty()) {
                VBox emptyCard = new VBox(10);
                emptyCard.setAlignment(Pos.CENTER);
                emptyCard.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 15; -fx-opacity: 0.5;");
                Label l = new Label("Pas de créneau");
                l.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11;");
                emptyCard.getChildren().add(l);
                col.getChildren().add(emptyCard);
            } else {
                for (Creneau c : slots) col.getChildren().add(createCard(c));
            }
            gridPlanning.add(col, i, 1);
        }
    }

    private VBox createCard(Creneau c) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(80);
        
        String baseStyle = "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;";
        if (c.isDisponibilite()) {
            card.setStyle(baseStyle + "-fx-background-color: #F0FDF4; -fx-border-color: #22C55E;");
        } else {
            card.setStyle(baseStyle + "-fx-background-color: #FEF2F2; -fx-border-color: #EF4444;");
        }

        Label lTime   = new Label(c.getHeure().toString());
        lTime.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");
        
        Label lStatus = new Label(c.isDisponibilite() ? "Disponible" : "Réservé");
        lStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " +
            (c.isDisponibilite() ? "#166534;" : "#991B1B;"));

        card.getChildren().addAll(lTime, lStatus);

        // Interaction au clic : Modifier
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                showEditDialog(c);
            }
        });

        // Menu contextuel: Supprimer / Basculer disponibilité
        ContextMenu menu = new ContextMenu();
        MenuItem miToggle = new MenuItem(c.isDisponibilite() ? "Marquer Réservé" : "Marquer Disponible");
        miToggle.setOnAction(e -> {
            try { creneauRepo.updateAvailability(c.getId(), !c.isDisponibilite()); refreshPlanning(); }
            catch (SQLException ex) { ex.printStackTrace(); }
        });
        MenuItem miDelete = new MenuItem("Supprimer");
        miDelete.setStyle("-fx-text-fill: #EF4444;");
        miDelete.setOnAction(e -> deleteCreneau(c));
        menu.getItems().addAll(miToggle, new SeparatorMenuItem(), miDelete);
        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));

        return card;
    }

    private void deleteCreneau(Creneau c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le créneau " + c.getHeure() + " (" + c.getJour() + ") ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { creneauRepo.delete(c.getId()); refreshPlanning(); }
            catch (SQLException e) { e.printStackTrace(); showAlert("Erreur", e.getMessage()); }
        }
    }

    @FXML
    private void showAddDialog() {
        openDialog(null);
    }

    private void showEditDialog(Creneau c) {
        openDialog(c);
    }

    private void openDialog(Creneau c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/learnhub/medical/view/AddCreneauDialog.fxml"));
            Parent root = loader.load();
            AddCreneauController ctrl = loader.getController();
            ctrl.setParentController(this);
            
            if (c != null) {
                ctrl.setEditData(c);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(c == null ? "Nouveau Créneau" : "Modifier le Créneau");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
