package com.learnhub.controller.parent;

import com.learnhub.dao.NoteDAO;
import com.learnhub.dao.RdvDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ParentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label enfantNomLabel;
    @FXML private Label moyenneLabel;
    @FXML private Label absencesLabel;
    @FXML private Label rdvLabel;

    @FXML private TableView<com.learnhub.models.Note> notesTable;
    @FXML private TableView<com.learnhub.models.Rdv> rdvTable;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final NoteDAO noteDAO = new NoteDAO();
    private final RdvDAO rdvDAO = new RdvDAO();

    @FXML
    public void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("👋 Bienvenue, " + currentUser.getPrenom() + " " + currentUser.getNom());
        }

        setupNotesTable();
        setupRdvTable();
        loadData();
    }

    private void setupNotesTable() {
        TableColumn<com.learnhub.models.Note, String> colModule = new TableColumn<>("Module");
        colModule.setCellValueFactory(new PropertyValueFactory<>("moduleIntitule"));
        colModule.setPrefWidth(200);

        TableColumn<com.learnhub.models.Note, Double> colNote = new TableColumn<>("Note");
        colNote.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        colNote.setPrefWidth(100);

        TableColumn<com.learnhub.models.Note, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("typeNote"));
        colType.setPrefWidth(100);

        TableColumn<com.learnhub.models.Note, Double> colCoef = new TableColumn<>("Coefficient");
        colCoef.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        colCoef.setPrefWidth(100);

        notesTable.getColumns().addAll(colModule, colNote, colType, colCoef);
    }

    private void setupRdvTable() {
        TableColumn<com.learnhub.models.Rdv, String> colMotif = new TableColumn<>("Motif");
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colMotif.setPrefWidth(200);

        TableColumn<com.learnhub.models.Rdv, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colDate.setPrefWidth(150);

        TableColumn<com.learnhub.models.Rdv, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);

        rdvTable.getColumns().addAll(colMotif, colDate, colStatut);
    }

    private void loadData() {
        try {
            // Récupérer l'enfant lié au parent (filiere_id de l'étudiant)
            // Pour l'exemple, on prend le premier étudiant trouvé
            List<Utilisateur> etudiants = utilisateurDAO.findByRole("etudiant");
            if (!etudiants.isEmpty()) {
                Utilisateur enfant = etudiants.get(0);
                enfantNomLabel.setText(enfant.getPrenom() + " " + enfant.getNom());

                // Charger les notes de l'enfant
                notesTable.getItems().setAll(noteDAO.findByEtudiant(enfant.getId()));

                // Calculer la moyenne
                double moyenne = calculateMoyenne(notesTable.getItems());
                moyenneLabel.setText(String.format("%.2f / 20", moyenne));
            }

            // Charger les RDV
            rdvTable.getItems().setAll(rdvDAO.findAll());
            rdvLabel.setText(String.valueOf(rdvDAO.count()));

            absencesLabel.setText("0"); // À implémenter avec PresenceDAO

        } catch (SQLException e) {
            e.printStackTrace();
            moyenneLabel.setText("--");
            rdvLabel.setText("0");
        }
    }

    private double calculateMoyenne(List<com.learnhub.models.Note> notes) {
        if (notes.isEmpty()) return 0;
        double total = 0;
        double coefTotal = 0;
        for (com.learnhub.models.Note n : notes) {
            total += n.getValeur() * n.getCoefficient();
            coefTotal += n.getCoefficient();
        }
        return coefTotal > 0 ? total / coefTotal : 0;
    }

    @FXML
    private void goConsulterNotes() {
        // Navigation vers la page des notes
    }

    @FXML
    private void goSuiviAbsences() {
        // Navigation vers la page des absences
    }

    @FXML
    private void goRdvMedicaux() {
        // Navigation vers la page des RDV
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/fxml/auth/login.fxml", "Connexion");
    }
}
