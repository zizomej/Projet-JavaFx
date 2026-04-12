package com.learnhub.dao;

import com.learnhub.models.Ressource;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RessourceDAO {

    public void add(Ressource ressource) throws SQLException {
        String req = "INSERT INTO ressource(titre, type, url, est_public, module_id) VALUES(?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, ressource.getTitre());
            ps.setString(2, ressource.getType());
            ps.setString(3, ressource.getUrl());
            ps.setBoolean(4, ressource.isEstPublic());
            ps.setInt(5, ressource.getModuleId());
            ps.executeUpdate();
        }
    }

    public void update(Ressource ressource) throws SQLException {
        String req = "UPDATE ressource SET titre = ?, type = ?, url = ?, est_public = ?, module_id = ? WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, ressource.getTitre());
            ps.setString(2, ressource.getType());
            ps.setString(3, ressource.getUrl());
            ps.setBoolean(4, ressource.isEstPublic());
            ps.setInt(5, ressource.getModuleId());
            ps.setInt(6, ressource.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String req = "DELETE FROM ressource WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Ressource> findAll() throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String req = "SELECT r.*, m.intitule AS module_intitule FROM ressource r LEFT JOIN module m ON r.module_id = m.id";
        try (Connection cnx = DatabaseConnection.getInstance();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Ressource r = new Ressource(
                    rs.getInt("id"), rs.getString("titre"), rs.getString("type"),
                    rs.getString("url"), rs.getBoolean("est_public"), rs.getInt("module_id")
                );
                r.setModuleIntitule(rs.getString("module_intitule"));
                ressources.add(r);
            }
        }
        return ressources;
    }

    public List<Ressource> findByModule(int moduleId) throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String req = "SELECT r.*, m.intitule AS module_intitule FROM ressource r LEFT JOIN module m ON r.module_id = m.id WHERE r.module_id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ressource r = new Ressource(
                    rs.getInt("id"), rs.getString("titre"), rs.getString("type"),
                    rs.getString("url"), rs.getBoolean("est_public"), rs.getInt("module_id")
                );
                r.setModuleIntitule(rs.getString("module_intitule"));
                ressources.add(r);
            }
        }
        return ressources;
    }
}
