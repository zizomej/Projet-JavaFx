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
        f.setDescription("");
        f.setResponsable("");
        f.setDuree(rs.getInt("duree_annees"));
        try {
            f.setVideoUrl(rs.getString("video_url"));
        } catch (SQLException e) {
            // Column might be missing, ignore
            f.setVideoUrl(null);
        }
        return f;
    }

    public List<Filiere> findAll() throws SQLException {
        List<Filiere> list = new ArrayList<>();
        String sql = "SELECT id, code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id, video_url FROM filiere ORDER BY nom";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Filiere findById(int id) throws SQLException {
        String sql = "SELECT id, code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id, video_url FROM filiere WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    public List<Filiere> findByUniversite(int universiteId) throws SQLException {
        List<Filiere> list = new ArrayList<>();
        String sql = "SELECT id, code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id, video_url FROM filiere WHERE universite_id = ? ORDER BY nom";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, universiteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Filiere> findByNiveau(String niveau) throws SQLException {
        List<Filiere> list = new ArrayList<>();
        String sql = "SELECT id, code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id, video_url FROM filiere WHERE niveau = ? ORDER BY nom";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, niveau);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Filiere f) throws SQLException {
        String sql = "INSERT INTO filiere (code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id, video_url) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getCode());
            ps.setString(2, f.getNom());
            ps.setString(3, f.getNiveau());
            ps.setInt(4, f.getDureeAnnees());
            ps.setInt(5, f.getCapaciteMax());
            ps.setInt(6, f.getUniversiteId());
            ps.setInt(7, f.getResponsableId());
            ps.setString(8, f.getVideoUrl());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                f.setId(keys.getInt(1));
            }
        }
    }

    public void update(Filiere f) throws SQLException {
        String sql = "UPDATE filiere SET code=?, nom=?, niveau=?, duree_annees=?, capacite_max=?, universite_id=?, responsable_id=?, video_url=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, f.getCode());
            ps.setString(2, f.getNom());
            ps.setString(3, f.getNiveau());
            ps.setInt(4, f.getDureeAnnees());
            ps.setInt(5, f.getCapaciteMax());
            ps.setInt(6, f.getUniversiteId());
            ps.setInt(7, f.getResponsableId());
            ps.setString(8, f.getVideoUrl());
            ps.setInt(9, f.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM filiere WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM filiere";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
