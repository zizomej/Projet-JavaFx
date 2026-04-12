package com.learnhub.dao;

import com.learnhub.models.Seance;
import com.learnhub.util.DatabaseConnection;
import com.learnhub.util.SessionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {

    private static final String BASE_SELECT = """
        SELECT s.*,
               m.intitule AS module_titre,
               CONCAT(u.prenom,' ',u.nom) AS enseignant_nom
        FROM seance s
        LEFT JOIN module m ON s.module_id = m.id
        LEFT JOIN utilisateur u ON s.enseignant_id = u.id
        """;

    private Seance mapRow(ResultSet rs) throws SQLException {
        Seance seance = new Seance();
        seance.setId(rs.getInt("id"));
        seance.setModuleId(rs.getInt("module_id"));
        seance.setModuleTitre(rs.getString("module_titre"));

        Date date = rs.getDate("date_seance");
        seance.setDateSeance(date != null ? date.toString() : null);

        Time heureDebut = rs.getTime("heure_debut");
        seance.setHeureDebut(heureDebut != null ? heureDebut.toLocalTime().toString() : null);

        Time heureFin = rs.getTime("heure_fin");
        seance.setHeureFin(heureFin != null ? heureFin.toLocalTime().toString() : null);

        seance.setSalle(rs.getString("salle"));
        seance.setType(rs.getString("type"));
        seance.setEnseignantId(rs.getInt("enseignant_id"));
        seance.setEnseignantNom(rs.getString("enseignant_nom"));
        return seance;
    }

    public List<Seance> findAll() throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY s.date_seance DESC, s.heure_debut";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Seance> findByModule(int moduleId) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE s.module_id = ? ORDER BY s.date_seance DESC, s.heure_debut";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Seance> findByProfesseur(int professeurId) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE s.enseignant_id = ? ORDER BY s.date_seance DESC, s.heure_debut";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, professeurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Seance findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE s.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    public void insert(Seance seance) throws SQLException {
        String sql = """
            INSERT INTO seance (module_id, date_seance, heure_debut, heure_fin, salle, type, enseignant_id)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, seance.getModuleId());
            ps.setDate(2, parseDate(seance.getDateSeance()));
            ps.setTime(3, parseTime(seance.getHeureDebut()));
            ps.setTime(4, parseTime(seance.getHeureFin()));
            ps.setString(5, defaultText(seance.getSalle(), "Salle a definir"));
            ps.setString(6, defaultText(seance.getType(), "Cours"));
            ps.setInt(7, resolveEnseignantId(seance));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                seance.setId(keys.getInt(1));
            }
        }
    }

    public void update(Seance seance) throws SQLException {
        String sql = """
            UPDATE seance
            SET module_id = ?, date_seance = ?, heure_debut = ?, heure_fin = ?, salle = ?, type = ?, enseignant_id = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, seance.getModuleId());
            ps.setDate(2, parseDate(seance.getDateSeance()));
            ps.setTime(3, parseTime(seance.getHeureDebut()));
            ps.setTime(4, parseTime(seance.getHeureFin()));
            ps.setString(5, defaultText(seance.getSalle(), "Salle a definir"));
            ps.setString(6, defaultText(seance.getType(), "Cours"));
            ps.setInt(7, resolveEnseignantId(seance));
            ps.setInt(8, seance.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM seance WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Date.valueOf(LocalDate.now());
        }
        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException ignored) {
            return Date.valueOf(LocalDate.now());
        }
    }

    private Time parseTime(String value) {
        if (value == null || value.isBlank()) {
            return Time.valueOf(LocalTime.of(8, 0));
        }
        String normalized = value.trim();
        if (normalized.length() == 5) {
            normalized = normalized + ":00";
        }
        try {
            return Time.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return Time.valueOf(LocalTime.of(8, 0));
        }
    }

    private String defaultText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private int resolveEnseignantId(Seance seance) throws SQLException {
        if (seance.getEnseignantId() > 0) {
            return seance.getEnseignantId();
        }

        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement ps = connection.prepareStatement("SELECT responsable_id FROM module WHERE id = ?")) {
            ps.setInt(1, seance.getModuleId());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return rs.getInt(1);
            }
        }

        if (SessionManager.getInstance().getCurrentUser() != null) {
            return SessionManager.getInstance().getCurrentUser().getId();
        }

        throw new SQLException("Impossible de determiner l'enseignant pour cette seance.");
    }
}
