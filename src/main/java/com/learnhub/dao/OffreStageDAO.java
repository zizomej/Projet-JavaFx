package com.learnhub.dao;

import com.learnhub.models.OffreStage;
import com.learnhub.util.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OffreStageDAO {

    private static final String BASE_SELECT = """
        SELECT o.id,
               o.titre,
               o.description,
               o.type_stage,
               o.duree_mois,
               o.date_publication,
               o.partenaire_id,
               p.nom AS partenaire_nom,
               o.filiere_id,
               f.nom AS filiere_nom,
               COUNT(d.id) AS candidature_count
        FROM offre_stage o
        LEFT JOIN partenaire p ON o.partenaire_id = p.id
        LEFT JOIN filiere f ON o.filiere_id = f.id
        LEFT JOIN demande_stage d ON d.offre_stage_id = o.id
        """;

    private static final String BASE_GROUP_BY = """
        GROUP BY o.id, o.titre, o.description, o.type_stage, o.duree_mois,
                 o.date_publication, o.partenaire_id, p.nom, o.filiere_id, f.nom
        """;

    private OffreStage mapRow(ResultSet rs) throws SQLException {
        OffreStage offre = new OffreStage();
        offre.setId(rs.getInt("id"));
        offre.setTitre(rs.getString("titre"));
        offre.setDescription(rs.getString("description"));
        offre.setTypeStage(rs.getString("type_stage"));
        offre.setDureeMois(rs.getInt("duree_mois"));
        offre.setDatePublication(rs.getString("date_publication"));
        offre.setPartenaireId(rs.getInt("partenaire_id"));
        offre.setPartenaireNom(rs.getString("partenaire_nom"));
        offre.setFiliereId(rs.getInt("filiere_id"));
        offre.setFiliereNom(rs.getString("filiere_nom"));
        offre.setCandidatureCount(rs.getInt("candidature_count"));
        offre.setStatut("publiee");
        return offre;
    }

    public List<OffreStage> findAll() throws SQLException {
        List<OffreStage> list = new ArrayList<>();
        String sql = BASE_SELECT + BASE_GROUP_BY + " ORDER BY o.date_publication DESC, o.id DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<OffreStage> findByStatut(String statut) throws SQLException {
        return findAll();
    }

    public List<OffreStage> search(String query, String type, String statut) throws SQLException {
        List<OffreStage> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("""
                 AND (
                     o.titre LIKE ?
                     OR o.description LIKE ?
                     OR p.nom LIKE ?
                     OR f.nom LIKE ?
                 )
                """);
            String like = "%" + query.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND o.type_stage = ? ");
            params.add(type);
        }

        sql.append(BASE_GROUP_BY);
        sql.append(" ORDER BY o.date_publication DESC, o.id DESC");

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

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM offre_stage";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countByType(String typeStage) throws SQLException {
        String sql = "SELECT COUNT(*) FROM offre_stage WHERE type_stage = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, typeStage);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countByStatut(String statut) throws SQLException {
        return count();
    }

    public void insert(OffreStage offre) throws SQLException {
        String sql = """
            INSERT INTO offre_stage (
                titre, description, type_stage, duree_mois, date_publication, partenaire_id, filiere_id
            ) VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getTypeStage());
            ps.setInt(4, offre.getDureeMois());
            ps.setString(5, offre.getDatePublication());
            ps.setInt(6, offre.getPartenaireId());
            if (offre.getFiliereId() > 0) {
                ps.setInt(7, offre.getFiliereId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                offre.setId(keys.getInt(1));
            }
        }
    }

    public void update(OffreStage offre) throws SQLException {
        String sql = """
            UPDATE offre_stage
            SET titre = ?,
                description = ?,
                type_stage = ?,
                duree_mois = ?,
                date_publication = ?,
                partenaire_id = ?,
                filiere_id = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setString(3, offre.getTypeStage());
            ps.setInt(4, offre.getDureeMois());
            ps.setString(5, offre.getDatePublication());
            ps.setInt(6, offre.getPartenaireId());
            if (offre.getFiliereId() > 0) {
                ps.setInt(7, offre.getFiliereId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, offre.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM offre_stage WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
