package com.learnhub.medical.controller;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.util.List;

public class StudentDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblNextRDV;
    @FXML private Label lblTotalMyRDV;

    private final RDVRepository rdvRepo = new RDVRepository();

    @FXML
    public void initialize() {
        String name = SessionManager.getInstance().getCurrentUser().getFullName();
        lblWelcome.setText("Bienvenue, " + name + " !");

        try {
            int studentId = SessionManager.getInstance().getCurrentUserId();
            List<RDV> myRdvs = rdvRepo.findByStudentId(studentId);
            lblTotalMyRDV.setText(String.valueOf(myRdvs.size()));

            if (!myRdvs.isEmpty()) {
                RDV last = myRdvs.get(0);
                lblNextRDV.setText(last.getMotif() + " (" + last.getStatut() + ")");
            } else {
                lblNextRDV.setText("Aucun rendez-vous");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
