package com.learnhub.dao;

import com.learnhub.models.Partenaire;
import com.learnhub.util.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PartenaireDAO {

    private Partenaire mapRow(ResultSet rs) throws SQLException {
        Partenaire partenaire = new Partenaire();
        partenaire.setId(rs.getInt("id"));
        partenaire.setNom(rs.getString("nom"));
        partenaire.setSecteur(rs.getString("secteur"));
        partenaire.setVille(rs.getString("ville"));
        partenaire.setEmail(rs.getString("email"));
        partenaire.setTelephone(rs.getString("telephone"));
        partenaire.setStatut(rs.getString("statut"));
        partenaire.setAdresse(rs.getString("adresse"));
        partenaire.setPays(rs.getString("pays"));
        partenaire.setWebsite(rs.getString("website"));
        partenaire.setDescription(rs.getString("description"));
        partenaire.setImage(rs.getString("image"));

        // ═══════════════════════════════════════════════════════════════
        //  NOUVEAUX CHAMPS LATITUDE / LONGITUDE
        // ═══════════════════════════════════════════════════════════════
        try {
            partenaire.setLatitude(rs.getString("latitude"));
        } catch (SQLException e) {
            // Colonne n'existe pas encore
        }
        try {
            partenaire.setLongitude(rs.getString("longitude"));
        } catch (SQLException e) {
            // Colonne n'existe pas encore
        }

        return partenaire;
    }

    public List<Partenaire> findAll() throws SQLException {
        List<Partenaire> list = new ArrayList<>();
        String sql = "SELECT * FROM partenaire ORDER BY nom";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Partenaires visibles sur l'espace visiteur (statut actif). */
    public List<Partenaire> findActiveOrdered() throws SQLException {
        List<Partenaire> list = new ArrayList<>();
        String sql = """
            SELECT * FROM partenaire
            WHERE LOWER(COALESCE(statut, '')) IN ('actif', 'active')
            ORDER BY nom
            """;
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Partenaire> search(String query) throws SQLException {
        return search(query, null, null);
    }

    public List<Partenaire> search(String query, String secteur, String statut) throws SQLException {
        List<Partenaire> list = new ArrayList<>();
        boolean hasQuery = query != null && !query.trim().isEmpty();
        StringBuilder sql = new StringBuilder("SELECT * FROM partenaire WHERE 1=1 ");

        if (hasQuery) {
            sql.append("""
                AND (nom LIKE ?
                   OR ville LIKE ?
                   OR email LIKE ?
                   OR COALESCE(pays, '') LIKE ?
                   OR COALESCE(secteur, '') LIKE ?
                   OR COALESCE(description, '') LIKE ?)
                """);
        }

        if (secteur != null && !secteur.isBlank()) {
            sql.append(" AND secteur = ?");
        }
        if (statut != null && !statut.isBlank()) {
            sql.append(" AND statut = ?");
        }
        sql.append(" ORDER BY nom");

        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql.toString())) {
            int index = 1;
            if (hasQuery) {
                String like = "%" + query.trim() + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
            }
            if (secteur != null && !secteur.isBlank()) {
                ps.setString(index++, secteur);
            }
            if (statut != null && !statut.isBlank()) {
                ps.setString(index, statut);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countByStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM partenaire WHERE statut = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM partenaire";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void insert(Partenaire partenaire) throws SQLException {
        String sql = """
            INSERT INTO partenaire (
                nom, secteur, ville, email, telephone, statut, adresse, pays, website, description, latitude, longitude
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, partenaire.getNom());
            ps.setString(2, partenaire.getSecteur());
            ps.setString(3, partenaire.getVille());
            ps.setString(4, partenaire.getEmail());
            ps.setString(5, partenaire.getTelephone());
            ps.setString(6, partenaire.getStatut());
            ps.setString(7, partenaire.getAdresse());
            ps.setString(8, partenaire.getPays());
            ps.setString(9, partenaire.getWebsite());
            ps.setString(10, partenaire.getDescription());
            ps.setString(11, partenaire.getLatitude());
            ps.setString(12, partenaire.getLongitude());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                partenaire.setId(keys.getInt(1));
            }
        }
    }

    public void update(Partenaire partenaire) throws SQLException {
        String sql = """
            UPDATE partenaire
            SET nom = ?,
                secteur = ?,
                ville = ?,
                email = ?,
                telephone = ?,
                statut = ?,
                adresse = ?,
                pays = ?,
                website = ?,
                description = ?,
                latitude = ?,
                longitude = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, partenaire.getNom());
            ps.setString(2, partenaire.getSecteur());
            ps.setString(3, partenaire.getVille());
            ps.setString(4, partenaire.getEmail());
            ps.setString(5, partenaire.getTelephone());
            ps.setString(6, partenaire.getStatut());
            ps.setString(7, partenaire.getAdresse());
            ps.setString(8, partenaire.getPays());
            ps.setString(9, partenaire.getWebsite());
            ps.setString(10, partenaire.getDescription());
            ps.setString(11, partenaire.getLatitude());
            ps.setString(12, partenaire.getLongitude());
            ps.setInt(13, partenaire.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM partenaire WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}