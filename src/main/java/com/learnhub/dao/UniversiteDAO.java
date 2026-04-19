package com.learnhub.dao;

import com.learnhub.models.Universite;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UniversiteDAO {

    private Universite mapRow(ResultSet rs) throws SQLException {
        Universite u = new Universite();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setType(rs.getString("type"));
        u.setVille(rs.getString("ville"));
        u.setAdresse(rs.getString("adresse"));
        u.setTelephone(rs.getString("telephone"));
        u.setEmail(rs.getString("email"));
        return u;
    }

    public List<Universite> findAll() throws SQLException {
        List<Universite> list = new ArrayList<>();
        String sql = "SELECT * FROM universite ORDER BY nom";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Universite findById(int id) throws SQLException {
        String sql = "SELECT * FROM universite WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM universite";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByType(String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM universite WHERE type = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void insert(Universite u) throws SQLException {
        String sql = "INSERT INTO universite (nom, type, ville, adresse, telephone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getType());
            ps.setString(3, u.getVille());
            ps.setString(4, u.getAdresse());
            ps.setString(5, u.getTelephone());
            ps.setString(6, u.getEmail());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) u.setId(keys.getInt(1));
        }
    }

    public void update(Universite u) throws SQLException {
        String sql = "UPDATE universite SET nom=?, type=?, ville=?, adresse=?, telephone=?, email=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getType());
            ps.setString(3, u.getVille());
            ps.setString(4, u.getAdresse());
            ps.setString(5, u.getTelephone());
            ps.setString(6, u.getEmail());
            ps.setInt(7, u.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM universite WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
