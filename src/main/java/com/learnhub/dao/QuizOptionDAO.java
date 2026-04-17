package com.learnhub.dao;

import com.learnhub.models.QuizOption;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizOptionDAO {

    public void addOption(QuizOption option) throws SQLException {
        String query = "INSERT INTO quiz_option (question_id, texte_option, is_correct) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, option.getQuestion_id());
            stmt.setString(2, option.getTexte_option());
            stmt.setBoolean(3, option.isIs_correct());
            
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    option.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<QuizOption> getOptionsByQuestion(int questionId) throws SQLException {
        List<QuizOption> options = new ArrayList<>();
        String query = "SELECT * FROM quiz_option WHERE question_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuizOption o = new QuizOption();
                    o.setId(rs.getInt("id"));
                    o.setQuestion_id(rs.getInt("question_id"));
                    o.setTexte_option(rs.getString("texte_option"));
                    o.setIs_correct(rs.getBoolean("is_correct"));
                    options.add(o);
                }
            }
        }
        return options;
    }
}
