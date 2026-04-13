package com.learnhub.medical.repository;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CreneauRepository {

    public List<Creneau> findAll() throws SQLException {
        List<Creneau> list = new ArrayList<>();
        String sql = "SELECT * FROM creneau ORDER BY jour ASC, heure ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Creneau findById(int id) throws SQLException {
        String sql = "SELECT * FROM creneau WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public void save(Creneau c) throws SQLException {
        String sql = "INSERT INTO creneau (jour, heure, recurrence, disponibilite) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getJour());
            ps.setTime(2, Time.valueOf(c.getHeure()));
            ps.setString(3, c.getRecurrence());
            ps.setBoolean(4, c.isDisponibilite());
            ps.executeUpdate();
        }
    }

    public void update(Creneau c) throws SQLException {
        String sql = "UPDATE creneau SET jour=?, heure=?, recurrence=?, disponibilite=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getJour());
            ps.setTime(2, Time.valueOf(c.getHeure()));
            ps.setString(3, c.getRecurrence());
            ps.setBoolean(4, c.isDisponibilite());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM creneau WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void updateAvailability(int id, boolean status) throws SQLException {
        String sql = "UPDATE creneau SET disponibilite=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Unicité : Vérifie si un créneau existe déjà à cette date et heure */
    public boolean exists(String jour, LocalTime heure, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM creneau WHERE jour=? AND heure=? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jour);
            ps.setTime(2, Time.valueOf(heure));
            ps.setInt(3, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    private Creneau map(ResultSet rs) throws SQLException {
        return new Creneau(
            rs.getInt("id"),
            rs.getString("jour"),
            rs.getTime("heure").toLocalTime(),
            rs.getString("recurrence"),
            rs.getBoolean("disponibilite")
        );
    }
}
