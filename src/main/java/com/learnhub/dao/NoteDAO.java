package com.learnhub.dao;

import com.learnhub.models.Note;
import com.learnhub.util.DatabaseConnection;
import com.learnhub.util.SessionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    private static final String BASE_SELECT = """
        SELECT n.*,
               CONCAT(e.prenom,' ',e.nom) AS etudiant_nom,
               m.intitule AS module_titre,
               CONCAT(t.prenom,' ',t.nom) AS enseignant_nom
        FROM note n
        LEFT JOIN utilisateur e ON n.etudiant_id = e.id
        LEFT JOIN module m ON n.module_id = m.id
        LEFT JOIN utilisateur t ON n.enseignant_id = t.id
        """;

    private Note mapRow(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setEtudiantId(rs.getInt("etudiant_id"));
        note.setEtudiantNom(rs.getString("etudiant_nom"));
        note.setModuleId(rs.getInt("module_id"));
        note.setModuleTitre(rs.getString("module_titre"));
        note.setValeur(rs.getDouble("valeur"));
        note.setTypeNote(rs.getString("type_note"));
        note.setCoefficient(rs.getDouble("coefficient"));
        Date date = rs.getDate("date_saisie");
        note.setDateSaisie(date != null ? date.toString() : null);
        note.setEnseignantId(rs.getInt("enseignant_id"));
        note.setEnseignantNom(rs.getString("enseignant_nom"));
        return note;
    }

    public List<Note> findAll() throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY n.date_saisie DESC, n.id DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Note> findByEtudiant(int etudiantId) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE n.etudiant_id = ? ORDER BY n.date_saisie DESC, n.id DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Note> findByModule(int moduleId) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE n.module_id = ? ORDER BY n.date_saisie DESC, n.valeur DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Note> findByEnseignant(int enseignantId) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE n.enseignant_id = ? ORDER BY n.date_saisie DESC, n.id DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public double getMoyenneEtudiant(int etudiantId) throws SQLException {
        String sql = "SELECT AVG(valeur) FROM note WHERE etudiant_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public void insert(Note note) throws SQLException {
        String sql = """
            INSERT INTO note (etudiant_id, module_id, valeur, type_note, coefficient, date_saisie, enseignant_id)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, note.getEtudiantId());
            ps.setInt(2, note.getModuleId());
            ps.setDouble(3, note.getValeur());
            ps.setString(4, safeType(note.getTypeNote()));
            ps.setDouble(5, note.getCoefficient() > 0 ? note.getCoefficient() : 1.0);
            ps.setDate(6, parseDate(note.getDateSaisie()));
            ps.setInt(7, resolveEnseignantId(note));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                note.setId(keys.getInt(1));
            }
        }
    }

    public void update(Note note) throws SQLException {
        String sql = """
            UPDATE note
            SET etudiant_id = ?, module_id = ?, valeur = ?, type_note = ?, coefficient = ?, date_saisie = ?, enseignant_id = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, note.getEtudiantId());
            ps.setInt(2, note.getModuleId());
            ps.setDouble(3, note.getValeur());
            ps.setString(4, safeType(note.getTypeNote()));
            ps.setDouble(5, note.getCoefficient() > 0 ? note.getCoefficient() : 1.0);
            ps.setDate(6, parseDate(note.getDateSaisie()));
            ps.setInt(7, resolveEnseignantId(note));
            ps.setInt(8, note.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM note WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Date.valueOf(LocalDate.now());
        }
        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException ignored) {
            return Date.valueOf(LocalDate.now());
        }
    }

    // Aliases for compatibility with reference controllers
    public List<Note> findByProfesseur(int profId) throws SQLException { return findByEnseignant(profId); }
    public void add(Note note) throws SQLException { insert(note); }

    private String safeType(String value) {
        return (value == null || value.isBlank()) ? "CC" : value.trim();
    }

    private int resolveEnseignantId(Note note) throws SQLException {
        if (note.getEnseignantId() > 0) {
            return note.getEnseignantId();
        }

        Connection connection = DatabaseConnection.getInstance();
        try (PreparedStatement ps = connection.prepareStatement("SELECT responsable_id FROM module WHERE id = ?")) {
            ps.setInt(1, note.getModuleId());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return rs.getInt(1);
            }
        }

        if (SessionManager.getInstance().getCurrentUser() != null) {
            return SessionManager.getInstance().getCurrentUser().getId();
        }

        throw new SQLException("Impossible de determiner l'enseignant pour cette note.");
    }
}
