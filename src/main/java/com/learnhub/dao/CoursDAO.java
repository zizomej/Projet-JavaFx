package com.learnhub.dao;

import com.learnhub.models.Cours;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    private Cours mapRow(ResultSet rs) throws SQLException {
        Cours m = new Cours();
        m.setId(rs.getInt("id"));
        m.setCode(rs.getString("code"));
        m.setIntitule(rs.getString("intitule"));
        m.setSemestre(rs.getInt("semestre"));
        m.setCredits(rs.getInt("credits"));
        m.setFiliereId(rs.getInt("filiere_id"));
        m.setResponsableId(rs.getInt("responsable_id"));
        try { m.setResponsableNom(rs.getString("responsable_nom")); } catch (SQLException ignored) {}
        return m;
    }

    public List<Cours> findAll() throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = """
            SELECT m.*, CONCAT(u.prenom,' ',u.nom) AS responsable_nom
            FROM module m
            LEFT JOIN utilisateur u ON m.responsable_id = u.id
            ORDER BY m.intitule
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Cours> findByProfesseur(int responsableId) throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = """
            SELECT m.*, CONCAT(u.prenom,' ',u.nom) AS responsable_nom
            FROM module m
            LEFT JOIN utilisateur u ON m.responsable_id = u.id
            WHERE m.responsable_id = ?
            ORDER BY m.intitule
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, responsableId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Cours findById(int id) throws SQLException {
        String sql = """
            SELECT m.*, CONCAT(u.prenom,' ',u.nom) AS responsable_nom
            FROM module m
            LEFT JOIN utilisateur u ON m.responsable_id = u.id
            WHERE m.id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void insert(Cours m) throws SQLException {
        String sql = "INSERT INTO module (code, intitule, semestre, credits, filiere_id, responsable_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getIntitule());
            ps.setInt(3, m.getSemestre());
            ps.setInt(4, m.getCredits());
            ps.setInt(5, m.getFiliereId());
            ps.setInt(6, m.getResponsableId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) m.setId(keys.getInt(1));
        }
    }

    public void update(Cours m) throws SQLException {
        String sql = "UPDATE module SET code=?, intitule=?, semestre=?, credits=?, filiere_id=?, responsable_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getIntitule());
            ps.setInt(3, m.getSemestre());
            ps.setInt(4, m.getCredits());
            ps.setInt(5, m.getFiliereId());
            ps.setInt(6, m.getResponsableId());
            ps.setInt(7, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement("DELETE FROM module WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM module")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
