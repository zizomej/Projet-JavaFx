package com.learnhub.dao;

import com.learnhub.models.Filiere;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FiliereDAO {

    private Filiere mapRow(ResultSet rs) throws SQLException {
        Filiere f = new Filiere();
        f.setId(rs.getInt("id"));
        f.setCode(rs.getString("code"));
        f.setNom(rs.getString("nom"));
        f.setNiveau(rs.getString("niveau"));
        f.setDureeAnnees(rs.getInt("duree_annees"));
        f.setCapaciteMax(rs.getInt("capacite_max"));
        f.setUniversiteId(rs.getInt("universite_id"));
        f.setResponsableId(rs.getInt("responsable_id"));
        try { f.setResponsableNom(rs.getString("responsable_nom")); } catch (SQLException ignored) {}
        return f;
    }

    public List<Filiere> findAll() throws SQLException {
        List<Filiere> list = new ArrayList<>();
        String sql = """
            SELECT f.*, CONCAT(u.prenom,' ',u.nom) AS responsable_nom
            FROM filiere f
            LEFT JOIN utilisateur u ON f.responsable_id = u.id
            ORDER BY f.nom
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Filiere findById(int id) throws SQLException {
        String sql = """
            SELECT f.*, CONCAT(u.prenom,' ',u.nom) AS responsable_nom
            FROM filiere f
            LEFT JOIN utilisateur u ON f.responsable_id = u.id
            WHERE f.id=?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void insert(Filiere f) throws SQLException {
        String sql = "INSERT INTO filiere (code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getCode() != null ? f.getCode() : "");
            ps.setString(2, f.getNom());
            ps.setString(3, f.getNiveau());
            ps.setInt(4, f.getDureeAnnees());
            ps.setInt(5, f.getCapaciteMax());
            ps.setInt(6, f.getUniversiteId() > 0 ? f.getUniversiteId() : 1);
            if (f.getResponsableId() > 0) ps.setInt(7, f.getResponsableId()); else ps.setNull(7, Types.INTEGER);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) f.setId(keys.getInt(1));
        }
    }

    public void update(Filiere f) throws SQLException {
        String sql = "UPDATE filiere SET code=?, nom=?, niveau=?, duree_annees=?, capacite_max=?, universite_id=?, responsable_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, f.getCode() != null ? f.getCode() : "");
            ps.setString(2, f.getNom());
            ps.setString(3, f.getNiveau());
            ps.setInt(4, f.getDureeAnnees());
            ps.setInt(5, f.getCapaciteMax());
            ps.setInt(6, f.getUniversiteId() > 0 ? f.getUniversiteId() : 1);
            if (f.getResponsableId() > 0) ps.setInt(7, f.getResponsableId()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, f.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement("DELETE FROM filiere WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM filiere")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
