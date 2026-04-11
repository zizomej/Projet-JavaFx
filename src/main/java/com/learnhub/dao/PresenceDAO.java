package com.learnhub.dao;

import com.learnhub.models.Presence;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresenceDAO {

    public List<Presence> findByEtudiant(int etudiantId) throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT p.*, s.date_seance, m.intitule as module_nom " +
                "FROM presence p " +
                "LEFT JOIN seance s ON p.seance_id = s.id " +
                "LEFT JOIN module m ON s.module_id = m.id " +
                "WHERE p.etudiant_id = ? " +
                "ORDER BY s.date_seance DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Presence p = new Presence();
                p.setId(rs.getInt("id"));
                p.setStatut(rs.getString("statut"));
                p.setSeanceId(rs.getInt("seance_id"));
                p.setEtudiantId(rs.getInt("etudiant_id"));
                p.setDateSeance(rs.getString("date_seance"));
                p.setModuleNom(rs.getString("module_nom"));
                list.add(p);
            }
        }
        return list;
    }

    public int calculatePresencePercentage(int etudiantId) throws SQLException {
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN statut = 'present' THEN 1 ELSE 0 END) as presents " +
                "FROM presence WHERE etudiant_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int presents = rs.getInt("presents");
                return total > 0 ? (presents * 100 / total) : 100;
            }
        }
        return 100;
    }
}
