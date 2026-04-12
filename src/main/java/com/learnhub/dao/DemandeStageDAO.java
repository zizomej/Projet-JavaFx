package com.learnhub.dao;

import com.learnhub.models.DemandeStage;
import com.learnhub.util.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DemandeStageDAO {

    private static final String BASE_SELECT = """
        SELECT d.id,
               d.date_demande,
               d.statut,
               d.piece_jointe,
               d.motivation,
               d.offre_stage_id,
               d.etudiant_id,
               CONCAT(u.prenom, ' ', u.nom) AS etudiant_nom,
               u.email AS etudiant_email,
               o.titre AS offre_titre,
               p.nom AS partenaire_nom
        FROM demande_stage d
        LEFT JOIN utilisateur u ON d.etudiant_id = u.id
        LEFT JOIN offre_stage o ON d.offre_stage_id = o.id
        LEFT JOIN partenaire p ON o.partenaire_id = p.id
        """;

    private DemandeStage mapRow(ResultSet rs) throws SQLException {
        DemandeStage demande = new DemandeStage();
        demande.setId(rs.getInt("id"));
        demande.setEtudiantId(rs.getInt("etudiant_id"));
        demande.setEtudiantNom(rs.getString("etudiant_nom"));
        demande.setEtudiantEmail(rs.getString("etudiant_email"));
        demande.setOffreStageId(rs.getInt("offre_stage_id"));
        demande.setOffreTitre(rs.getString("offre_titre"));
        demande.setPartenaireNom(rs.getString("partenaire_nom"));
        demande.setPieceJointe(rs.getString("piece_jointe"));
        demande.setMotivation(rs.getString("motivation"));
        demande.setDateDemande(rs.getString("date_demande"));
        demande.setStatut(rs.getString("statut"));
        return demande;
    }

    public List<DemandeStage> findAll() throws SQLException {
        List<DemandeStage> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY d.date_demande DESC, d.id DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<DemandeStage> findByEtudiant(int etudiantId) throws SQLException {
        List<DemandeStage> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE d.etudiant_id = ? ORDER BY d.date_demande DESC, d.id DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<DemandeStage> search(String query, String statut) throws SQLException {
        List<DemandeStage> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("""
                 AND (
                     CONCAT(u.prenom, ' ', u.nom) LIKE ?
                     OR COALESCE(u.email, '') LIKE ?
                     OR o.titre LIKE ?
                     OR p.nom LIKE ?
                     OR d.motivation LIKE ?
                     OR COALESCE(d.piece_jointe, '') LIKE ?
                 )
                """);
            String like = "%" + query.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (statut != null && !statut.isBlank()) {
            sql.append(" AND d.statut = ? ");
            params.add(statut);
        }

        sql.append(" ORDER BY d.date_demande DESC, d.id DESC");

        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countByStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demande_stage WHERE statut = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void insert(DemandeStage demande) throws SQLException {
        String sql = """
            INSERT INTO demande_stage (
                date_demande, statut, piece_jointe, motivation, offre_stage_id, etudiant_id
            ) VALUES (?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, demande.getDateDemande());
            ps.setString(2, demande.getStatut());
            ps.setString(3, normalizePieceJointe(demande.getPieceJointe()));
            ps.setString(4, normalizeMotivation(demande.getMotivation()));
            ps.setInt(5, demande.getOffreStageId());
            ps.setInt(6, demande.getEtudiantId());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                demande.setId(keys.getInt(1));
            }
        }
    }

    public void updateStatut(int id, String statut, String commentaire) throws SQLException {
        String sql = "UPDATE demande_stage SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM demande_stage WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private String normalizePieceJointe(String pieceJointe) {
        if (pieceJointe == null || pieceJointe.isBlank()) {
            return "piece-non-fournie";
        }
        return pieceJointe.trim();
    }

    private String normalizeMotivation(String motivation) {
        if (motivation == null || motivation.isBlank()) {
            return "Motivation non fournie.";
        }
        return motivation.trim();
    }
}
