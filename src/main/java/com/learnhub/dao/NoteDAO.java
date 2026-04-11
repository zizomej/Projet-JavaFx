package com.learnhub.dao;

import com.learnhub.models.Note;
import com.learnhub.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    private Note mapRow(ResultSet rs) throws SQLException {
        Note n = new Note();
        n.setId(rs.getInt("id"));
        n.setValeur(rs.getDouble("valeur"));
        n.setTypeNote(rs.getString("type_note"));
        n.setCoefficient(rs.getDouble("coefficient"));
        n.setDateSaisie(rs.getString("date_saisie"));
        n.setEtudiantId(rs.getInt("etudiant_id"));
        n.setEnseignantId(rs.getInt("enseignant_id"));
        n.setModuleId(rs.getInt("module_id"));
        return n;
    }

    public List<Note> findAll() throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT n.*, CONCAT(u.prenom, ' ', u.nom) as etudiant_nom, m.intitule as module_intitule " +
                "FROM note n " +
                "LEFT JOIN utilisateur u ON n.etudiant_id = u.id " +
                "LEFT JOIN module m ON n.module_id = m.id " +
                "ORDER BY n.date_saisie DESC";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Note n = mapRow(rs);
                n.setEtudiantNom(rs.getString("etudiant_nom"));
                n.setModuleIntitule(rs.getString("module_intitule"));
                list.add(n);
            }
        }
        return list;
    }

    public List<Note> findByEtudiant(int etudiantId) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT n.*, CONCAT(u.prenom, ' ', u.nom) as etudiant_nom, m.intitule as module_intitule " +
                "FROM note n " +
                "LEFT JOIN utilisateur u ON n.etudiant_id = u.id " +
                "LEFT JOIN module m ON n.module_id = m.id " +
                "WHERE n.etudiant_id = ? " +
                "ORDER BY n.date_saisie DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Note n = mapRow(rs);
                n.setEtudiantNom(rs.getString("etudiant_nom"));
                n.setModuleIntitule(rs.getString("module_intitule"));
                list.add(n);
            }
        }
        return list;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM note";
        try (Statement st = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void add(Note note) throws SQLException {
        String sql = "INSERT INTO note (valeur, type_note, coefficient, date_saisie, etudiant_id, enseignant_id, module_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setDouble(1, note.getValeur());
            ps.setString(2, note.getTypeNote());
            ps.setDouble(3, note.getCoefficient());
            ps.setString(4, note.getDateSaisie());
            ps.setInt(5, note.getEtudiantId());
            ps.setInt(6, note.getEnseignantId());
            ps.setInt(7, note.getModuleId());
            ps.executeUpdate();
        }
    }

    public void update(Note note) throws SQLException {
        String sql = "UPDATE note SET valeur=?, type_note=?, coefficient=?, date_saisie=?, etudiant_id=?, enseignant_id=?, module_id=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setDouble(1, note.getValeur());
            ps.setString(2, note.getTypeNote());
            ps.setDouble(3, note.getCoefficient());
            ps.setString(4, note.getDateSaisie());
            ps.setInt(5, note.getEtudiantId());
            ps.setInt(6, note.getEnseignantId());
            ps.setInt(7, note.getModuleId());
            ps.setInt(8, note.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM note WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    
    public List<Note> findByProfesseur(int profId) throws SQLException {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT n.*, CONCAT(u.prenom, ' ', u.nom) as etudiant_nom, m.intitule as module_intitule " +
                "FROM note n " +
                "LEFT JOIN utilisateur u ON n.etudiant_id = u.id " +
                "LEFT JOIN module m ON n.module_id = m.id " +
                "WHERE n.enseignant_id = ? " +
                "ORDER BY n.date_saisie DESC";
        try (PreparedStatement ps = DatabaseConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, profId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Note n = mapRow(rs);
                n.setEtudiantNom(rs.getString("etudiant_nom"));
                n.setModuleIntitule(rs.getString("module_intitule"));
                list.add(n);
            }
        }
        return list;
    }
}
