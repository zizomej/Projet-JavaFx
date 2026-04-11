package com.learnhub.dao;

import com.learnhub.models.Utilisateur;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        // CORRECTION: utiliser 'mot_de_passe' au lieu de 'password'
        u.setPassword(rs.getString("mot_de_passe"));
        // CORRECTION: utiliser 'role' au lieu de 'roles'
        u.setRoles(rs.getString("role"));
        u.setTelephone(rs.getString("telephone"));
        // Ces colonnes n'existent pas dans votre table, mettre des valeurs par défaut
        u.setAdresse("");  // colonne n'existe pas
        u.setDateNaissance("");  // colonne n'existe pas
        // CORRECTION: 'statut' au lieu de 'actif'
        u.setActif("actif".equalsIgnoreCase(rs.getString("statut")));
        u.setPhoto("");  // colonne n'existe pas
        return u;
    }

    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom, prenom";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Utilisateur> findByRole(String role) throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        // CORRECTION: 'role' au lieu de 'roles', 'statut' au lieu de 'actif'
        String sql = "SELECT * FROM utilisateur WHERE role = ? AND statut = 'actif' ORDER BY nom, prenom";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Utilisateur findById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public void insert(Utilisateur u) throws SQLException {
        // CORRECTION: adapter aux colonnes réelles de la table
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, telephone, statut, date_inscription, cin) VALUES (?,?,?,?,?,?,?, NOW(), ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getRoles());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.isActif() ? "actif" : "inactif");
            ps.setString(8, "CIN_TEMP"); // CIN temporaire, à modifier selon vos besoins
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) u.setId(keys.getInt(1));
        }
    }

    public void update(Utilisateur u) throws SQLException {
        // CORRECTION: adapter aux colonnes réelles
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, role=?, telephone=?, statut=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getRoles());
            ps.setString(5, u.getTelephone());
            ps.setString(6, u.isActif() ? "actif" : "inactif");
            ps.setInt(7, u.getId());
            ps.executeUpdate();
        }
    }

    public void updatePassword(int id, String hashedPassword) throws SQLException {
        // CORRECTION: 'mot_de_passe' au lieu de 'password'
        String sql = "UPDATE utilisateur SET mot_de_passe=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByRole(String role) throws SQLException {
        // CORRECTION: 'role' au lieu de 'roles'
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
