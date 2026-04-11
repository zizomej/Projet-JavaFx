package com.learnhub.controller.student;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Note;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentNotesController {

    @FXML private Label topUserName;
    @FXML private Label lblTotalNotes;
    @FXML private Label lblModules;
    @FXML private Label lblMoyenne;
    @FXML private Accordion notesAccordion;

    private final NoteDAO noteDAO = new NoteDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            topUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            loadNotes(currentUser.getId());
        }
    }

    private void loadNotes(int etudiantId) {
        notesAccordion.getPanes().clear();
        try {
            List<Note> notes = noteDAO.findByEtudiant(etudiantId);
            List<Module> allModules = moduleDAO.findAll();
            
            lblTotalNotes.setText(String.valueOf(notes.size()));
            
            // Group by module title since Note has moduleIntitule 
            // Better: group by Module if we have a way. Note.java has `moduleIntitule`.
            Map<String, List<Note>> notesByModule = notes.stream()
                    .filter(n -> n.getModuleIntitule() != null)
                    .collect(Collectors.groupingBy(Note::getModuleIntitule));

            lblModules.setText(String.valueOf(notesByModule.size()));

            double totalGlobal = 0;
            double coefGlobal = 0;

            for (Map.Entry<String, List<Note>> entry : notesByModule.entrySet()) {
                String moduleName = entry.getKey();
                List<Note> moduleNotes = entry.getValue();

                // Find the original Module object to get the code (optional, we might just use moduleName)
                String moduleBannerTitle = moduleName;
                for(Module m : allModules) {
                    if (m.getIntitule().equalsIgnoreCase(moduleName)) {
                        moduleBannerTitle = m.getCode() + " - " + m.getIntitule();
                        break;
                    }
                }

                double moduleSum = 0;
                double moduleCoef = 0;
                for (Note n : moduleNotes) {
                    moduleSum += n.getValeur() * n.getCoefficient();
                    moduleCoef += n.getCoefficient();
                    totalGlobal += n.getValeur() * n.getCoefficient();
                    coefGlobal += n.getCoefficient();
                }
                double moduleAvg = moduleCoef > 0 ? moduleSum / moduleCoef : 0.0;

                // Build Pivot Table UI
                VBox contentBox = buildPivotTable(moduleNotes, moduleAvg);

                TitledPane pane = new TitledPane();
                pane.setText("📖 " + moduleBannerTitle);
                pane.setContent(contentBox);
                notesAccordion.getPanes().add(pane);
            }

            double generalAvg = coefGlobal > 0 ? totalGlobal / coefGlobal : 0.0;
            lblMoyenne.setText(String.format(Locale.US, "%.2f", generalAvg));

            if (!notesAccordion.getPanes().isEmpty()) {
                notesAccordion.setExpandedPane(notesAccordion.getPanes().get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblTotalNotes.setText("0");
            lblModules.setText("0");
            lblMoyenne.setText("--");
        }
    }

    private VBox buildPivotTable(List<Note> notes, double moduleAvg) {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: white;");

        String[] cols = {"DS1", "DS2", "DS3", "TP1", "TP2", "TP3", "PROJET", "EXAMEN", "CC", "MOYENNE"};
        
        // Header Row
        HBox headerRow = new HBox();
        headerRow.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #e5e7eb transparent; -fx-padding: 15;");
        for (String col : cols) {
            Label lbl = new Label(col);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 10px;");
            lbl.setAlignment(Pos.CENTER);
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, Priority.ALWAYS);
            headerRow.getChildren().add(lbl);
        }

        // Value Row
        HBox valueRow = new HBox();
        valueRow.setStyle("-fx-padding: 15;");
        for (String col : cols) {
            Label lbl = new Label("-");
            lbl.setAlignment(Pos.CENTER);
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, Priority.ALWAYS);

            if (col.equals("MOYENNE")) {
                lbl.setText(String.format(Locale.US, "%.2f", moduleAvg));
                lbl.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 3 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                // Find note
                Note found = null;
                for (Note n : notes) {
                    if (n.getTypeNote() != null && n.getTypeNote().equalsIgnoreCase(col)) {
                        found = n;
                        break;
                    }
                }
                if (found != null) {
                    lbl.setText(String.format(Locale.US, "%.2f", found.getValeur()));
                    if (found.getValeur() >= 10) {
                        lbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 900; -fx-font-size: 11px;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 900; -fx-font-size: 11px;");
                    }
                } else {
                    lbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
                }
            }
            valueRow.getChildren().add(lbl);
        }

        container.getChildren().addAll(headerRow, valueRow);
        return container;
    }

    @FXML private void goDashboard() { navigate("/fxml/student/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goModules() { navigate("/fxml/student/modules.fxml", "Mes Modules"); }
    @FXML private void goPresences() { navigate("/fxml/student/presences.fxml", "Mes Présences"); }
    @FXML private void goEmploi() { navigate("/fxml/student/emploi.fxml", "Emploi du temps"); }
    @FXML private void goRdv() { navigate("/fxml/student/rdv.fxml", "Mes RDV"); }
    @FXML private void goStages() { navigate("/fxml/student/stages.fxml", "Stages"); }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) lblTotalNotes.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxml, title);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) lblTotalNotes.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}
