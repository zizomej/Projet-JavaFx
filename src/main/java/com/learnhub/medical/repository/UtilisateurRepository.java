package com.learnhub.medical.repository;

import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurRepository {

    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom ASC, prenom ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    public List<Utilisateur> findByRole(String role) throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = ? ORDER BY nom ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void save(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (email, mot_de_passe, nom, prenom, cin, role, telephone, statut, date_inscription) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getMotDePasse() != null ? u.getMotDePasse() : "password123"); // Password mock
            ps.setString(3, u.getNom());
            ps.setString(4, u.getPrenom());
            ps.setString(5, u.getCin());
            ps.setString(6, u.getRole());
            ps.setString(7, u.getTelephone());
            ps.setString(8, u.getStatut() != null ? u.getStatut() : "actif");
            ps.executeUpdate();
        }
    }

    public void update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET email=?, nom=?, prenom=?, cin=?, role=?, telephone=?, statut=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getNom());
            ps.setString(3, u.getPrenom());
            ps.setString(4, u.getCin());
            ps.setString(5, u.getRole());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.getStatut());
            ps.setInt(8, u.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Utilisateur map(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setCin(rs.getString("cin"));
        u.setRole(rs.getString("role"));
        u.setTelephone(rs.getString("telephone"));
        u.setStatut(rs.getString("statut"));
        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null) u.setDateInscription(ts.toLocalDateTime());
        return u;
    }
}
