package com.learnhub.dao;

import com.learnhub.models.Seance;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {

    private Seance mapRow(ResultSet rs) throws SQLException {
        Seance s = new Seance();
        s.setId(rs.getInt("id"));
        s.setModuleId(rs.getInt("module_id"));
        s.setModuleTitre(rs.getString("module_titre"));
        s.setDate(rs.getString("date"));
        s.setHeureDebut(rs.getString("heure_debut"));
        s.setHeureFin(rs.getString("heure_fin"));
        s.setSalle(rs.getString("salle"));
        s.setType(rs.getString("type"));
        s.setDescription(rs.getString("description"));
        return s;
    }

    public List<Seance> findAll() throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = "SELECT s.*, m.titre as module_titre FROM seance s LEFT JOIN module m ON s.module_id=m.id ORDER BY s.date DESC, s.heure_debut";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Seance> findByModule(int moduleId) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = "SELECT s.*, m.titre as module_titre FROM seance s LEFT JOIN module m ON s.module_id=m.id WHERE s.module_id=? ORDER BY s.date DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Seance> findByProfesseur(int professeurId) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = "SELECT s.*, m.titre as module_titre FROM seance s JOIN module m ON s.module_id=m.id WHERE m.professeur_id=? ORDER BY s.date DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, professeurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Seance findById(int id) throws SQLException {
        String sql = "SELECT s.*, m.titre as module_titre FROM seance s LEFT JOIN module m ON s.module_id=m.id WHERE s.id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void insert(Seance s) throws SQLException {
        String sql = "INSERT INTO seance (module_id, date, heure_debut, heure_fin, salle, type, description) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getModuleId());
            ps.setString(2, s.getDate());
            ps.setString(3, s.getHeureDebut());
            ps.setString(4, s.getHeureFin());
            ps.setString(5, s.getSalle());
            ps.setString(6, s.getType());
            ps.setString(7, s.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
        }
    }

    public void update(Seance s) throws SQLException {
        String sql = "UPDATE seance SET module_id=?, date=?, heure_debut=?, heure_fin=?, salle=?, type=?, description=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, s.getModuleId());
            ps.setString(2, s.getDate());
            ps.setString(3, s.getHeureDebut());
            ps.setString(4, s.getHeureFin());
            ps.setString(5, s.getSalle());
            ps.setString(6, s.getType());
            ps.setString(7, s.getDescription());
            ps.setInt(8, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM seance WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM seance";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
