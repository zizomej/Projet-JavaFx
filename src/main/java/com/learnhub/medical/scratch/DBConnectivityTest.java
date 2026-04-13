package com.learnhub.medical.scratch;

import com.learnhub.medical.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnectivityTest {
    public static void main(String[] args) {
        System.out.println("--- 🔍 Test de connexion BDD ---");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Connexion réussie à 'gestion_universitaire' !");
                
                Statement stmt = conn.createStatement();
                
                // Vérifier les tables
                checkTable(stmt, "utilisateur");
                checkTable(stmt, "medical_creneau");
                checkTable(stmt, "medical_rdv");
                
                System.out.println("\n--- 📊 Résumé des données ---");
                countRows(stmt, "utilisateur");
                countRows(stmt, "medical_creneau");
                countRows(stmt, "medical_rdv");
                
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    private static void checkTable(Statement stmt, String table) {
        try {
            stmt.executeQuery("SELECT 1 FROM " + table + " LIMIT 1");
            System.out.println("✅ Table '" + table + "' trouvée.");
        } catch (Exception e) {
            System.err.println("❌ Table '" + table + "' manquante ou inaccessible.");
        }
    }

    private static void countRows(Statement stmt, String table) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
            if (rs.next()) {
                System.out.println("- Table '" + table + "' : " + rs.getInt(1) + " lignes.");
            }
        } catch (Exception e) {}
    }
}
