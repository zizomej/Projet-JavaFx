package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentAppointmentsController {

    @FXML private VBox      vboxAppointments;
    @FXML private TextField txtSearch;
    @FXML private Label     lblTotalRDV;
    @FXML private Label     lblConfirmedRDV;

    private final RDVRepository     rdvRepo     = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private List<RDV>     allRDVs     = new ArrayList<>();
    private List<Creneau> allCreneaux = new ArrayList<>();

    @FXML
    public void initialize() {
        txtSearch.textProperty().addListener((obs, old, newValue) -> {
            renderCards(newValue);
        });
        loadData();
    }

    @FXML
    public void loadData() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        try {
            allCreneaux = creneauRepo.findAll();
            allRDVs     = rdvRepo.findByStudentId(userId);
            
            long confirmedCount = allRDVs.stream()
                .filter(r -> "Confirmé".equalsIgnoreCase(r.getStatut()) || "Accepté".equalsIgnoreCase(r.getStatut()))
                .count();
            
            lblTotalRDV.setText(String.valueOf(allRDVs.size()));
            lblConfirmedRDV.setText(String.valueOf(confirmedCount));
            
            renderCards(txtSearch.getText());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderCards(String filter) {
        vboxAppointments.getChildren().clear();
        
        List<RDV> filteredList = allRDVs;
        if (filter != null && !filter.isEmpty()) {
            String lower = filter.toLowerCase();
            filteredList = allRDVs.stream()
                .filter(r -> r.getMotif().toLowerCase().contains(lower) || r.getStatut().toLowerCase().contains(lower))
                .collect(Collectors.toList());
        }

        if (filteredList.isEmpty()) {
            Label placeholder = new Label("Aucun rendez-vous trouvé.");
            placeholder.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic; -fx-padding: 20;");
            vboxAppointments.getChildren().add(placeholder);
            return;
        }

        for (RDV rdv : filteredList) {
            try {
                vboxAppointments.getChildren().add(createAppointmentCard(rdv));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Node createAppointmentCard(RDV rdv) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/AppointmentCard.fxml"));
        HBox card = loader.load();
        
        // Find UI elements in card
        Pane  statusIndicator = (Pane)  card.lookup("#statusIndicator");
        Label lblDate         = (Label) card.lookup("#lblDate");
        Label lblTime         = (Label) card.lookup("#lblTime");
        Label lblStatus       = (Label) card.lookup("#lblStatus");
        Label lblDoctor       = (Label) card.lookup("#lblDoctor");
        Label lblMotif        = (Label) card.lookup("#lblMotif");
        Label lblDescription  = (Label) card.lookup("#lblDescription");
        Label lblCompteRendu  = (Label) card.lookup("#lblCompteRendu");
        VBox  boxCompteRendu  = (VBox)  card.lookup("#boxCompteRendu");
        Button btnDetails     = (Button) card.lookup("#btnDetails");
        Button btnRecu        = (Button) card.lookup("#btnRecu");

        // Affichage du bouton REÇU uniquement si c'est payé
        boolean isPaye = "Payé".equalsIgnoreCase(rdv.getStatut());
        btnRecu.setVisible(isPaye);
        btnRecu.setManaged(isPaye);

        if (isPaye) {
            btnRecu.setOnAction(e -> {
                try {
                    FXMLLoader recuLoader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/RecuView.fxml"));
                    Parent recuRoot = recuLoader.load();
                    RecuController recuCtrl = recuLoader.getController();
                    recuCtrl.setData(rdv);
                    
                    Stage recuStage = new Stage();
                    recuStage.setTitle("Récupération de mon Reçu");
                    recuStage.initModality(Modality.APPLICATION_MODAL);
                    recuStage.setScene(new Scene(recuRoot));
                    recuStage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        // Find linked Creneau for exact time
        Creneau linked = allCreneaux.stream()
            .filter(c -> c.getId() == rdv.getCreneauId())
            .findFirst().orElse(null);
            
        // Si non trouvé dans la liste globale, on tente un chargement direct (Sécurité)
        if (linked == null) {
            try {
                linked = creneauRepo.findById(rdv.getCreneauId());
            } catch (SQLException e) { /* ignore */ }
        }

        // Force displayDate from Creneau if possible
        LocalDate consultationDate = null;
        if (linked != null) {
            String jourStr = linked.getJour();
            if (jourStr != null) {
                if (jourStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    consultationDate = LocalDate.parse(jourStr);
                } else {
                    // C'est un nom de jour (Lundi, etc.), on calcule sa date pour la semaine actuelle
                    consultationDate = getDateFromDayName(jourStr);
                }
            }
        }
        
        LocalDate finalDate = (consultationDate != null) ? consultationDate : rdv.getDateDemande();

        if (finalDate != null) {
            lblDate.setText(finalDate.getDayOfMonth() + " " + 
                            getMonthName(finalDate.getMonthValue()) + " " + 
                            finalDate.getYear());
        } else {
            lblDate.setText("Date non définie");
        }
        
        lblTime.setText("🕒 " + (linked != null ? linked.getHeure().toString() : "--:--"));
        lblMotif.setText(rdv.getMotif().toUpperCase());
        lblDescription.setText(rdv.getDescription() != null && !rdv.getDescription().isEmpty() ? rdv.getDescription() : "Aucune remarque particulière.");
        
        // Status Logic & Styling
        String status = (rdv.getStatut() != null) ? rdv.getStatut().toUpperCase() : "EN ATTENTE";
        lblStatus.setText(status);
        
        if (status.contains("CONFIRM") || status.contains("ACCEPT")) {
            statusIndicator.setStyle("-fx-background-color: #10B981; -fx-background-radius: 15 0 0 15;");
            lblStatus.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 6 15; -fx-background-radius: 25; -fx-font-weight: 900; -fx-font-size: 10;");
        } else if (status.contains("TERMIN")) {
            statusIndicator.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 15 0 0 15;");
            lblStatus.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 6 15; -fx-background-radius: 25; -fx-font-weight: 900; -fx-font-size: 10;");
        } else if (status.contains("ANNUL")) {
            statusIndicator.setStyle("-fx-background-color: #EF4444; -fx-background-radius: 15 0 0 15;");
            lblStatus.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 6 15; -fx-background-radius: 25; -fx-font-weight: 900; -fx-font-size: 10;");
        } else {
            statusIndicator.setStyle("-fx-background-color: #F59E0B; -fx-background-radius: 15 0 0 15;");
            lblStatus.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 6 15; -fx-background-radius: 25; -fx-font-weight: 900; -fx-font-size: 10;");
        }

        // Medical report logic (Professional document box)
        if (rdv.getCompteRendu() == null || rdv.getCompteRendu().trim().isEmpty() || rdv.getCompteRendu().equals("null")) {
            boxCompteRendu.setVisible(false);
            boxCompteRendu.setManaged(false);
        } else {
            lblCompteRendu.setText(rdv.getCompteRendu());
        }

        // Dynamic hover effect for PRO feel
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 8); -fx-translate-y: -2;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-translate-y: 0;"));

        // Event for Details Button
        btnDetails.setOnAction(e -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/RDVDetails.fxml"));
                Parent root = fxmlLoader.load();
                
                RDVDetailsController controller = fxmlLoader.getController();
                controller.setRDV(rdv);
                controller.setViewOnlyMode(true); // Lecture seule pour l'élève

                Stage stage = new Stage();
                stage.setTitle("Détails de mon Rendez-vous");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait(); // Attendre la fermeture (ex: après annulation)
                loadData(); // Rafraîchir la liste
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        return card;
    }

    private String getMonthName(int month) {
        String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return (month >= 1 && month <= 12) ? months[month - 1] : "";
    }

    private LocalDate getDateFromDayName(String dayName) {
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        for (int i = 0; i < 7; i++) {
            if (days[i].equalsIgnoreCase(dayName)) {
                return monday.plusDays(i);
            }
        }
        return null;
    }
}
