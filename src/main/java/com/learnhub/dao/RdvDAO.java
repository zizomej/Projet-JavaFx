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
        r.setMotif(rs.getString("motif"));
        r.setDescription(rs.getString("description"));
        r.setDateDemande(rs.getString("date_demande"));
        r.setStatut(rs.getString("statut"));
        r.setCompteRendu(rs.getString("compte_rendu"));
        r.setOrdonnanceUrl(rs.getString("ordonnance_url"));
        r.setEtudiantId(rs.getInt("etudiant_id"));
        r.setCreneauId(rs.getInt("creneau_id"));
        try { r.setEtudiantNom(rs.getString("etudiant_nom")); } catch (SQLException ignored) {}
        try { r.setMedecinNom(rs.getString("medecin_nom")); } catch (SQLException ignored) {}
        return r;
    }

    public List<Rdv> findAll() throws SQLException {
        List<Rdv> list = new ArrayList<>();
        String sql = """
            SELECT r.*, CONCAT(u.prenom,' ',u.nom) AS etudiant_nom,
                   CONCAT(m.prenom,' ',m.nom) AS medecin_nom
            FROM rdv r
            LEFT JOIN utilisateur u ON r.etudiant_id = u.id
            LEFT JOIN creneau c ON r.creneau_id = c.id
            LEFT JOIN utilisateur m ON c.medecin_id = m.id
            ORDER BY r.date_demande DESC
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Rdv> findByEtudiant(int etudiantId) throws SQLException {
        List<Rdv> list = new ArrayList<>();
        String sql = """
            SELECT r.*, CONCAT(u.prenom,' ',u.nom) AS etudiant_nom
            FROM rdv r
            LEFT JOIN utilisateur u ON r.etudiant_id = u.id
            WHERE r.etudiant_id = ?
            ORDER BY r.date_demande DESC
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Alias for backward compat
    public List<Rdv> findByPatient(int etudiantId) throws SQLException {
        return findByEtudiant(etudiantId);
    }

    public void insert(Rdv r) throws SQLException {
        String sql = "INSERT INTO rdv (motif, description, date_demande, statut, etudiant_id, creneau_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getMotif());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getDateDemande() != null ? r.getDateDemande() : java.time.LocalDate.now().toString());
            ps.setString(4, r.getStatut() != null ? r.getStatut() : "En attente");
            ps.setInt(5, r.getEtudiantId());
            if (r.getCreneauId() > 0) ps.setInt(6, r.getCreneauId()); else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setId(keys.getInt(1));
        }
    }

    public void update(Rdv r) throws SQLException {
        String sql = "UPDATE rdv SET motif=?, description=?, date_demande=?, statut=?, compte_rendu=?, ordonnance_url=?, etudiant_id=?, creneau_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, r.getMotif());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getDateDemande());
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getCompteRendu());
            ps.setString(6, r.getOrdonnanceUrl());
            ps.setInt(7, r.getEtudiantId());
            if (r.getCreneauId() > 0) ps.setInt(8, r.getCreneauId()); else ps.setNull(8, Types.INTEGER);
            ps.setInt(9, r.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatut(int id, String statut) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement("UPDATE rdv SET statut=? WHERE id=?")) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement("DELETE FROM rdv WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM rdv")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
