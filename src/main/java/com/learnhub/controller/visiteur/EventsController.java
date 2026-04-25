package com.learnhub.controller.visiteur;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class EventsController {

    @FXML private BorderPane rootPane;
    @FXML private FlowPane eventsContainer;
    @FXML private Label eventsCountLabel;

    private final EvenementDAO evenementDAO = new EvenementDAO();

    @FXML
    public void initialize() {
        loadEvents();
    }

    private void loadEvents() {
        if (eventsContainer == null) return;
        eventsContainer.getChildren().clear();

        try {
            List<Evenement> events = evenementDAO.findUpcoming();
            eventsCountLabel.setText(events.size() + (events.size() > 1 ? " événements trouvés" : " événement trouvé"));

            for (Evenement e : events) {
                VBox card = createEventCard(e);
                eventsContainer.getChildren().add(card);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            eventsCountLabel.setText("Erreur de chargement des événements");
        }
    }

    private VBox createEventCard(Evenement e) {
        VBox card = new VBox(15);
        String defaultStyle = "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-pref-width: 450;";
        String hoverStyle = "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #CBD5E1; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(14, 165, 233, 0.15), 15, 0, 0, 6); -fx-pref-width: 450; -fx-cursor: hand;";
        
        card.setStyle(defaultStyle);
        card.setOnMouseEntered(event -> card.setStyle(hoverStyle));
        card.setOnMouseExited(event -> card.setStyle(defaultStyle));

        // Top Header: Date badge + Type
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox dateBadge = new VBox(2);
        dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setStyle("-fx-background-color: linear-gradient(to bottom right, #EFF6FF, #DBEAFE); -fx-border-color: #BFDBFE; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 18;");
        
        String day = e.getDateDebut() != null ? String.valueOf(e.getDateDebut().getDayOfMonth()) : "?";
        String month = e.getDateDebut() != null ? e.getDateDebut().format(DateTimeFormatter.ofPattern("MMM")).toUpperCase() : "?";
        
        Label dayLabel = new Label(day);
        dayLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #1E3A8A;");
        Label monthLabel = new Label(month);
        monthLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #3B82F6; -fx-letter-spacing: 1px;");
        dateBadge.getChildren().addAll(dayLabel, monthLabel);

        VBox typeBox = new VBox(5);
        Label typeLabel = new Label(e.getTypeEvenement() != null ? e.getTypeEvenement().toUpperCase() : "ÉVÉNEMENT");
        typeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #64748B; -fx-letter-spacing: 1.5px;");
        
        Label statusLabel = new Label("• " + e.getStatut());
        String statusColor = "En cours".equalsIgnoreCase(e.getStatut()) ? "#10B981" : "#F59E0B";
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: 900; -fx-font-size: 13px;");
        
        typeBox.getChildren().addAll(typeLabel, statusLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(dateBadge, typeBox, spacer);

        // Body: Title + Description
        Label titleLabel = new Label(e.getTitre());
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");
        titleLabel.setWrapText(true);

        Text descText = new Text(e.getDescription() != null ? e.getDescription() : "Aucune description fournie.");
        descText.setStyle("-fx-font-size: 15px; -fx-fill: #475569; -fx-line-spacing: 5px;");
        descText.setWrappingWidth(400);

        // Footer: Time and Location + Button
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 20 0 0 0; -fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");
        
        VBox infoBox = new VBox(8);
        String timeStr = (e.getHeureDebut() != null) ? "🕒 " + e.getHeureDebut().toString() : "🕒 Non spécifié";
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B; -fx-font-weight: bold;");

        String locStr = (e.getLieuNom() != null && !e.getLieuNom().isEmpty()) ? "📍 " + e.getLieuNom() : "📍 Lieu à confirmer";
        Label locLabel = new Label(locStr);
        locLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B; -fx-font-weight: bold;");
        infoBox.getChildren().addAll(timeLabel, locLabel);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        footer.getChildren().addAll(infoBox, spacer2);

        card.getChildren().addAll(header, titleLabel, descText, footer);
        return card;
    }

    // Navigation Methods
    private Stage getStage() { return (Stage) rootPane.getScene().getWindow(); }

    @FXML private void goHome() { NavigationUtil.navigateTo(getStage(), "/fxml/visiteur/home.fxml", "Accueil"); }
    @FXML private void goPrograms() { }
    @FXML private void goEvents() { NavigationUtil.navigateTo(getStage(), "/fxml/visiteur/events.fxml", "Événements"); }
    @FXML private void goPartners() { }
    @FXML private void goLogin() {
        NavigationUtil.navigateTo(getStage(), "/fxml/auth/login.fxml", "Connexion");
    }
}
