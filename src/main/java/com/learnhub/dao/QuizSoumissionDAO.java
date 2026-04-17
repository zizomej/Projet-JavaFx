package com.learnhub.dao;

import com.learnhub.models.QuizSoumission;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class QuizSoumissionDAO {

    public void addSoumission(QuizSoumission soumission) throws SQLException {
        String query = "INSERT INTO quiz_soumission (quiz_id, etudiant_id, note_obtenue, date_soumission) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, soumission.getQuiz_id());
            stmt.setInt(2, soumission.getEtudiant_id());
            stmt.setInt(3, soumission.getNote_obtenue());
            stmt.setTimestamp(4, Timestamp.valueOf(soumission.getDate_soumission() != null ? soumission.getDate_soumission() : LocalDateTime.now()));
            
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    soumission.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public QuizSoumission getSoumissionByQuizAndEtudiant(int quizId, int etudiantId) throws SQLException {
        String query = "SELECT * FROM quiz_soumission WHERE quiz_id = ? AND etudiant_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, etudiantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    QuizSoumission s = new QuizSoumission();
                    s.setId(rs.getInt("id"));
                    s.setQuiz_id(rs.getInt("quiz_id"));
                    s.setEtudiant_id(rs.getInt("etudiant_id"));
                    s.setNote_obtenue(rs.getInt("note_obtenue"));
                    Timestamp ts = rs.getTimestamp("date_soumission");
                    if(ts != null) s.setDate_soumission(ts.toLocalDateTime());
                    return s;
                }
            }
        }
        return null; // Pas encore de soumission
    }
}
