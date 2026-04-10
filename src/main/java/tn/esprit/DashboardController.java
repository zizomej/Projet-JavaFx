package tn.esprit;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    
    // Summary Cards
    @FXML private Label lblTotalUtilisateurs;
    @FXML private Label lblEtudiantsList;
    @FXML private Label lblProfsList;
    @FXML private Label lblMedecinsList;

    @FXML private Label lblTotalUniversites;
    
    @FXML private Label lblModules;
    @FXML private Label lblFilieres;

    // Utilisateurs Actifs
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;
    @FXML private Label lblTotalActifs;
    @FXML private Label lblEtuStat;
    @FXML private Label lblProfStat;
    @FXML private Label lblMedStat;

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_universitaire", "root", "");
        } catch (SQLException e) { }
        return conn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStatistics();
    }

    private void loadStatistics() {
        int etudiants = 0, profs = 0, medecins = 0, totalUsers = 0, actifs = 0, inactifs = 0;
        int univs = 0;

        try (Connection conn = getConnection()) {
            if (conn != null) {
                // Users
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT role, statut FROM utilisateur");
                while(rs.next()) {
                    totalUsers++;
                    String r = rs.getString("role") != null ? rs.getString("role").toLowerCase() : "";
                    String s = rs.getString("statut") != null ? rs.getString("statut").toLowerCase() : "";
                    
                    if (r.contains("etudiant")) etudiants++;
                    if (r.contains("prof")) profs++;
                    if (r.contains("medecin") || r.contains("méd")) medecins++;
                    
                    if (s.equals("actif")) actifs++;
                    else inactifs++;
                }
                
                // Universities
                ResultSet rs2 = st.executeQuery("SELECT count(*) FROM universite");
                if(rs2.next()) {
                    univs = rs2.getInt(1);
                }
            }
        } catch (Exception e) {}

        lblTotalUtilisateurs.setText(String.valueOf(totalUsers));
        lblEtudiantsList.setText(etudiants + " Étudiants");
        lblProfsList.setText(profs + " Professeurs");
        lblMedecinsList.setText(medecins + " Médecins");

        lblEtuStat.setText(String.valueOf(etudiants));
        lblProfStat.setText(String.valueOf(profs));
        lblMedStat.setText(String.valueOf(medecins));

        lblActifs.setText(String.valueOf(actifs));
        lblInactifs.setText(String.valueOf(inactifs));
        lblTotalActifs.setText(String.valueOf(totalUsers));

        lblTotalUniversites.setText(String.valueOf(univs));
        
        // Mocks for modules/filieres as requested
        lblModules.setText("2");
        lblFilieres.setText("4");
    }

    @FXML
    private void goToUniversites(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/universite.fxml"));
            lblTotalUtilisateurs.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToUtilisateurs(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/utilisateur.fxml"));
            lblTotalUtilisateurs.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
