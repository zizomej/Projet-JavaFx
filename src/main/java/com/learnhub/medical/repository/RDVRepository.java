package com.learnhub.medical.repository;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class RDVRepository {

    /** Tous les RDV avec le nom de l'étudiant (pour médecin/admin) */
    public List<RDV> findAllWithStudentNames() throws SQLException {
        List<RDV> list = new ArrayList<>();
        String sql = "SELECT r.*, u.nom, u.prenom FROM rdv r " +
                     "LEFT JOIN utilisateur u ON r.etudiant_id = u.id " +
                     "ORDER BY r.date_demande DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RDV rdv = map(rs);
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                String full = ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
                rdv.setStudentName(full.isEmpty() ? "Étudiant #" + rdv.getEtudiantId() : full);
                list.add(rdv);
            }
        }
        return list;
    }

    /** Tous les RDV (sans join) */
    public List<RDV> findAll() throws SQLException {
        List<RDV> list = new ArrayList<>();
        String sql = "SELECT * FROM rdv ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** RDV d'un étudiant spécifique */
    public List<RDV> findByStudentId(int studentId) throws SQLException {
        List<RDV> list = new ArrayList<>();
        String sql = "SELECT * FROM rdv WHERE etudiant_id=? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void save(RDV r) throws SQLException {
        String sql = "INSERT INTO rdv (motif, description, date_demande, statut, etudiant_id, creneau_id, compte_rendu, ordonnance_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getMotif());
            ps.setString(2, r.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(r.getDateDemande()));
            ps.setString(4, r.getStatut() != null ? r.getStatut() : "En attente");
            ps.setInt(5, r.getEtudiantId());
            ps.setInt(6, r.getCreneauId());
            ps.setString(7, r.getCompteRendu() != null ? r.getCompteRendu() : "");
            ps.setString(8, r.getOrdonnanceUrl() != null ? r.getOrdonnanceUrl() : "");
            ps.executeUpdate();
        }
    }

    public void update(RDV r) throws SQLException {
        String sql = "UPDATE rdv SET motif=?, description=?, date_demande=?, statut=?, " +
                     "compte_rendu=?, creneau_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getMotif());
            ps.setString(2, r.getDescription());
            ps.setDate(3, r.getDateDemande() != null ? java.sql.Date.valueOf(r.getDateDemande()) : null);
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getCompteRendu());
            ps.setInt(6, r.getCreneauId());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM rdv WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE rdv SET statut=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Unicité : Vérifie si un créneau est déjà pris à une date donnée (excluant le RDV actuel si modification) */
    public boolean isCreneauTaken(int creneauId, LocalDate date, int excludeRdvId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rdv WHERE creneau_id=? AND date_demande=? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creneauId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setInt(3, excludeRdvId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public int getFirstStudentId() throws SQLException {
        String sql = "SELECT id FROM utilisateur WHERE role='ETUDIANT' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("id");
        }
        // Fallback to any user if no student found, or throw if empty
        String sqlAny = "SELECT id FROM utilisateur LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlAny)) {
            if (rs.next()) return rs.getInt("id");
        }
        throw new SQLException("Aucun utilisateur trouvé en base de données pour lier le rendez-vous.");
    }

    public Map<String, Integer> getStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT " +
            "SUM(CASE WHEN statut='En attente' THEN 1 ELSE 0 END) AS pending, " +
            "SUM(CASE WHEN statut IN ('Confirmé','Accepté') THEN 1 ELSE 0 END) AS confirmed, " +
            "SUM(CASE WHEN date_demande = CURRENT_DATE THEN 1 ELSE 0 END) AS today, " +
            "SUM(CASE WHEN MONTH(date_demande)=MONTH(CURRENT_DATE) AND YEAR(date_demande)=YEAR(CURRENT_DATE) THEN 1 ELSE 0 END) AS month " +
            "FROM rdv";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("pending",   rs.getInt("pending"));
                stats.put("confirmed", rs.getInt("confirmed"));
                stats.put("today",     rs.getInt("today"));
                stats.put("month",     rs.getInt("month"));
            }
        }
        return stats;
    }

    public RDV findById(int id) throws SQLException {
        String sql = "SELECT * FROM rdv WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    /**
     * Libération ATOMIQUE du rendez-vous et du créneau.
     * Utilise connection.setAutoCommit(false) pour garantir la cohérence des données.
     */
    public void processAtomicCancellation(int rdvId, int creneauId, String newStatus) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // DEBUT TRANSACTION

            // 1. Mise à jour du RDV
            String sqlRdv = "UPDATE rdv SET statut=? WHERE id=?";
            try (PreparedStatement psRdv = conn.prepareStatement(sqlRdv)) {
                psRdv.setString(1, newStatus);
                psRdv.setInt(2, rdvId);
                psRdv.executeUpdate();
            }

            // 2. Libération du créneau (dispo = 1)
            String sqlCreneau = "UPDATE creneau SET disponibilite=1 WHERE id=?";
            try (PreparedStatement psCr = conn.prepareStatement(sqlCreneau)) {
                psCr.setInt(1, creneauId);
                psCr.executeUpdate();
            }

            conn.commit(); // VALIDATION TRANSACTION
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // ANNULATION EN CAS D'ERREUR
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    /**
     * Echange ATOMIQUE de créneaux.
     * Libère l'ancien créneau, réserve le nouveau, et met à jour le RDV.
     */
    public void processAtomicReschedule(int rdvId, int oldCreneauId, int newCreneauId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // DEBUT TRANSACTION

            // 1. Libérer l'ancien créneau
            String sqlOld = "UPDATE creneau SET disponibilite=1 WHERE id=?";
            try (PreparedStatement psOld = conn.prepareStatement(sqlOld)) {
                psOld.setInt(1, oldCreneauId);
                psOld.executeUpdate();
            }

            // 2. Réserver le nouveau créneau
            String sqlNew = "UPDATE creneau SET disponibilite=0 WHERE id=?";
            try (PreparedStatement psNew = conn.prepareStatement(sqlNew)) {
                psNew.setInt(1, newCreneauId);
                psNew.executeUpdate();
            }

            // 3. Mettre à jour le RDV
            String sqlRdv = "UPDATE rdv SET creneau_id=?, statut='Confirmé' WHERE id=?";
            try (PreparedStatement psRdv = conn.prepareStatement(sqlRdv)) {
                psRdv.setInt(1, newCreneauId);
                psRdv.setInt(2, rdvId);
                psRdv.executeUpdate();
            }

            conn.commit(); // VALIDATION
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    /**
     * Récupère les annulations récentes pour l'interface Médecin.
     */
    public List<RDV> findRecentCancellations() throws SQLException {
        List<RDV> list = new ArrayList<>();
        String sql = "SELECT r.*, u.nom, u.prenom FROM rdv r " +
                     "JOIN utilisateur u ON r.etudiant_id = u.id " +
                     "WHERE r.statut LIKE 'CANCELLED%' " +
                     "ORDER BY r.id DESC LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RDV rdv = map(rs);
                rdv.setStudentName(rs.getString("prenom") + " " + rs.getString("nom"));
                list.add(rdv);
            }
        }
        return list;
    }

    private RDV map(ResultSet rs) throws SQLException {
        java.sql.Date d = rs.getDate("date_demande");
        return new RDV(
            rs.getInt("id"),
            rs.getString("motif"),
            rs.getString("description"),
            d != null ? d.toLocalDate() : null,
            rs.getString("statut"),
            rs.getString("compte_rendu"),
            rs.getString("ordonnance_url"),
            rs.getInt("etudiant_id"),
            rs.getInt("creneau_id")
        );
    }
}
