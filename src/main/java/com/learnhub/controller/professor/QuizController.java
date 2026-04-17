package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.QuizDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Quiz;
import com.learnhub.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class QuizController {

    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private TableView<Quiz> quizTable;
    @FXML private TableColumn<Quiz, String> colTitre;
    @FXML private TableColumn<Quiz, LocalDateTime> colDeadline;
    @FXML private TableColumn<Quiz, Boolean> colVisible;
    @FXML private TableColumn<Quiz, Void> colActions;

    private QuizDAO quizDAO;
    private ModuleDAO moduleDAO;
    private ObservableList<Quiz> quizzesList;

    @FXML
    public void initialize() {
        quizDAO = new QuizDAO();
        moduleDAO = new ModuleDAO();
        quizzesList = FXCollections.observableArrayList();

        setupTable();
        loadModules();

        moduleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadQuizzes(newVal.getId());
            }
        });
    }

    private void setupTable() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        colVisible.setCellFactory(tc -> new TableCell<Quiz, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    boolean isVisible = checkBox.isSelected();
                    try {
                        quizDAO.updateQuizVisibility(quiz.getId(), isVisible);
                        quiz.setIs_visible(isVisible);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        checkBox.setSelected(!isVisible); // revert
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(quiz.isIs_visible());
                    setGraphic(checkBox);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<Quiz, Void>() {
            private final Button btnDelete = new Button("🗑");
            {
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 14px;");
                btnDelete.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    try {
                        quizDAO.deleteQuiz(quiz.getId());
                        quizzesList.remove(quiz);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, btnDelete);
                    setGraphic(box);
                }
            }
        });

        quizTable.setItems(quizzesList);
    }

    private void loadModules() {
        try {
            int profId = SessionManager.getInstance().getCurrentUser().getId();
            List<Module> modules = moduleDAO.findByProfesseur(profId);
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
        try {
            quizzesList.setAll(quizDAO.getQuizzesByModule(moduleId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddQuiz(ActionEvent event) {
        Module selectedModule = moduleComboBox.getValue();
        if (selectedModule == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un module d'abord.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professor/quiz_create_dialog.fxml"));
            Parent root = loader.load();
            QuizCreateDialogController dialogController = loader.getController();
            dialogController.setModule(selectedModule);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Créer un nouveau Quiz");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            if (dialogController.isConfirmed()) {
                loadQuizzes(selectedModule.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard() { navigate("/fxml/professor/dashboard.fxml"); }
    @FXML
    private void goModules() { navigate("/fxml/professor/modules.fxml"); }
    @FXML
    private void goNotes() { navigate("/fxml/professor/notes.fxml"); }
    @FXML
    private void goSeances() { navigate("/fxml/professor/seances.fxml"); }

    private void navigate(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            moduleComboBox.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
