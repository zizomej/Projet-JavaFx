package com.learnhub.dao;

import com.learnhub.models.Participation;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;

public class ParticipationDAO {

    public void insert(Participation p) throws SQLException {
        String sql = "INSERT INTO inscription_evenement (nom, email, telephone, evenement_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getTelephone());
            ps.setInt(4, p.getEvenementId());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }
}
