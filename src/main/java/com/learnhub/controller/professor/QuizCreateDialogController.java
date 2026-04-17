package com.learnhub.controller.professor;

import com.learnhub.dao.QuizDAO;
import com.learnhub.dao.QuizOptionDAO;
import com.learnhub.dao.QuizQuestionDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Quiz;
import com.learnhub.models.QuizOption;
import com.learnhub.models.QuizQuestion;
import com.learnhub.util.DialogUtil;
import com.learnhub.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuizCreateDialogController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dateDeadline;
    @FXML private TextField txtTimeDeadline;
    @FXML private CheckBox chkVisible;
    @FXML private VBox questionsContainer;

    private Module module;
    private boolean confirmed = false;
    private int questionCounter = 0;

    private QuizDAO quizDAO = new QuizDAO();
    private QuizQuestionDAO questionDAO = new QuizQuestionDAO();
    private QuizOptionDAO optionDAO = new QuizOptionDAO();

    public void setModule(Module module) {
        this.module = module;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    public void initialize() {
        // Enlever l'erreur si l'utilisateur modifie le champ
        txtTitle.textProperty().addListener((obs, old, newVal) -> clearError(txtTitle));
        txtDescription.textProperty().addListener((obs, old, newVal) -> clearError(txtDescription));
        txtTimeDeadline.textProperty().addListener((obs, old, newVal) -> clearError(txtTimeDeadline));
        dateDeadline.valueProperty().addListener((obs, old, newVal) -> clearError(dateDeadline));

        // Add the first question automatically
        handleAddQuestion();
    }

    private void showError(Control control, String message) {
        Pane parent = (Pane) control.getParent();
        if (!control.getStyle().contains("#ef4444")) {
            control.setStyle(control.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1px;");
        }
        // Check if error label already exists
        boolean exists = parent.getChildren().stream().anyMatch(node -> node instanceof Label && node.getStyleClass().contains("error-label"));
        if (!exists) {
            Label errorLabel = new Label("• " + message);
            errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
            errorLabel.getStyleClass().add("error-label");
            parent.getChildren().add(errorLabel);
        }
    }

    private void clearError(Control c) {
        Pane parent = (Pane) c.getParent();
        if (parent != null) {
            parent.getChildren().removeIf(node -> node instanceof Label && node.getStyleClass().contains("error-label"));
            if (c.getStyle().contains("#ef4444")) {
                c.setStyle(c.getStyle().replace("; -fx-border-color: #ef4444; -fx-border-width: 1px;", ""));
            }
        }
    }

    private void clearAllErrors() {
        clearError(txtTitle);
        clearError(dateDeadline);
        clearError(txtTimeDeadline);
    }

    @FXML
    private void handleAddQuestion() {
        questionCounter++;
        VBox questionBox = createQuestionBlock(questionCounter);
        questionsContainer.getChildren().add(questionBox);
    }

    private VBox createQuestionBlock(int num) {
        VBox qBox = new VBox(10);
        qBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e5e7eb; -fx-padding: 20;");
        qBox.setUserData("QUESTION_BOX");

        Label lblTitle = new Label("Question " + num);
        lblTitle.setStyle("-fx-font-weight: 900; -fx-font-size: 13px; -fx-text-fill: #1f2937;");

        // Question Text
        TextArea txtQText = new TextArea();
        txtQText.setPromptText("Entrer la question...");
        txtQText.setPrefRowCount(2);
        txtQText.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 6;");
        txtQText.setId("txtQText");

        // Points
        HBox pointsBox = new HBox(10);
        pointsBox.setAlignment(Pos.CENTER_LEFT);
        Label lblPoints = new Label("Points:");
        lblPoints.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        TextField txtPts = new TextField("1");
        txtPts.setPrefWidth(60);
        txtPts.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 6;");
        txtPts.setId("txtPts");
        pointsBox.getChildren().addAll(lblPoints, txtPts);

        // Options Container
        VBox optionsBox = new VBox(8);
        optionsBox.setId("optionsBox");
        // Add 2 initial options
        optionsBox.getChildren().add(createOptionBlock());
        optionsBox.getChildren().add(createOptionBlock());

        // Buttons (Add option, delete question)
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAddOp = new Button("➕ Réponse");
        btnAddOp.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnAddOp.setOnAction(e -> optionsBox.getChildren().add(createOptionBlock()));

        Button btnDelQ = new Button("🗑");
        btnDelQ.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnDelQ.setOnAction(e -> {
            questionsContainer.getChildren().remove(qBox);
            updateQuestionNumbers();
        });

        buttonsBox.getChildren().addAll(btnAddOp, btnDelQ);

        qBox.getChildren().addAll(lblTitle, txtQText, pointsBox, optionsBox, buttonsBox);
        return qBox;
    }

    private void updateQuestionNumbers() {
        int idx = 1;
        for (Node node : questionsContainer.getChildren()) {
            if (node instanceof VBox && "QUESTION_BOX".equals(node.getUserData())) {
                VBox qBox = (VBox) node;
                // Le premier label est le titre "Question X"
                if (!qBox.getChildren().isEmpty() && qBox.getChildren().get(0) instanceof Label) {
                    ((Label) qBox.getChildren().get(0)).setText("Question " + idx);
                }
                idx++;
            }
        }
        questionCounter = idx - 1;
    }

    private HBox createOptionBlock() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField txtOp = new TextField();
        txtOp.setPromptText("Entrer une option...");
        txtOp.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-padding: 6;");
        txtOp.setId("txtOp");
        HBox.setHgrow(txtOp, Priority.ALWAYS);

        CheckBox chkCorrect = new CheckBox("Correcte");
        chkCorrect.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        chkCorrect.setId("chkCorrect");

        Button btnDelOp = new Button("✕");
        btnDelOp.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand;");
        btnDelOp.setOnAction(e -> {
            VBox parent = (VBox) row.getParent();
            if (parent != null && parent.getChildren().size() > 1) {
                parent.getChildren().remove(row);
            }
        });

        row.getChildren().addAll(txtOp, chkCorrect, btnDelOp);
        return row;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        clearAllErrors();
        boolean hasError = false;

        LocalDate date = dateDeadline.getValue();
        String time = txtTimeDeadline.getText();

        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty()) { showError(txtTitle, "Obligatoire"); hasError = true; }
        if (date == null) { showError(dateDeadline, "Obligatoire"); hasError = true; }
        if (time == null || time.trim().isEmpty()) { showError(txtTimeDeadline, "Obligatoire"); hasError = true; }
        else if (!time.matches("^([0-1]?\\d|2[0-3]):[0-5]\\d$")) { showError(txtTimeDeadline, "Format HH:MM"); hasError = true; }

        if (hasError) return;

        try {
            String[] timeParts = time.split(":");
            LocalDateTime deadline = date.atTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));

            // Save Quiz First
            Quiz quiz = new Quiz(0, module.getId(), SessionManager.getInstance().getCurrentUser().getId(),
                    txtTitle.getText(), txtDescription.getText(), LocalDateTime.now(), deadline, chkVisible.isSelected());
            quizDAO.addQuiz(quiz);

            // Save dynamically questions and options
            for (Node qNode : questionsContainer.getChildren()) {
                if (qNode instanceof VBox && "QUESTION_BOX".equals(qNode.getUserData())) {
                    VBox qBox = (VBox) qNode;
                    
                    TextArea txtQText = (TextArea) qBox.lookup("#txtQText");
                    TextField txtPts = (TextField) qBox.lookup("#txtPts");
                    VBox optionsBox = (VBox) qBox.lookup("#optionsBox");

                    String qText = txtQText.getText();
                    if (qText == null || qText.trim().isEmpty()) continue; // Skip empty questions

                    int points = 1;
                    try { points = Integer.parseInt(txtPts.getText().trim()); } catch (Exception ignored){}

                    QuizQuestion question = new QuizQuestion(0, quiz.getId(), qText, "QCM", points);
                    questionDAO.addQuestion(question);

                    if (optionsBox != null) {
                        for (Node oNode : optionsBox.getChildren()) {
                            if (oNode instanceof HBox) {
                                HBox row = (HBox) oNode;
                                TextField txtOp = (TextField) row.lookup("#txtOp");
                                CheckBox chkCorrect = (CheckBox) row.lookup("#chkCorrect");
                                
                                String oText = txtOp.getText();
                                if (oText != null && !oText.trim().isEmpty()) {
                                    optionDAO.addOption(new QuizOption(0, question.getId(), oText, chkCorrect.isSelected()));
                                }
                            }
                        }
                    }
                }
            }

            confirmed = true;
            DialogUtil.showSuccessMessage("Succès", "Le quiz a été créé avec succès.");
            closeDialog();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }
}
