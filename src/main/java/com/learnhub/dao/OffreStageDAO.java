package com.learnhub.dao;

import com.learnhub.models.OffreStage;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OffreStageDAO {

    private OffreStage mapRow(ResultSet rs) throws SQLException {
        OffreStage o = new OffreStage();
        o.setId(rs.getInt("id"));
        o.setTitre(rs.getString("titre"));
        o.setDescription(rs.getString("description"));
        o.setTypeStage(rs.getString("type_stage"));
        o.setDureeMois(rs.getInt("duree_mois"));
        o.setDatePublication(rs.getDate("date_publication").toLocalDate());
        o.setPartenaireId(rs.getInt("partenaire_id"));
        o.setFiliereId(rs.getInt("filiere_id"));

        try {
            o.setPartenaireNom(rs.getString("partenaire_nom"));
        } catch (SQLException e) {}
        try {
            o.setFiliereNom(rs.getString("filiere_nom"));
        } catch (SQLException e) {}

        o.setStatut("active");
        return o;
    }

    public List<OffreStage> findAll() throws SQLException {
        List<OffreStage> list = new ArrayList<>();
        String sql = "SELECT o.*, p.nom as partenaire_nom, f.nom as filiere_nom FROM offre_stage o " +
                "LEFT JOIN partenaire p ON o.partenaire_id = p.id " +
                "LEFT JOIN filiere f ON o.filiere_id = f.id " +
                "ORDER BY o.date_publication DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<OffreStage> findActive() throws SQLException {
        List<OffreStage> list = new ArrayList<>();
        String sql = "SELECT o.*, p.nom as partenaire_nom, f.nom as filiere_nom FROM offre_stage o " +
                "LEFT JOIN partenaire p ON o.partenaire_id = p.id " +
                "LEFT JOIN filiere f ON o.filiere_id = f.id " +
                "WHERE o.date_publication <= CURDATE() " +
                "ORDER BY o.date_publication DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<OffreStage> findByType(String type) throws SQLException {
        List<OffreStage> list = new ArrayList<>();
        String sql = "SELECT o.*, p.nom as partenaire_nom, f.nom as filiere_nom FROM offre_stage o " +
                "LEFT JOIN partenaire p ON o.partenaire_id = p.id " +
                "LEFT JOIN filiere f ON o.filiere_id = f.id " +
                "WHERE o.type_stage = ? ORDER BY o.date_publication DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public OffreStage findById(int id) throws SQLException {
        String sql = "SELECT o.*, p.nom as partenaire_nom, f.nom as filiere_nom FROM offre_stage o " +
                "LEFT JOIN partenaire p ON o.partenaire_id = p.id " +
                "LEFT JOIN filiere f ON o.filiere_id = f.id WHERE o.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void insert(OffreStage o) throws SQLException {
        String sql = "INSERT INTO offre_stage (titre, description, type_stage, duree_mois, date_publication, partenaire_id, filiere_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getTitre());
            ps.setString(2, o.getDescription());
            ps.setString(3, o.getTypeStage());
            ps.setInt(4, o.getDureeMois());
            ps.setDate(5, Date.valueOf(o.getDatePublication()));
            ps.setInt(6, o.getPartenaireId());
            ps.setInt(7, o.getFiliereId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) o.setId(keys.getInt(1));
        }
    }

    // UNE SEULE méthode update - celle avec date_publication
    public void update(OffreStage o) throws SQLException {
        String sql = "UPDATE offre_stage SET titre=?, description=?, type_stage=?, duree_mois=?, date_publication=?, partenaire_id=?, filiere_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, o.getTitre());
            ps.setString(2, o.getDescription());
            ps.setString(3, o.getTypeStage());
            ps.setInt(4, o.getDureeMois());
            ps.setDate(5, Date.valueOf(o.getDatePublication()));
            ps.setInt(6, o.getPartenaireId());
            ps.setInt(7, o.getFiliereId());
            ps.setInt(8, o.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM offre_stage WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM offre_stage";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countCandidatures(int offreId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demande_stage WHERE offre_stage_id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
