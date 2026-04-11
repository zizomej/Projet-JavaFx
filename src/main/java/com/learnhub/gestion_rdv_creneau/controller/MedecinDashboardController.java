package com.learnhub.gestion_rdv_creneau.controller;

import com.learnhub.gestion_rdv_creneau.entity.RDV;
import com.learnhub.gestion_rdv_creneau.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.sql.*;
import java.time.LocalDate;

public class MedecinDashboardController {

    @FXML private Label lblPendingCount;
    @FXML private Label lblConfirmedCount;
    @FXML private Label lblTodayCount;
    @FXML private Label lblMonthCount;

    @FXML private TableView<RDV> tablePendingRDV;
    @FXML private TableColumn<RDV, String> colMotif;
    @FXML private TableColumn<RDV, String> colDescription;
    @FXML private TableColumn<RDV, LocalDate> colDate;
    @FXML private TableColumn<RDV, Void> colActions;

    private ObservableList<RDV> pendingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));

        addActionsToTable();
        refreshData();
    }

    @FXML
    public void refreshData() {
        loadStats();
        loadPendingRequests();
    }

    private void loadStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Pending
            lblPendingCount.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM rdv WHERE statut = 'En attente'")));
            // Confirmed
            lblConfirmedCount.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM rdv WHERE statut IN ('Confirmé', 'Accepté')")));
            // Today
            lblTodayCount.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM rdv WHERE date_demande = CURRENT_DATE")));
            // This Month
            lblMonthCount.setText(String.valueOf(getCount(conn, "SELECT COUNT(*) FROM rdv WHERE MONTH(date_demande) = MONTH(CURRENT_DATE) AND YEAR(date_demande) = YEAR(CURRENT_DATE)")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCount(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private void loadPendingRequests() {
        pendingList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rdv WHERE statut = 'En attente'")) {

            while (rs.next()) {
                Date d = rs.getDate("date_demande");
                pendingList.add(new RDV(
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
            tablePendingRDV.setItems(pendingList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addActionsToTable() {
        Callback<TableColumn<RDV, Void>, TableCell<RDV, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<RDV, Void> call(final TableColumn<RDV, Void> param) {
                final TableCell<RDV, Void> cell = new TableCell<>() {
                    private final Button btnAccept = new Button("Accepter");
                    private final Button btnRefuse = new Button("Refuser");
                    private final HBox pane = new HBox(btnAccept, btnRefuse);

                    {
                        btnAccept.getStyleClass().add("btn-primary");
                        btnAccept.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #059669;");
                        btnAccept.setOnAction(event -> {
                            RDV rdv = getTableView().getItems().get(getIndex());
                            updateStatus(rdv.getId(), "Confirmé");
                        });

                        btnRefuse.getStyleClass().add("btn-orange");
                        btnRefuse.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                        btnRefuse.setOnAction(event -> {
                            RDV rdv = getTableView().getItems().get(getIndex());
                            updateStatus(rdv.getId(), "Annulé");
                        });
                        
                        pane.setSpacing(10);
                        pane.setAlignment(javafx.geometry.Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
                return cell;
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void updateStatus(int id, String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE rdv SET statut = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



