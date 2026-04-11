package com.learnhub.dao;

import com.learnhub.util.DatabaseConnection;
import java.sql.*;

public class LieuDAO {

    public int getOrCreateLieuId(String nom) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            return 0; // Ou gérer une valeur par défaut
        }
        
        nom = nom.trim();
        
        // 1. Chercher si le lieu existe déjà
        String selectSql = "SELECT id FROM lieu WHERE nom = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(selectSql)) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        // 2. Si non, le créer
        String insertSql = "INSERT INTO lieu (nom, adresse) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, ""); // Adresse par défaut
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        
        return 0;
    }
}
