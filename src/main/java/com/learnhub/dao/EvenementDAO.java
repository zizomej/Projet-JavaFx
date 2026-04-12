package com.learnhub.dao;

import com.learnhub.models.Evenement;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementDAO {

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setId(rs.getInt("id"));
        e.setTitre(rs.getString("titre"));
        e.setDescription(rs.getString("description"));
        e.setTypeEvenement(rs.getString("type_evenement"));
        e.setDateDebut(rs.getString("date_debut"));
        e.setDateFin(rs.getString("date_fin"));
        e.setHeureDebut(rs.getString("heure_debut"));
        e.setHeureFin(rs.getString("heure_fin"));
        e.setStatut(rs.getString("statut"));
        e.setLieuId(rs.getInt("lieu_id"));
        try { e.setLieuNom(rs.getString("lieu_nom")); } catch (SQLException ignored) {}
        return e;
    }

    public List<Evenement> findAll() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = """
            SELECT e.*, l.nom AS lieu_nom
            FROM evenement e
            LEFT JOIN lieu l ON e.lieu_id = l.id
            ORDER BY e.date_debut DESC
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Evenement> findUpcoming() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = """
            SELECT e.*, l.nom AS lieu_nom
            FROM evenement e
            LEFT JOIN lieu l ON e.lieu_id = l.id
            WHERE e.date_debut >= CURDATE()
            ORDER BY e.date_debut ASC
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void insert(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenement (titre, description, type_evenement, date_debut, date_fin, heure_debut, heure_fin, statut, lieu_id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getTypeEvenement());
            ps.setString(4, e.getDateDebut());
            ps.setString(5, e.getDateFin());
            ps.setString(6, e.getHeureDebut());
            ps.setString(7, e.getHeureFin());
            ps.setString(8, e.getStatut() != null ? e.getStatut() : "En attente");
            if (e.getLieuId() > 0) ps.setInt(9, e.getLieuId()); else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) e.setId(keys.getInt(1));
        }
    }

    public void update(Evenement e) throws SQLException {
        String sql = "UPDATE evenement SET titre=?, description=?, type_evenement=?, date_debut=?, date_fin=?, heure_debut=?, heure_fin=?, statut=?, lieu_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getTypeEvenement());
            ps.setString(4, e.getDateDebut());
            ps.setString(5, e.getDateFin());
            ps.setString(6, e.getHeureDebut());
            ps.setString(7, e.getHeureFin());
            ps.setString(8, e.getStatut());
            if (e.getLieuId() > 0) ps.setInt(9, e.getLieuId()); else ps.setNull(9, Types.INTEGER);
            ps.setInt(10, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement("DELETE FROM evenement WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM evenement")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
