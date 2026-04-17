package com.learnhub.controller.student;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.QuizDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Quiz;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentQuizzesController {

    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private VBox quizzesContainer;

    private QuizDAO quizDAO = new QuizDAO();
    private ModuleDAO moduleDAO = new ModuleDAO();

    @FXML
    public void initialize() {
        loadModules();

        moduleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadQuizzes(newVal.getId());
            }
        });
    }

    private void loadModules() {
        try {
            // Un étudiant devrait techniquement ne voir que les modules de sa filière,
            // Pour simplifier selon l'existant, on va utiliser findAll() ou les modules disponibles.
            List<Module> modules = moduleDAO.findAll();
            moduleComboBox.setItems(FXCollections.observableArrayList(modules));
            moduleComboBox.setCellFactory(lv -> new ListCell<Module>() {
                @Override
                protected void updateItem(Module item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getIntitule());
                }
            });
            moduleComboBox.setButtonCell(new ListCell<Module>() {
                @Override
                protected void updateItem(Module item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getIntitule());
                }
            });
            if (!modules.isEmpty()) {
                moduleComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadQuizzes(int moduleId) {
        quizzesContainer.getChildren().clear();
        try {
            List<Quiz> quizzes = quizDAO.getVisibleQuizzesByModule(moduleId);
            
            if (quizzes.isEmpty()) {
                Label noQuizMsg = new Label("Aucun test interactif prévu pour l'instant.");
                noQuizMsg.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
                quizzesContainer.getChildren().add(noQuizMsg);
                return;
            }

            for (Quiz q : quizzes) {
                quizzesContainer.getChildren().add(createQuizCard(q));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createQuizCard(Quiz q) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);");

        Label icon = new Label("📝");
        icon.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-padding: 10 12; -fx-background-radius: 10; -fx-font-size: 20px;");

        VBox textPart = new VBox(5);
        Label title = new Label(q.getTitre());
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 16px; -fx-text-fill: #111827;");
        
        boolean isActive = q.getDeadline().isAfter(LocalDateTime.now());
        
        Label deadlineBadge = new Label("Deadline: " + q.getDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        deadlineBadge.setStyle(isActive ? "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;" 
                                        : "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");

        textPart.getChildren().addAll(title, deadlineBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTake = new Button(isActive ? "▶ Passer le quiz" : "❌ Indisponible (Expiré)");
        if (isActive) {
            btnTake.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
            btnTake.setOnAction(e -> handleTakeQuiz(q));
        } else {
            btnTake.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;");
            btnTake.setDisable(true);
        }

        card.getChildren().addAll(icon, textPart, spacer, btnTake);
        return card;
    }

    private void handleTakeQuiz(Quiz q) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/quiz_take.fxml"));
            Parent root = loader.load();
            QuizTakeController controller = loader.getController();
            controller.initQuiz(q);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Passage du Quiz");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml"); }
    @FXML private void goModules() { navigate("/fxml/student/modules.fxml"); }
    @FXML private void goNotes() { navigate("/fxml/student/notes.fxml"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml"); }
    @FXML private void goStages() { navigate("/fxml/student/stages.fxml"); }

    private void navigate(String fxml) {
        try {
            Stage stage = (Stage) quizzesContainer.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, "");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) quizzesContainer.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}
