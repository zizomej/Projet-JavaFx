package com.learnhub.dao;

import com.learnhub.models.Evenement;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EvenementDAO {

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setId(rs.getInt("id"));
        e.setTitre(rs.getString("titre"));
        e.setDescription(rs.getString("description"));
        e.setTypeEvenement(rs.getString("type_evenement"));

        // Conversion des dates
        Date dateDebutSql = rs.getDate("date_debut");
        if (dateDebutSql != null) e.setDateDebut(dateDebutSql.toLocalDate());

        Date dateFinSql = rs.getDate("date_fin");
        if (dateFinSql != null) e.setDateFin(dateFinSql.toLocalDate());

        Time heureDebutSql = rs.getTime("heure_debut");
        if (heureDebutSql != null) e.setHeureDebut(heureDebutSql.toLocalTime());

        Time heureFinSql = rs.getTime("heure_fin");
        if (heureFinSql != null) e.setHeureFin(heureFinSql.toLocalTime());

        e.setStatut(rs.getString("statut"));
        e.setLieuId(rs.getInt("lieu_id"));

        // Récupérer le nom du lieu si jointure
        try {
            e.setLieuNom(rs.getString("lieu_nom"));
        } catch (SQLException ex) {
            e.setLieuNom("");
        }

        return e;
    }

    public List<Evenement> findAll() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT e.*, l.nom as lieu_nom FROM evenement e " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id " +
                "ORDER BY e.date_debut DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Evenement> findUpcoming() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        // CORRECTION: utiliser 'date_debut' au lieu de 'date'
        String sql = "SELECT e.*, l.nom as lieu_nom FROM evenement e " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id " +
                "WHERE e.date_debut >= CURDATE() AND e.statut = 'En cours' " +
                "ORDER BY e.date_debut ASC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Evenement> findByType(String type) throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT e.*, l.nom as lieu_nom FROM evenement e " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id " +
                "WHERE e.type_evenement = ? ORDER BY e.date_debut DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Evenement findById(int id) throws SQLException {
        String sql = "SELECT e.*, l.nom as lieu_nom FROM evenement e " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id WHERE e.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void insert(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenement (titre, description, type_evenement, date_debut, date_fin, heure_debut, heure_fin, statut, lieu_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getTypeEvenement());
            ps.setDate(4, Date.valueOf(e.getDateDebut()));
            ps.setDate(5, Date.valueOf(e.getDateFin()));
            ps.setTime(6, Time.valueOf(e.getHeureDebut()));
            ps.setTime(7, Time.valueOf(e.getHeureFin()));
            ps.setString(8, e.getStatut());
            ps.setInt(9, e.getLieuId());
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
            ps.setDate(4, Date.valueOf(e.getDateDebut()));
            ps.setDate(5, Date.valueOf(e.getDateFin()));
            ps.setTime(6, Time.valueOf(e.getHeureDebut()));
            ps.setTime(7, Time.valueOf(e.getHeureFin()));
            ps.setString(8, e.getStatut());
            ps.setInt(9, e.getLieuId());
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
