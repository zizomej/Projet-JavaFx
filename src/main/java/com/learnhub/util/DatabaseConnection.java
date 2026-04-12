package com.learnhub.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    /** URL utilisée par l’app et les tests (vérifier le même schéma dans MySQL Workbench). */
    public static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/gestion_universitaire?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "";

    private static Connection instance;

    private DatabaseConnection() {}

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                instance.setAutoCommit(true);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable", e);
            }
        }
        return instance;
    }

    public static void close() {
        if (instance != null) {
            try { instance.close(); } catch (SQLException ignored) {}
        }
    }
}
