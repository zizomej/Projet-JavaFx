package com.learnhub.dao;

import com.learnhub.models.Quiz;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    
    public void addQuiz(Quiz quiz) throws SQLException {
        String query = "INSERT INTO quiz (module_id, professeur_id, titre, description, deadline, is_visible) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, quiz.getModule_id());
            stmt.setInt(2, quiz.getProfesseur_id());
            stmt.setString(3, quiz.getTitre());
            stmt.setString(4, quiz.getDescription());
            stmt.setTimestamp(5, Timestamp.valueOf(quiz.getDeadline()));
            stmt.setBoolean(6, quiz.isIs_visible());
            
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    quiz.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateQuizVisibility(int quizId, boolean isVisible) throws SQLException {
        String query = "UPDATE quiz SET is_visible = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isVisible);
            stmt.setInt(2, quizId);
            stmt.executeUpdate();
        }
    }

    public List<Quiz> getQuizzesByModule(int moduleId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz WHERE module_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, moduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSetToQuiz(rs));
                }
            }
        }
        return quizzes;
    }

    public List<Quiz> getVisibleQuizzesByModule(int moduleId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz WHERE module_id = ? AND is_visible = TRUE";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, moduleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSetToQuiz(rs));
                }
            }
        }
        return quizzes;
    }

    public void deleteQuiz(int quizId) throws SQLException {
        String query = "DELETE FROM quiz WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quizId);
            stmt.executeUpdate();
        }
    }

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id"));
        quiz.setModule_id(rs.getInt("module_id"));
        quiz.setProfesseur_id(rs.getInt("professeur_id"));
        quiz.setTitre(rs.getString("titre"));
        quiz.setDescription(rs.getString("description"));
        Timestamp creation = rs.getTimestamp("date_creation");
        if (creation != null) quiz.setDate_creation(creation.toLocalDateTime());
        Timestamp deadline = rs.getTimestamp("deadline");
        if (deadline != null) quiz.setDeadline(deadline.toLocalDateTime());
        quiz.setIs_visible(rs.getBoolean("is_visible"));
        return quiz;
    }
}
