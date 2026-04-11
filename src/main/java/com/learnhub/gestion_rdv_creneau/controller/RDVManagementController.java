package com.learnhub.gestion_rdv_creneau.controller;

import com.learnhub.gestion_rdv_creneau.entity.RDV;
import com.learnhub.gestion_rdv_creneau.entity.Creneau;
import com.learnhub.gestion_rdv_creneau.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class RDVManagementController {

    @FXML private TableView<RDV> tableRDV;
    @FXML private TableColumn<RDV, Integer> colId;
    @FXML private TableColumn<RDV, String> colMotif;
    @FXML private TableColumn<RDV, LocalDate> colDate;
    @FXML private TableColumn<RDV, String> colStatut;
    @FXML private TableColumn<RDV, Integer> colCreneau;

    @FXML private TextField txtMotif;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<Creneau> comboCreneau;
    @FXML private TextArea txtCompteRendu;

    private ObservableList<RDV> rdvList = FXCollections.observableArrayList();
    private RDV selectedRDV = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCreneau.setCellValueFactory(new PropertyValueFactory<>("creneauId"));

        comboStatut.setItems(FXCollections.observableArrayList("En attente", "Confirmé", "Annulé", "Terminé"));
        
        loadCreneaux();
        loadRDVs();

        tableRDV.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedRDV = newSelection;
                txtMotif.setText(selectedRDV.getMotif());
                txtDescription.setText(selectedRDV.getDescription());
                datePicker.setValue(selectedRDV.getDateDemande());
                comboStatut.setValue(selectedRDV.getStatut());
                txtCompteRendu.setText(selectedRDV.getCompteRendu());
                
                // Select correponding creneau in combo
                for (Creneau c : comboCreneau.getItems()) {
                    if (c.getId() == selectedRDV.getCreneauId()) {
                        comboCreneau.setValue(c);
                        break;
                    }
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadRDVs();
        loadCreneaux();
    }

    private void loadCreneaux() {
        ObservableList<Creneau> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM creneau")) {
            while (rs.next()) {
                list.add(new Creneau(
                    rs.getInt("id"),
                    rs.getString("jour"),
                    rs.getTime("heure").toLocalTime(),
                    rs.getString("recurrence"),
                    rs.getBoolean("disponibilite")
                ));
            }
            comboCreneau.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRDVs() {
        rdvList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rdv")) {

            while (rs.next()) {
                Date d = rs.getDate("date_demande");
                rdvList.add(new RDV(
                    rs.getInt("id"),
                    rs.getString("motif"),
                    rs.getString("description"),
                    d != null ? d.toLocalDate() : null,
                    rs.getString("statut"),
                    rs.getString("compte_rendu"),
                    rs.getString("ordonnance_url"),
                    rs.getInt("etudiant_id"),
                    rs.getInt("creneau_id")
                ));
            }
            tableRDV.setItems(rdvList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les RDV: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (txtMotif.getText().isEmpty() || comboCreneau.getValue() == null) {
            showAlert("Validation", "Le motif et le créneau sont obligatoires");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (selectedRDV == null) {
                // Insert
                String sql = "INSERT INTO rdv (motif, description, date_demande, statut, compte_rendu, creneau_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, txtMotif.getText());
                pstmt.setString(2, txtDescription.getText());
                pstmt.setDate(3, Date.valueOf(datePicker.getValue()));
                pstmt.setString(4, comboStatut.getValue());
                pstmt.setString(5, txtCompteRendu.getText());
                pstmt.setInt(6, comboCreneau.getValue().getId());
                pstmt.executeUpdate();
            } else {
                // Update
                String sql = "UPDATE rdv SET motif = ?, description = ?, date_demande = ?, statut = ?, compte_rendu = ?, creneau_id = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, txtMotif.getText());
                pstmt.setString(2, txtDescription.getText());
                pstmt.setDate(3, Date.valueOf(datePicker.getValue()));
                pstmt.setString(4, comboStatut.getValue());
                pstmt.setString(5, txtCompteRendu.getText());
                pstmt.setInt(6, comboCreneau.getValue().getId());
                pstmt.setInt(7, selectedRDV.getId());
                pstmt.executeUpdate();
            }
            loadRDVs();
            handleClear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedRDV == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM rdv WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedRDV.getId());
            pstmt.executeUpdate();
            loadRDVs();
            handleClear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        selectedRDV = null;
        txtMotif.clear();
        txtDescription.clear();
        datePicker.setValue(null);
        comboStatut.setValue(null);
        comboCreneau.setValue(null);
        txtCompteRendu.clear();
        tableRDV.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}



