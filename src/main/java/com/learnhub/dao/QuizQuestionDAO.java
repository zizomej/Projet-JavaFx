package com.learnhub.dao;

import com.learnhub.models.QuizQuestion;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionDAO {
    
    public void addQuestion(QuizQuestion question) throws SQLException {
        String query = "INSERT INTO quiz_question (quiz_id, texte_question, type_question, points) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, question.getQuiz_id());
            stmt.setString(2, question.getTexte_question());
            stmt.setString(3, question.getType_question());
            stmt.setInt(4, question.getPoints());
            
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<QuizQuestion> getQuestionsByQuiz(int quizId) throws SQLException {
        List<QuizQuestion> questions = new ArrayList<>();
        String query = "SELECT * FROM quiz_question WHERE quiz_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuizQuestion q = new QuizQuestion();
                    q.setId(rs.getInt("id"));
                    q.setQuiz_id(rs.getInt("quiz_id"));
                    q.setTexte_question(rs.getString("texte_question"));
                    q.setType_question(rs.getString("type_question"));
                    q.setPoints(rs.getInt("points"));
                    questions.add(q);
                }
            }
        }
        return questions;
    }
}
