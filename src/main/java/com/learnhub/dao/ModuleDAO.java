package com.learnhub.dao;

import com.learnhub.models.Module;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleDAO {

    public List<Module> findAll() throws SQLException {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM module";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modules.add(new Module(
                    rs.getInt("id"), rs.getString("code"), rs.getString("intitule"),
                    rs.getInt("semestre"), rs.getInt("credits"),
                    rs.getInt("filiere_id"), rs.getInt("responsable_id")
                ));
            }
        }
        return modules;
    }

    public List<Module> findByProfesseur(int profId) throws SQLException {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM module WHERE responsable_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    modules.add(new Module(
                        rs.getInt("id"), rs.getString("code"), rs.getString("intitule"),
                        rs.getInt("semestre"), rs.getInt("credits"),
                        rs.getInt("filiere_id"), rs.getInt("responsable_id")
                    ));
                }
            }
        }
        return modules;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM module";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    public int countByProfesseur(int profId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM module WHERE responsable_id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        }
        return 0;
    }

    public void add(Module m) throws SQLException {
        String sql = "INSERT INTO module (code, intitule, semestre, credits, filiere_id, responsable_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getIntitule());
            ps.setInt(3, m.getSemestre());
            ps.setInt(4, m.getCredits());
            ps.setInt(5, m.getFiliere_id());
            ps.setInt(6, m.getResponsable_id());
            ps.executeUpdate();
        }
    }

    public void update(Module m) throws SQLException {
        String sql = "UPDATE module SET code=?, intitule=?, semestre=?, credits=?, filiere_id=?, responsable_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getIntitule());
            ps.setInt(3, m.getSemestre());
            ps.setInt(4, m.getCredits());
            ps.setInt(5, m.getFiliere_id());
            ps.setInt(6, m.getResponsable_id());
            ps.setInt(7, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM module WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
