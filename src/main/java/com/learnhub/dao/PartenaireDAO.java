package com.learnhub.dao;

import com.learnhub.models.Partenaire;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartenaireDAO {

    private Partenaire mapRow(ResultSet rs) throws SQLException {
        Partenaire p = new Partenaire();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setSecteur(rs.getString("secteur"));
        p.setVille(rs.getString("ville"));
        p.setEmail(rs.getString("email"));
        p.setTelephone(rs.getString("telephone"));
        p.setStatut(rs.getString("statut"));
        p.setImage(rs.getString("image"));
        p.setLatitude(rs.getString("latitude"));
        p.setLongitude(rs.getString("longitude"));
        p.setAdresseVerifiee(rs.getBoolean("adresse_verifiee"));
        p.setAdresse(rs.getString("adresse"));
        p.setPays(rs.getString("pays"));
        p.setWebsite(rs.getString("website"));
        p.setDescription(rs.getString("description"));
        return p;
    }

    public List<Partenaire> findAll() throws SQLException {
        List<Partenaire> list = new ArrayList<>();
        String sql = "SELECT * FROM partenaire WHERE statut IS NULL OR statut != 'inactif' ORDER BY nom";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    public List<Partenaire> findByStatut(String statut) throws SQLException {
        List<Partenaire> list = new ArrayList<>();
        String sql = "SELECT * FROM partenaire WHERE statut = ? ORDER BY nom";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    public Partenaire findById(int id) throws SQLException {
        String sql = "SELECT * FROM partenaire WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        }
        return null;
    }

    public void insert(Partenaire p) throws SQLException {
        String sql = "INSERT INTO partenaire (nom, secteur, ville, email, telephone, statut, image, latitude, longitude, adresse_verifiee, adresse, pays, website, description) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getSecteur());
            ps.setString(3, p.getVille());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getTelephone());
            ps.setString(6, p.getStatut());
            ps.setString(7, p.getImage());
            ps.setString(8, p.getLatitude());
            ps.setString(9, p.getLongitude());
            ps.setBoolean(10, p.isAdresseVerifiee());
            ps.setString(11, p.getAdresse());
            ps.setString(12, p.getPays());
            ps.setString(13, p.getWebsite());
            ps.setString(14, p.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                p.setId(keys.getInt(1));
        }
    }

    public void update(Partenaire p) throws SQLException {
        String sql = "UPDATE partenaire SET nom=?, secteur=?, ville=?, email=?, telephone=?, statut=?, image=?, latitude=?, longitude=?, adresse_verifiee=?, adresse=?, pays=?, website=?, description=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getSecteur());
            ps.setString(3, p.getVille());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getTelephone());
            ps.setString(6, p.getStatut());
            ps.setString(7, p.getImage());
            ps.setString(8, p.getLatitude());
            ps.setString(9, p.getLongitude());
            ps.setBoolean(10, p.isAdresseVerifiee());
            ps.setString(11, p.getAdresse());
            ps.setString(12, p.getPays());
            ps.setString(13, p.getWebsite());
            ps.setString(14, p.getDescription());
            ps.setInt(15, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        // Soft delete to avoid foreign key constraints
        String sql = "UPDATE partenaire SET statut='inactif' WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM partenaire";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        }
        return 0;
    }

    public int countByStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM partenaire WHERE statut=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        }
        return 0;
    }
}
