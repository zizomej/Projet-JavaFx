package com.learnhub.dao;

import com.learnhub.models.Presence;
import com.learnhub.util.DatabaseConnection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PresenceDAO {

    private static final String BASE_SELECT = """
        SELECT p.*,
               CONCAT(u.prenom,' ',u.nom) AS etudiant_nom,
               CONCAT(COALESCE(m.intitule, 'Seance'), ' - ', s.date_seance, ' - ', COALESCE(s.type, '')) AS seance_info,
               s.date_seance AS presence_date
        FROM presence p
        LEFT JOIN utilisateur u ON p.etudiant_id = u.id
        LEFT JOIN seance s ON p.seance_id = s.id
        LEFT JOIN module m ON s.module_id = m.id
        """;

    private Presence mapRow(ResultSet rs) throws SQLException {
        Presence presence = new Presence();
        presence.setId(rs.getInt("id"));
        presence.setEtudiantId(rs.getInt("etudiant_id"));
        presence.setEtudiantNom(rs.getString("etudiant_nom"));
        presence.setSeanceId(rs.getInt("seance_id"));
        presence.setSeanceInfo(rs.getString("seance_info"));
        presence.setStatut(normalizeStatus(rs.getString("statut")));
        Date date = rs.getDate("presence_date");
        presence.setDateSeance(date != null ? date.toString() : null);
        return presence;
    }

    public List<Presence> findAll() throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY s.date_seance DESC, p.id DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Presence> findByEtudiant(int etudiantId) throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.etudiant_id = ? ORDER BY s.date_seance DESC, p.id DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Presence> findBySeance(int seanceId) throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE p.seance_id = ? ORDER BY p.id DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, seanceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Presence presence) throws SQLException {
        String sql = "INSERT INTO presence (etudiant_id, seance_id, statut) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, presence.getEtudiantId());
            ps.setInt(2, presence.getSeanceId());
            ps.setString(3, normalizeStatus(presence.getStatut()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                presence.setId(keys.getInt(1));
            }
        }
    }

    public void update(Presence presence) throws SQLException {
        String sql = "UPDATE presence SET etudiant_id = ?, seance_id = ?, statut = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, presence.getEtudiantId());
            ps.setInt(2, presence.getSeanceId());
            ps.setString(3, normalizeStatus(presence.getStatut()));
            ps.setInt(4, presence.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM presence WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "PRESENT";
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("JUSTIFIE".equals(normalized) || "JUSTIFIEE".equals(normalized)) {
            return "JUSTIFIE";
        }
        if ("RETARD".equals(normalized)) {
            return "RETARD";
        }
        if ("ABSENT".equals(normalized)) {
            return "ABSENT";
        }
        return "PRESENT";
    }
}
