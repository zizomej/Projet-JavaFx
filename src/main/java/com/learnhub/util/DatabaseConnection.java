package com.learnhub.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_universitaire?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection instance;

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                updateSchema(); // Run migration on first connection
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable", e);
            }
        }
        return instance;
    }

    private static void updateSchema() {
        try (Statement stmt = instance.createStatement()) {
            // Add video_url column if not exists
            try {
                stmt.executeUpdate("ALTER TABLE filiere ADD COLUMN video_url VARCHAR(255)");
                System.out.println("Migration: Column video_url added.");
            } catch (SQLException e) {
                // Column probably already exists, ignore
            }

            // One-time migration to WIPE the unwanted MP4 links
            stmt.executeUpdate("UPDATE filiere SET video_url = NULL");
            System.out.println("Migration: All old video links (MP4) have been deleted.");
            
        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }

    public static void close() {
        if (instance != null) {
            try { instance.close(); } catch (SQLException ignored) {}
        }
    }
}
