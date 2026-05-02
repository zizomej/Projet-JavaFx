package com.learnhub.controller.student;

import com.learnhub.dao.NotificationDAO;
import com.learnhub.models.Notification;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationsPopupController {

    @FXML private ListView<Notification> notificationsList;
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private Runnable onRefresh;

    @FXML
    public void initialize() {
        setupListView();
        loadNotifications();
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    private void setupListView() {
        notificationsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    boolean isElimination = n.getType().equalsIgnoreCase("ELIMINATION");
                    String accentColor = isElimination ? "#ef4444" : "#f59e0b";
                    String lightBg = isElimination ? "#fef2f2" : "#fffbeb";
                    String iconBg = isElimination ? "#fee2e2" : "#fef3c7";

                    HBox card = new HBox(0);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: " + (n.isLu() ? "#ffffff" : lightBg) + "; " +
                                 "-fx-background-radius: 12; -fx-border-radius: 12; " +
                                 "-fx-border-color: " + (n.isLu() ? "#f1f5f9" : accentColor) + "; " +
                                 "-fx-border-width: 0 0 0 6; " + // Vertical bar on left
                                 "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 4);");

                    VBox content = new VBox(12);
                    content.setPadding(new javafx.geometry.Insets(15, 20, 15, 15));
                    HBox.setHgrow(content, Priority.ALWAYS);

                    HBox header = new HBox(12);
                    header.setAlignment(Pos.CENTER_LEFT);
                    
                    // Circular Icon
                    StackPane iconContainer = new StackPane();
                    iconContainer.setPrefSize(40, 40);
                    iconContainer.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 20;");
                    Label icon = new Label(isElimination ? "🚫" : "⚠️");
                    icon.setStyle("-fx-font-size: 18px;");
                    iconContainer.getChildren().add(icon);
                    
                    VBox titleInfo = new VBox(2);
                    Label title = new Label(isElimination ? "Alerte Élimination" : "Avertissement Absence");
                    title.setStyle("-fx-font-weight: 900; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
                    
                    Label time = new Label(new SimpleDateFormat("HH:mm • dd MMMM").format(n.getDateCreation()));
                    time.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
                    titleInfo.getChildren().addAll(title, time);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    if (!n.isLu()) {
                        Label newBadge = new Label("NOUVEAU");
                        newBadge.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;");
                        header.getChildren().add(newBadge);
                    }
                    
                    header.getChildren().addAll(iconContainer, titleInfo, spacer);
                    if (!n.isLu()) header.getChildren().add(header.getChildren().remove(header.getChildren().size()-2)); // Move badge before spacer

                    Label msg = new Label(n.getMessage().replace("?? ", "").replace("ATTENTION : ", ""));
                    msg.setWrapText(true);
                    msg.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-line-spacing: 1.3; -fx-font-weight: 500;");
                    
                    content.getChildren().addAll(header, msg);
                    card.getChildren().add(content);
                    
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 8 5;");
                }
            }
        });
    }

    private void loadNotifications() {
        try {
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            List<Notification> list = notificationDAO.findByUtilisateur(userId);
            notificationsList.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void markAllRead() {
        try {
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            notificationDAO.markAllAsRead(userId);
            loadNotifications();
            if (onRefresh != null) onRefresh.run();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void close() {
        ((Stage) notificationsList.getScene().getWindow()).close();
    }
}
