package com.learnhub.controller.parent;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.PresenceDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.BulletinRow;
import com.learnhub.models.Module;
import com.learnhub.models.Note;
import com.learnhub.models.Presence;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.PDFGenerator;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentDashboardController {

    // Top Section
    @FXML private Label topParentNameLabel;
    @FXML private Label topParentInitialLabel;

    // Profil Enfant
    @FXML private Label enfantInitialLabel;
    @FXML private Label enfantNomLabel;
    @FXML private Label enfantFiliereLabel;
    @FXML private Label enfantEmailLabel;
    @FXML private Label moyenneLabel;
    @FXML private Label presenceTauxLabel;

    // Stats Cards
    @FXML private Label modulesCountLabel;
    @FXML private Label creditsAcquisLabel;
    @FXML private Label meilleureNoteLabel;
    @FXML private Label totalNotesLabel;

    // Table
    @FXML private Label moyenneTopRightLabel;
    @FXML private TableView<BulletinRow> releveTable;
    @FXML private TableColumn<BulletinRow, String> colCode;
    @FXML private TableColumn<BulletinRow, String> colModule;
    @FXML private TableColumn<BulletinRow, String> colCC;
    @FXML private TableColumn<BulletinRow, String> colTP;
    @FXML private TableColumn<BulletinRow, String> colExamen;
    @FXML private TableColumn<BulletinRow, String> colMoyenne;
    @FXML private TableColumn<BulletinRow, Integer> colCredits;
    @FXML private TableColumn<BulletinRow, String> colResultat;

    // Table Footer
    @FXML private Label tableMoyenneLabel;
    @FXML private Label tableCreditsLabel;

    // Informations Importantes
    @FXML private Label infoCinLabel;
    @FXML private Label infoModulesLabel;
    @FXML private Label infoCreditsLabel;
    @FXML private Label infoPerformanceLabel;
    @FXML private Label infoStatutLabel;

    // DAOs
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final NoteDAO noteDAO = new NoteDAO();
    private final PresenceDAO presenceDAO = new PresenceDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();

    private Utilisateur currentEnfant;
    private List<BulletinRow> currentBulletin;
    private double currentMoyenneGenerale = 0;
    private int currentCreditsAcquis = 0;
    private int totalCreditsPossibles = 0;
    private double bestNote = 0;
    private int totalNotesCount = 0;

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            topParentNameLabel.setText("Parent de " + currentUser.getNom());
            topParentInitialLabel.setText(currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty() 
                ? currentUser.getPrenom().substring(0, 1).toUpperCase() : "P");
        }

        setupTable();
        loadData();
    }

    private void setupTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("moduleCode"));
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleIntitule"));
        colCC.setCellValueFactory(new PropertyValueFactory<>("noteCCStr"));
        colTP.setCellValueFactory(new PropertyValueFactory<>("noteTPStr"));
        colExamen.setCellValueFactory(new PropertyValueFactory<>("noteExamenStr"));
        colMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenneStr"));
        colCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        colResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        
        // Custom styling for bad results
        colResultat.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<BulletinRow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("X")) {
                            setStyle("-fx-text-fill: #111827; -fx-alignment: CENTER;");
                        } else {
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        }
                    }
                }
            };
        });
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        try {
            List<Utilisateur> etudiants = utilisateurDAO.findByRole("etudiant");
            if (etudiants.isEmpty()) return;
            
            // Simulation : take the first student as the child
            currentEnfant = etudiants.get(0);
            
            // Profil config
            String prenomNom = (currentEnfant.getPrenom() == null ? "" : currentEnfant.getPrenom()) + " " + 
                               (currentEnfant.getNom() == null ? "" : currentEnfant.getNom());
            enfantNomLabel.setText(prenomNom.trim());
            
            String init1 = currentEnfant.getPrenom() != null && !currentEnfant.getPrenom().isEmpty() ? currentEnfant.getPrenom().substring(0,1).toUpperCase() : "";
            String init2 = currentEnfant.getNom() != null && !currentEnfant.getNom().isEmpty() ? currentEnfant.getNom().substring(0,1).toUpperCase() : "";
            enfantInitialLabel.setText(init1 + init2);
            
            enfantEmailLabel.setText(currentEnfant.getEmail());
            enfantFiliereLabel.setText("Étudiant"); // Normalement filiere_id via FiliereDAO
            infoCinLabel.setText(String.valueOf(currentEnfant.getId()));
            
            // Build Bulletin data aggregating Notes
            List<Note> allNotes = noteDAO.findByEtudiant(currentEnfant.getId());
            totalNotesCount = allNotes.size();
            totalNotesLabel.setText(String.valueOf(totalNotesCount));
            
            // Map by Module ID
            Map<Integer, BulletinRow> bulletinMap = new HashMap<>();
            bestNote = 0;
            
            for (Note n : allNotes) {
                if (n.getValeur() > bestNote) bestNote = n.getValeur();
                
                if (!bulletinMap.containsKey(n.getModuleId())) {
                    // Try to fetch real module info, else dummy
                    String modInt = n.getModuleIntitule() != null ? n.getModuleIntitule() : "Module " + n.getModuleId();
                    String code = "MOD" + String.format("%03d", n.getModuleId());
                    int creds = 4; // default
                    bulletinMap.put(n.getModuleId(), new BulletinRow(code, modInt, creds));
                }
                bulletinMap.get(n.getModuleId()).addNote(n);
            }
            
            // If we have ModuleDAO let's try to attach missing modules where student attends but has no notes yet
            List<Module> allModules = moduleDAO.findAll();
            for (Module m : allModules) {
                if (!bulletinMap.containsKey(m.getId())) {
                    bulletinMap.put(m.getId(), new BulletinRow(m.getCode(), m.getIntitule(), m.getCredits()));
                } else {
                    // update correct code/credits
                    BulletinRow row = bulletinMap.get(m.getId());
                    if (m.getCode() != null) row.setModuleCode(m.getCode());
                    row.setCredits(m.getCredits());
                }
            }

            currentBulletin = new ArrayList<>(bulletinMap.values());
            
            double sumMoyennes = 0;
            int countedModules = 0;
            currentCreditsAcquis = 0;
            totalCreditsPossibles = 0;
            
            for (BulletinRow row : currentBulletin) {
                row.calculerMoyenne();
                totalCreditsPossibles += row.getCredits();
                
                if (row.getMoyenneFinal() >= 0) {
                    sumMoyennes += row.getMoyenneFinal();
                    countedModules++;
                    if (row.getMoyenneFinal() >= 10) {
                        currentCreditsAcquis += row.getCredits();
                    }
                }
            }
            
            if (countedModules > 0) {
                currentMoyenneGenerale = sumMoyennes / countedModules;
            } else {
                currentMoyenneGenerale = 0;
            }
            
            // Setup UI from results
            releveTable.getItems().setAll(currentBulletin);
            
            // Stats updates
            String moyFormat = String.format("%.2f", currentMoyenneGenerale);
            moyenneLabel.setText(moyFormat + "/20");
            meilleureNoteLabel.setText(bestNote > 0 ? String.format("%.2f", bestNote) : "—");
            modulesCountLabel.setText(String.valueOf(currentBulletin.size()));
            creditsAcquisLabel.setText(String.valueOf(currentCreditsAcquis));
            
            tableMoyenneLabel.setText(moyFormat + "/20");
            moyenneTopRightLabel.setText("Moyenne générale : " + moyFormat + "/20");
            tableCreditsLabel.setText(String.valueOf(totalCreditsPossibles));
            
            // Taux Presence
            int pctPresence = presenceDAO.calculatePresencePercentage(currentEnfant.getId());
            presenceTauxLabel.setText(pctPresence + "%");
            
            // Info bas de page
            infoModulesLabel.setText(String.valueOf(currentBulletin.size()));
            infoCreditsLabel.setText(currentCreditsAcquis + " sur " + totalCreditsPossibles);
            infoPerformanceLabel.setText(bestNote > 0 ? String.format("%.2f / 20", bestNote) : "—");
            
            if (currentMoyenneGenerale >= 10) {
                infoStatutLabel.setText("✅ Bonne évolution");
                infoStatutLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else {
                infoStatutLabel.setText("⚠️ Nécessite un suivi");
                infoStatutLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDownloadPDF() {
        if (currentEnfant == null || currentBulletin == null || currentBulletin.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Aucune donnée de carnet de notes disponible pour cet étudiant.");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Bulletin PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Bulletin_" + currentEnfant.getNom() + "_" + currentEnfant.getPrenom() + ".pdf");
        
        Stage stage = (Stage) topParentNameLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                PDFGenerator generator = new PDFGenerator();
                boolean success = generator.genererBulletinPdf(file, currentEnfant, currentBulletin, currentMoyenneGenerale, currentCreditsAcquis, bestNote, 3 /*rang simulé*/);
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Le bulletin PDF a été généré avec succès !");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Une erreur est survenue lors de la génération du PDF.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur système: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) topParentNameLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}
