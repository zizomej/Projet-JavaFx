package com.learnhub.dao;

import com.learnhub.models.Notification;
import com.learnhub.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void insert(Notification n) throws SQLException {
        String sql = "INSERT INTO notification (utilisateur_id, message, type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, n.getUtilisateurId());
            ps.setString(2, n.getMessage());
            ps.setString(3, n.getType());
            ps.executeUpdate();
        }
    }

    public List<Notification> findByUtilisateur(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE utilisateur_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setUtilisateurId(rs.getInt("utilisateur_id"));
                n.setMessage(rs.getString("message"));
                n.setType(rs.getString("type"));
                n.setDateCreation(rs.getTimestamp("date_creation"));
                n.setLu(rs.getBoolean("lu"));
                list.add(n);
            }
        }
        return list;
    }

    public int countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notification WHERE utilisateur_id = ? AND lu = FALSE";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notification SET lu = TRUE WHERE utilisateur_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
