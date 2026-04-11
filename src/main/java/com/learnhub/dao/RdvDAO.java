package com.learnhub.dao;

import com.learnhub.models.Rdv;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RdvDAO {

    private Rdv mapRow(ResultSet rs) throws SQLException {
        Rdv r = new Rdv();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setPatientNom(rs.getString("patient_nom"));
        r.setMedecinId(rs.getInt("medecin_id"));
        r.setMedecinNom(rs.getString("medecin_nom"));
        r.setCreneauId(rs.getInt("creneau_id"));
        r.setDateHeure(rs.getString("date_heure"));
        r.setMotif(rs.getString("motif"));
        r.setStatut(rs.getString("statut"));
        r.setNotes(rs.getString("notes"));
        return r;
    }

    public List<Rdv> findAll() throws SQLException {
        List<Rdv> list = new ArrayList<>();
        String sql = """
            SELECT r.*,
                CONCAT(p.prenom,' ',p.nom) as patient_nom,
                CONCAT(m.prenom,' ',m.nom) as medecin_nom
            FROM rdv r
            LEFT JOIN utilisateur p ON r.patient_id=p.id
            LEFT JOIN utilisateur m ON r.medecin_id=m.id
            ORDER BY r.date_heure DESC
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Rdv> findByPatient(int patientId) throws SQLException {
        List<Rdv> list = new ArrayList<>();
        String sql = """
            SELECT r.*,
                CONCAT(p.prenom,' ',p.nom) as patient_nom,
                CONCAT(m.prenom,' ',m.nom) as medecin_nom
            FROM rdv r
            LEFT JOIN utilisateur p ON r.patient_id=p.id
            LEFT JOIN utilisateur m ON r.medecin_id=m.id
            WHERE r.patient_id=?
            ORDER BY r.date_heure DESC
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Rdv> findByMedecin(int medecinId) throws SQLException {
        List<Rdv> list = new ArrayList<>();
        String sql = """
            SELECT r.*,
                CONCAT(p.prenom,' ',p.nom) as patient_nom,
                CONCAT(m.prenom,' ',m.nom) as medecin_nom
            FROM rdv r
            LEFT JOIN utilisateur p ON r.patient_id=p.id
            LEFT JOIN utilisateur m ON r.medecin_id=m.id
            WHERE r.medecin_id=?
            ORDER BY r.date_heure DESC
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void insert(Rdv r) throws SQLException {
        String sql = "INSERT INTO rdv (patient_id, medecin_id, creneau_id, date_heure, motif, statut, notes) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getMedecinId());
            ps.setInt(3, r.getCreneauId());
            ps.setString(4, r.getDateHeure());
            ps.setString(5, r.getMotif());
            ps.setString(6, r.getStatut() != null ? r.getStatut() : "EN_ATTENTE");
            ps.setString(7, r.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setId(keys.getInt(1));
        }
    }

    public void update(Rdv r) throws SQLException {
        String sql = "UPDATE rdv SET patient_id=?, medecin_id=?, creneau_id=?, date_heure=?, motif=?, statut=?, notes=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getMedecinId());
            ps.setInt(3, r.getCreneauId());
            ps.setString(4, r.getDateHeure());
            ps.setString(5, r.getMotif());
            ps.setString(6, r.getStatut());
            ps.setString(7, r.getNotes());
            ps.setInt(8, r.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE rdv SET statut=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut); ps.setInt(2, id); ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM rdv WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM rdv")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    public List<Rdv> findByEtudiant(int etudiantId) throws SQLException {
        List<Rdv> list = new ArrayList<>();
        String sql = "SELECT * FROM rdv WHERE etudiant_id = ? ORDER BY date_demande DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int countByEtudiant(int etudiantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rdv WHERE etudiant_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
