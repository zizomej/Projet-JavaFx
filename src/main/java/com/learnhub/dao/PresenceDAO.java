package com.learnhub.dao;

import com.learnhub.models.Presence;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresenceDAO {

    public List<Presence> findBySeance(int seanceId) throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = "SELECT p.*, u.nom, u.prenom FROM presence p " +
                     "JOIN utilisateur u ON p.etudiant_id = u.id " +
                     "WHERE p.seance_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, seanceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Presence p = new Presence();
                p.setId(rs.getInt("id"));
                p.setStatut(rs.getString("statut"));
                p.setSeanceId(rs.getInt("seance_id"));
                p.setEtudiantId(rs.getInt("etudiant_id"));
                p.setEtudiantNom(rs.getString("nom") + " " + rs.getString("prenom"));
                list.add(p);
            }
        }
        return list;
    }

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

    public void save(Presence p) throws SQLException {
        String sql;
        if (p.getId() > 0) {
            sql = "UPDATE presence SET statut = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO presence (statut, seance_id, etudiant_id) VALUES (?, ?, ?)";
        }
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getStatut());
            if (p.getId() > 0) {
                ps.setInt(2, p.getId());
            } else {
                ps.setInt(2, p.getSeanceId());
                ps.setInt(3, p.getEtudiantId());
            }
            ps.executeUpdate();
            if (p.getId() <= 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    public int getAbsenceCountByModule(int etudiantId, int moduleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM presence p " +
                "JOIN seance s ON p.seance_id = s.id " +
                "WHERE p.etudiant_id = ? AND s.module_id = ? AND p.statut = 'absent'";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setInt(2, moduleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<Utilisateur> findStudentsByModule(int moduleId) throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        // Note: On récupère les étudiants actifs. 
        // L'idéal serait de filtrer par ceux inscrits au module, mais ici on prend tous les étudiants par défaut.
        String sql = "SELECT id, nom, prenom FROM utilisateur WHERE role IN ('ROLE_ETUDIANT', 'student', 'Etudiant') AND statut = 'actif'";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                list.add(u);
            }
        }
        return list;
    }
}
