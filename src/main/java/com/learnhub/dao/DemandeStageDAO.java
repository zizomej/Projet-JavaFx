package com.learnhub.dao;

import com.learnhub.models.DemandeStage;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandeStageDAO {

    private DemandeStage mapRow(ResultSet rs) throws SQLException {
        DemandeStage d = new DemandeStage();
        d.setId(rs.getInt("id"));
        d.setDateDemande(rs.getDate("date_demande").toLocalDate());
        d.setStatut(rs.getString("statut"));
        d.setPieceJointe(rs.getString("piece_jointe"));
        d.setMotivation(rs.getString("motivation"));
        d.setOffreStageId(rs.getInt("offre_stage_id"));
        d.setEtudiantId(rs.getInt("etudiant_id"));

        try {
            d.setOffreTitre(rs.getString("offre_titre"));
        } catch (SQLException e) {}
        try {
            d.setEtudiantNom(rs.getString("etudiant_nom"));
            d.setEtudiantPrenom(rs.getString("etudiant_prenom"));
        } catch (SQLException e) {}

        return d;
    }

    public List<DemandeStage> findAll() throws SQLException {
        List<DemandeStage> list = new ArrayList<>();
        String sql = "SELECT d.*, o.titre as offre_titre, u.nom as etudiant_nom, u.prenom as etudiant_prenom " +
                "FROM demande_stage d " +
                "LEFT JOIN offre_stage o ON d.offre_stage_id = o.id " +
                "LEFT JOIN utilisateur u ON d.etudiant_id = u.id " +
                "ORDER BY d.date_demande DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<DemandeStage> findByStatut(String statut) throws SQLException {
        List<DemandeStage> list = new ArrayList<>();
        String sql = "SELECT d.*, o.titre as offre_titre, u.nom as etudiant_nom, u.prenom as etudiant_prenom " +
                "FROM demande_stage d " +
                "LEFT JOIN offre_stage o ON d.offre_stage_id = o.id " +
                "LEFT JOIN utilisateur u ON d.etudiant_id = u.id " +
                "WHERE d.statut = ? ORDER BY d.date_demande DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<DemandeStage> findByOffreStage(int offreStageId) throws SQLException {
        List<DemandeStage> list = new ArrayList<>();
        String sql = "SELECT d.*, o.titre as offre_titre, u.nom as etudiant_nom, u.prenom as etudiant_prenom " +
                "FROM demande_stage d " +
                "LEFT JOIN offre_stage o ON d.offre_stage_id = o.id " +
                "LEFT JOIN utilisateur u ON d.etudiant_id = u.id " +
                "WHERE d.offre_stage_id = ? ORDER BY d.date_demande DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, offreStageId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public DemandeStage findById(int id) throws SQLException {
        String sql = "SELECT d.*, o.titre as offre_titre, u.nom as etudiant_nom, u.prenom as etudiant_prenom " +
                "FROM demande_stage d " +
                "LEFT JOIN offre_stage o ON d.offre_stage_id = o.id " +
                "LEFT JOIN utilisateur u ON d.etudiant_id = u.id " +
                "WHERE d.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void insert(DemandeStage d) throws SQLException {
        String sql = "INSERT INTO demande_stage (date_demande, statut, piece_jointe, motivation, offre_stage_id, etudiant_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(d.getDateDemande()));
            ps.setString(2, d.getStatut());
            ps.setString(3, d.getPieceJointe());
            ps.setString(4, d.getMotivation());
            ps.setInt(5, d.getOffreStageId());
            ps.setInt(6, d.getEtudiantId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) d.setId(keys.getInt(1));
        }
    }

    public void update(DemandeStage d) throws SQLException {
        String sql = "UPDATE demande_stage SET statut=?, piece_jointe=?, motivation=?, offre_stage_id=?, etudiant_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, d.getStatut());
            ps.setString(2, d.getPieceJointe());
            ps.setString(3, d.getMotivation());
            ps.setInt(4, d.getOffreStageId());
            ps.setInt(5, d.getEtudiantId());
            ps.setInt(6, d.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE demande_stage SET statut=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM demande_stage WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM demande_stage";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demande_stage WHERE statut=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public boolean hasAlreadyApplied(int etudiantId, int offreStageId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demande_stage WHERE etudiant_id=? AND offre_stage_id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ps.setInt(2, offreStageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }
}
