package com.learnhub.gestion_rdv_creneau.controller;

import com.learnhub.gestion_rdv_creneau.entity.RDV;
import com.learnhub.gestion_rdv_creneau.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class StudentAppointmentsController {

    @FXML private TableView<RDV> tableMyRDV;
    @FXML private TableColumn<RDV, LocalDate> colDate;
    @FXML private TableColumn<RDV, String> colMotif;
    @FXML private TableColumn<RDV, String> colDescription;
    @FXML private TableColumn<RDV, String> colStatut;
    @FXML private TableColumn<RDV, Integer> colCreneau;

    @FXML private Label lblTotalRDV;
    @FXML private Label lblConfirmedRDV;

    private ObservableList<RDV> myRDVList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCreneau.setCellValueFactory(new PropertyValueFactory<>("creneauId"));

        loadData();
    }

    @FXML
    public void loadData() {
        myRDVList.clear();
        int confirmed = 0;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM rdv WHERE etudiant_id = ? ORDER BY id DESC")) {
            
            pstmt.setInt(1, 1); // Mock student ID
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Date d = rs.getDate("date_demande");
                RDV rdv = new RDV(
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
                myRDVList.add(rdv);
                
                if ("Confirmé".equalsIgnoreCase(rdv.getStatut()) || "Accepté".equalsIgnoreCase(rdv.getStatut())) {
                    confirmed++;
                }
            }
            tableMyRDV.setItems(myRDVList);
            lblTotalRDV.setText(String.valueOf(myRDVList.size()));
            lblConfirmedRDV.setText(String.valueOf(confirmed));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



