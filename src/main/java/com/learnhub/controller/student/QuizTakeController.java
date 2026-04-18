package com.learnhub.controller.student;

import com.learnhub.dao.QuizOptionDAO;
import com.learnhub.dao.QuizQuestionDAO;
import com.learnhub.dao.QuizSoumissionDAO;
import com.learnhub.models.Quiz;
import com.learnhub.models.QuizOption;
import com.learnhub.models.QuizQuestion;
import com.learnhub.models.QuizSoumission;
import com.learnhub.util.DialogUtil;
import com.learnhub.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizTakeController {

    @FXML private Label lblTitre;
    @FXML private Label lblChronometer;
    @FXML private Label lblProgress;
    @FXML private VBox mainContent;
    @FXML private Label lblQuestionHeader;
    @FXML private Label lblPoints;
    @FXML private Label lblQuestionText;
    @FXML private VBox optionsContainer;
    
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnSubmit;

    private Quiz quiz;
    private QuizQuestionDAO questionDAO = new QuizQuestionDAO();
    private QuizOptionDAO optionDAO = new QuizOptionDAO();
    private QuizSoumissionDAO soumissionDAO = new QuizSoumissionDAO();

    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private boolean isReviewMode = false;


    // questionId -> selected Option Id
    private Map<Integer, Integer> userResponses = new HashMap<>();
    private Map<Integer, Integer> correctOptionsMap = new HashMap<>(); // QuestionId -> OptionId correct

    private Timeline timeline;
    private int elapsedSeconds = 0;

    public void initQuiz(Quiz q) {
        this.quiz = q;
        lblTitre.setText("Quiz - " + q.getTitre());
        loadQuestions();
    }

    private void loadQuestions() {
        try {
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            QuizSoumission exist = soumissionDAO.getSoumissionByQuizAndEtudiant(quiz.getId(), userId);
            if (exist != null) {
                showSubmittedState(exist.getNote_obtenue());
                return;
            }

            questions = questionDAO.getQuestionsByQuiz(quiz.getId());
            if (questions.isEmpty()) {
                lblQuestionText.setText("Ce quiz ne contient aucune question.");
                optionsContainer.getChildren().clear();
                btnNext.setVisible(false);
                btnPrev.setVisible(false);
                btnSubmit.setVisible(false);
                return;
            }

            // Prep correctly answers map
            for (QuizQuestion q : questions) {
                List<QuizOption> opts = optionDAO.getOptionsByQuestion(q.getId());
                for(QuizOption o : opts) {
                    if (o.isIs_correct()) correctOptionsMap.put(q.getId(), o.getId());
                }
            }

            currentIndex = 0;
            displayCurrentQuestion();
            startChronometer();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showSubmittedState(int note) {
        lblProgress.setText("Terminé");
        lblQuestionText.setText("✅ Vous avez déjà passé ce quiz.");
        lblQuestionText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        lblQuestionHeader.setText("");
        lblPoints.setText("");
        
        Label noteLabel = new Label("Votre note : " + note + " points");
        noteLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #374151;");
        
        optionsContainer.getChildren().clear();
        optionsContainer.getChildren().add(noteLabel);
        
        btnPrev.setVisible(false);
        btnNext.setVisible(false);
        btnSubmit.setVisible(false);
    }

    private void startChronometer() {
        if (timeline != null) timeline.stop();
        elapsedSeconds = 0;
        lblChronometer.setText("00:00");
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            elapsedSeconds++;
            int min = elapsedSeconds / 60;
            int sec = elapsedSeconds % 60;
            lblChronometer.setText(String.format("%02d:%02d", min, sec));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void displayCurrentQuestion() {
        if (questions == null || questions.isEmpty()) return;

        QuizQuestion currentQ = questions.get(currentIndex);
        lblQuestionHeader.setText("Question " + (currentIndex + 1));
        lblPoints.setText(currentQ.getPoints() + (currentQ.getPoints() > 1 ? " points" : " point"));
        lblQuestionText.setText(currentQ.getTexte_question());
        lblProgress.setText((currentIndex + 1) + " / " + questions.size());

        optionsContainer.getChildren().clear();

        try {
            List<QuizOption> options = optionDAO.getOptionsByQuestion(currentQ.getId());
            ToggleGroup group = new ToggleGroup();

            for (QuizOption opt : options) {
                HBox optBox = new HBox(15);
                optBox.setAlignment(Pos.CENTER_LEFT);
                String baseStyle = "-fx-border-color: #e5e7eb; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 12 20; -fx-background-color: #f3f4f6;";
                optBox.setStyle(baseStyle);

                RadioButton rb = new RadioButton(opt.getTexte_option());
                rb.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
                rb.setToggleGroup(group);
                rb.setUserData(opt.getId());

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label statusLabel = new Label();
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
                statusLabel.setVisible(false);

                // Initial Selection
                if (userResponses.containsKey(currentQ.getId()) && userResponses.get(currentQ.getId()) == opt.getId()) {
                    rb.setSelected(true);
                    if (!isReviewMode) {
                        optBox.setStyle("-fx-background-color: white; -fx-border-color: #9ca3af; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 12 20;");
                    }
                }

                if (isReviewMode) {
                    rb.setDisable(true);
                    boolean isCorrectChoice = opt.isIs_correct();
                    boolean userSelectedThis = rb.isSelected();

                    // Blackboard styling
                    if (isCorrectChoice) {
                        statusLabel.setText("Correct answer");
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #10b981;");
                        statusLabel.setVisible(true);
                        if (userSelectedThis) {
                            optBox.setStyle("-fx-background-color: #ecfdf5; -fx-border-color: #10b981; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 12 20;");
                        } else {
                            optBox.setStyle("-fx-background-color: #f3f4f6; -fx-border-color: #10b981; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 12 20;");
                        }
                    } else if (userSelectedThis && !isCorrectChoice) {
                        statusLabel.setText("Incorrect");
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #ef4444;");
                        statusLabel.setVisible(true);
                        optBox.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #ef4444; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 12 20;");
                    }

                } else {
                    // Interaction only when not in review mode
                    rb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            optBox.setStyle("-fx-background-color: white; -fx-border-color: #9ca3af; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 12 20;");
                            userResponses.put(currentQ.getId(), opt.getId());
                        } else {
                            optBox.setStyle(baseStyle);
                        }
                    });
                    optBox.setOnMouseClicked(e -> rb.setSelected(true));
                    optBox.setCursor(javafx.scene.Cursor.HAND);
                }

                optBox.getChildren().addAll(rb, spacer, statusLabel);
                optionsContainer.getChildren().add(optBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Updating buttons
        btnPrev.setVisible(currentIndex > 0);
        
        if (currentIndex == questions.size() - 1) {
            btnNext.setVisible(false);
            btnSubmit.setVisible(true);
        } else {
            btnNext.setVisible(true);
            btnSubmit.setVisible(false);
        }
    }

    @FXML
    private void handleNext() {
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            displayCurrentQuestion();
        }
    }

    @FXML
    private void handlePrev() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentQuestion();
        }
    }

    @FXML
    private void handleSubmit() {
        if (isReviewMode) {
            closeDialog();
            return;
        }

        if (userResponses.isEmpty()) { 
            closeDialog();
            return;
        }

        int score = 0;
        for (QuizQuestion q : questions) {
            Integer selectedOpt = userResponses.get(q.getId());
            Integer correctOpt = correctOptionsMap.get(q.getId());
            if (selectedOpt != null && selectedOpt.equals(correctOpt)) {
                score += q.getPoints();
            }
        }

        try {
            QuizSoumission sub = new QuizSoumission(0, quiz.getId(), SessionManager.getInstance().getCurrentUser().getId(), score, LocalDateTime.now());
            soumissionDAO.addSoumission(sub);
            
            if (timeline != null) timeline.stop();
            lblChronometer.setText("Terminé");

            DialogUtil.showSuccessMessage("Succès", "Quiz soumis avec succès !\nVous avez obtenu " + score + " points en " + elapsedSeconds + " secondes.\nVous pouvez consulter vos erreurs maintenant.");

            // Enter review mode
            isReviewMode = true;
            currentIndex = 0; // go back to start to review
            displayCurrentQuestion();
            btnSubmit.setVisible(false);
            btnNext.setVisible(questions.size() > 1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        if (timeline != null) timeline.stop();
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}
