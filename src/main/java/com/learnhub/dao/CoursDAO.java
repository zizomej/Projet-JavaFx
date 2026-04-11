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
        m.setTitre(rs.getString("titre"));
        m.setDescription(rs.getString("description"));
        m.setMatiere(rs.getString("matiere"));
        m.setProfesseurId(rs.getInt("professeur_id"));
        m.setProfesseurNom(rs.getString("professeur_nom"));
        m.setNiveau(rs.getString("niveau"));
        m.setFiliere(rs.getString("filiere"));
        m.setActif(rs.getBoolean("actif"));
        return m;
    }

    public List<Cours> findAll() throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = """
            SELECT m.*, CONCAT(u.prenom,' ',u.nom) as professeur_nom
            FROM module m
            LEFT JOIN utilisateur u ON m.professeur_id = u.id
            ORDER BY m.titre
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Cours> findByProfesseur(int professeurId) throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = """
            SELECT m.*, CONCAT(u.prenom,' ',u.nom) as professeur_nom
            FROM module m
            LEFT JOIN utilisateur u ON m.professeur_id = u.id
            WHERE m.professeur_id = ?
            ORDER BY m.titre
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, professeurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Cours findById(int id) throws SQLException {
        String sql = """
            SELECT m.*, CONCAT(u.prenom,' ',u.nom) as professeur_nom
            FROM module m
            LEFT JOIN utilisateur u ON m.professeur_id = u.id
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
        String sql = "INSERT INTO module (titre, description, matiere, professeur_id, niveau, filiere, actif) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTitre());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getMatiere());
            ps.setInt(4, m.getProfesseurId());
            ps.setString(5, m.getNiveau());
            ps.setString(6, m.getFiliere());
            ps.setBoolean(7, m.isActif());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) m.setId(keys.getInt(1));
        }
    }

    public void update(Cours m) throws SQLException {
        String sql = "UPDATE module SET titre=?, description=?, matiere=?, professeur_id=?, niveau=?, filiere=?, actif=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, m.getTitre());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getMatiere());
            ps.setInt(4, m.getProfesseurId());
            ps.setString(5, m.getNiveau());
            ps.setString(6, m.getFiliere());
            ps.setBoolean(7, m.isActif());
            ps.setInt(8, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM module WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
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
