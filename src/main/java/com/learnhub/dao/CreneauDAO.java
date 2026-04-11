package com.learnhub.dao;

import com.learnhub.models.Creneau;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreneauDAO {

    private Creneau mapRow(ResultSet rs) throws SQLException {
        Creneau c = new Creneau();
        c.setId(rs.getInt("id"));
        c.setMedecinId(rs.getInt("medecin_id"));
        c.setMedecinNom(rs.getString("medecin_nom"));
        c.setDate(rs.getString("date"));
        c.setHeureDebut(rs.getString("heure_debut"));
        c.setHeureFin(rs.getString("heure_fin"));
        c.setDisponible(rs.getBoolean("disponible"));
        return c;
    }

    public List<Creneau> findAll() throws SQLException {
        List<Creneau> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(u.prenom,' ',u.nom) as medecin_nom FROM creneau c LEFT JOIN utilisateur u ON c.medecin_id=u.id ORDER BY c.date, c.heure_debut";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Creneau> findByMedecin(int medecinId) throws SQLException {
        List<Creneau> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(u.prenom,' ',u.nom) as medecin_nom FROM creneau c LEFT JOIN utilisateur u ON c.medecin_id=u.id WHERE c.medecin_id=? ORDER BY c.date, c.heure_debut";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Creneau> findDisponibles() throws SQLException {
        List<Creneau> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(u.prenom,' ',u.nom) as medecin_nom FROM creneau c LEFT JOIN utilisateur u ON c.medecin_id=u.id WHERE c.disponible=1 AND c.date >= CURDATE() ORDER BY c.date, c.heure_debut";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void insert(Creneau c) throws SQLException {
        String sql = "INSERT INTO creneau (medecin_id, date, heure_debut, heure_fin, disponible) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getMedecinId());
            ps.setString(2, c.getDate());
            ps.setString(3, c.getHeureDebut());
            ps.setString(4, c.getHeureFin());
            ps.setBoolean(5, c.isDisponible());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getInt(1));
        }
    }

    public void update(Creneau c) throws SQLException {
        String sql = "UPDATE creneau SET medecin_id=?, date=?, heure_debut=?, heure_fin=?, disponible=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, c.getMedecinId());
            ps.setString(2, c.getDate());
            ps.setString(3, c.getHeureDebut());
            ps.setString(4, c.getHeureFin());
            ps.setBoolean(5, c.isDisponible());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement("DELETE FROM creneau WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
}
