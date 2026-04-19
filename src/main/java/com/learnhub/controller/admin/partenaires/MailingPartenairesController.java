package com.learnhub.controller.admin.partenaires;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.MailMessage;
import com.learnhub.models.Partenaire;
import com.learnhub.util.GmailApiClient;
import com.learnhub.util.GmailOAuthService;
import com.learnhub.util.MailHistory;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MailingPartenairesController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Label  lblSidebarEmail;
    @FXML private Label  lblStatus;
    @FXML private Label  lblConnectedEmail;
    @FXML private Button btnConnect;
    @FXML private Button btnDisconnect;
    @FXML private VBox   paneNotConnected;
    @FXML private VBox   paneConnected;

    @FXML private ListView<Partenaire>   partenaireListView;
    @FXML private TextField              partenaireSearchField;
    @FXML private Label                  lblPartenaireCount;

    @FXML private TabPane                mailTabPane;
    @FXML private TableView<MailMessage> inboxTable;
    @FXML private TableView<MailMessage> sentTable;

    @FXML private Label     lblFromEmail;
    @FXML private TextField toField;
    @FXML private TextField subjectField;
    @FXML private TextArea  bodyArea;

    // ── State ─────────────────────────────────────────────────────────────────
    private final PartenaireDAO     partenaireDAO = new PartenaireDAO();
    private final GmailOAuthService oauth         = GmailOAuthService.getInstance();
    private       GmailApiClient    gmailClient;

    private final ObservableList<Partenaire>  allPartenaires = FXCollections.observableArrayList();
    private final ObservableList<Partenaire>  filteredList   = FXCollections.observableArrayList();
    private final ObservableList<MailMessage> inboxData      = FXCollections.observableArrayList();
    private final ObservableList<MailMessage> sentData       = FXCollections.observableArrayList();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // ── Init ──────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        gmailClient = new GmailApiClient(oauth);

        var user = SessionManager.getInstance().getCurrentUser();
        String email = (user != null && user.getEmail() != null) ? user.getEmail() : "";
        if (lblSidebarEmail != null)
            lblSidebarEmail.setText(email.isBlank() ? "admin@learnhub.tn" : email);

        setupPartenaireList();
        setupInboxTable();
        setupSentTable();
        loadPartenaires();

        if (partenaireSearchField != null)
            partenaireSearchField.textProperty().addListener((obs, o, q) -> filterPartenaires(q));

        if (oauth.isAuthenticated()) {
            updateConnectionState(true);
            refreshInboxAsync();
        } else {
            updateConnectionState(false);
        }
    }

    // ── OAuth2 ────────────────────────────────────────────────────────────────

    @FXML
    private void handleConnect() {
        showStatus("Ouverture de Google dans votre navigateur...", "#1e40af");
        if (btnConnect != null) {
            btnConnect.setDisable(true);
            btnConnect.setText("En attente...");
        }
        oauth.startOAuthFlow(success -> {
            if (success) {
                updateConnectionState(true);
                showStatus("Connecte a Gmail - chargement...", "#059669");
                refreshInboxAsync();
            } else {
                Platform.runLater(() -> {
                    if (btnConnect != null) {
                        btnConnect.setDisable(false);
                        btnConnect.setText("Connecter Gmail");
                    }
                });
                showStatus("Connexion echouee.", "#dc2626");
            }
        });
    }

    @FXML
    private void handleDisconnect() {
        oauth.logout();
        updateConnectionState(false);
        inboxData.clear();
        showStatus("Deconnecte de Gmail.", "#64748b");
    }

    @FXML
    private void handleRefreshInbox() {
        if (!oauth.isAuthenticated()) {
            showStatus("Connectez-vous a Gmail.", "#d97706");
            return;
        }
        refreshInboxAsync();
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleSendEmail() {
        if (!oauth.isAuthenticated()) {
            showStatus("Connectez-vous a Gmail avant d'envoyer.", "#d97706");
            return;
        }
        String to      = toField      != null ? toField.getText().trim()      : "";
        String subject = subjectField != null ? subjectField.getText().trim()  : "";
        String body    = bodyArea     != null ? bodyArea.getText().trim()      : "";

        if (to.isEmpty())      { showStatus("Le destinataire est obligatoire.", "#dc2626"); return; }
        if (subject.isEmpty()) { showStatus("L'objet est obligatoire.", "#dc2626"); return; }
        if (body.isEmpty())    { showStatus("Le corps du message est vide.", "#dc2626"); return; }

        showStatus("Envoi en cours via Gmail API...", "#1e40af");

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception {
                return gmailClient.sendEmail(to, subject, body);
            }
        };
        task.setOnSucceeded(e -> {
            String now = LocalDateTime.now().format(FMT);
            String id  = task.getValue();
            sentData.add(0, new MailMessage(oauth.getUserEmail(), to, subject, body, now, false, id));
            MailHistory.getInstance().addSent(
                    new MailMessage(oauth.getUserEmail(), to, subject, body, now, false));
            if (mailTabPane != null && mailTabPane.getTabs().size() > 1)
                mailTabPane.getSelectionModel().select(1);
            clearCompose();
            showStatus("Email envoye a " + to, "#059669");
        });
        task.setOnFailed(e ->
                showStatus("Erreur : " + task.getException().getMessage(), "#dc2626"));
        new Thread(task, "gmail-send").start();
    }

    @FXML private void handleClearCompose() { clearCompose(); }

    // ── Inbox loading ─────────────────────────────────────────────────────────

    private void refreshInboxAsync() {
        showStatus("Chargement de la boite Gmail...", "#1e40af");
        Task<List<MailMessage>> task = new Task<>() {
            @Override protected List<MailMessage> call() throws Exception {
                return gmailClient.fetchInbox(30);
            }
        };
        task.setOnSucceeded(e -> {
            inboxData.setAll(task.getValue());
            showStatus(task.getValue().size() + " message(s) - " + oauth.getUserEmail(), "#059669");
        });
        task.setOnFailed(e ->
                showStatus("Erreur chargement : " + task.getException().getMessage(), "#dc2626"));
        new Thread(task, "gmail-inbox").start();
    }

    // ── Table setup ───────────────────────────────────────────────────────────

    private void setupInboxTable() {
        if (inboxTable == null) return;
        inboxTable.setItems(inboxData);
        inboxTable.setPlaceholder(new Label("Connectez-vous a Gmail pour voir vos messages reels."));

        TableColumn<MailMessage, String> c1 = new TableColumn<>("De");
        c1.setCellValueFactory(c -> new ReadOnlyStringWrapper(shorten(c.getValue().getFrom(), 35)));
        c1.setPrefWidth(210);

        TableColumn<MailMessage, String> c2 = new TableColumn<>("Objet");
        c2.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getSubject()));
        c2.setPrefWidth(290);

        TableColumn<MailMessage, String> c3 = new TableColumn<>("Apercu");
        c3.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getBodyPreview()));
        c3.prefWidthProperty().bind(inboxTable.widthProperty().subtract(590));

        TableColumn<MailMessage, String> c4 = new TableColumn<>("Date");
        c4.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDate()));
        c4.setPrefWidth(85);

        inboxTable.getColumns().setAll(c1, c2, c3, c4);

        // Double-clic -> repondre
        inboxTable.setRowFactory(tv -> {
            TableRow<MailMessage> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    MailMessage m = row.getItem();
                    if (toField      != null) toField.setText(m.getFrom());
                    if (subjectField != null) subjectField.setText("Re: " + m.getSubject());
                    if (bodyArea     != null)
                        bodyArea.setText("\n\n--- Message original ---\n" + m.getBody());
                    showStatus("Repondre a : " + m.getFrom(), "#1e40af");
                }
            });
            return row;
        });
    }

    private void setupSentTable() {
        if (sentTable == null) return;
        sentTable.setItems(sentData);
        sentTable.setPlaceholder(new Label("Aucun email envoye dans cette session."));

        TableColumn<MailMessage, String> c1 = new TableColumn<>("A");
        c1.setCellValueFactory(c -> new ReadOnlyStringWrapper(shorten(c.getValue().getTo(), 35)));
        c1.setPrefWidth(210);

        TableColumn<MailMessage, String> c2 = new TableColumn<>("Objet");
        c2.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getSubject()));
        c2.setPrefWidth(290);

        TableColumn<MailMessage, String> c3 = new TableColumn<>("Apercu");
        c3.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getBodyPreview()));
        c3.prefWidthProperty().bind(sentTable.widthProperty().subtract(590));

        TableColumn<MailMessage, String> c4 = new TableColumn<>("Date");
        c4.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDate()));
        c4.setPrefWidth(85);

        sentTable.getColumns().setAll(c1, c2, c3, c4);
    }

    // ── Partenaires ───────────────────────────────────────────────────────────

    private void setupPartenaireList() {
        if (partenaireListView == null) return;
        partenaireListView.setItems(filteredList);
        partenaireListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Partenaire p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); setStyle(""); return; }
                String em = (p.getEmail() == null || p.getEmail().isBlank())
                        ? "(pas d'email)" : p.getEmail();
                setText(p.getNom() + "\n" + em);
                setStyle("-fx-padding: 7 10; -fx-font-size: 12px;");
            }
        });
        partenaireListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, p) -> {
                    if (p == null) return;
                    String em = (p.getEmail() == null || p.getEmail().isBlank()) ? "" : p.getEmail();
                    if (toField != null) toField.setText(em);
                    showStatus("Destinataire : " + p.getNom(), "#1e40af");
                });
    }

    private void loadPartenaires() {
        try {
            List<Partenaire> list = partenaireDAO.findAll();
            allPartenaires.setAll(list);
            filteredList.setAll(list);
            if (lblPartenaireCount != null)
                lblPartenaireCount.setText(list.size() + " partenaire(s)");
        } catch (SQLException e) {
            showStatus("Chargement partenaires : " + e.getMessage(), "#d97706");
        }
    }

    private void filterPartenaires(String query) {
        if (query == null || query.isBlank()) { filteredList.setAll(allPartenaires); return; }
        String q = query.toLowerCase().trim();
        filteredList.setAll(allPartenaires.filtered(p ->
                (p.getNom()   != null && p.getNom().toLowerCase().contains(q)) ||
                (p.getEmail() != null && p.getEmail().toLowerCase().contains(q))));
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void updateConnectionState(boolean connected) {
        Platform.runLater(() -> {
            if (paneNotConnected != null) {
                paneNotConnected.setVisible(!connected);
                paneNotConnected.setManaged(!connected);
            }
            if (paneConnected != null) {
                paneConnected.setVisible(connected);
                paneConnected.setManaged(connected);
            }
            if (btnConnect != null) {
                btnConnect.setDisable(false);
                btnConnect.setText("Connecter Gmail");
            }
            if (connected && oauth.getUserEmail() != null) {
                if (lblConnectedEmail != null) lblConnectedEmail.setText(oauth.getUserEmail());
                if (lblFromEmail      != null) lblFromEmail.setText(oauth.getUserEmail());
                if (lblSidebarEmail   != null) lblSidebarEmail.setText(oauth.getUserEmail());
            }
        });
    }

    private void showStatus(String msg, String hexColor) {
        Platform.runLater(() -> {
            if (lblStatus == null) return;
            lblStatus.setText(msg);
            lblStatus.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + hexColor + ";");
        });
    }

    private void clearCompose() {
        if (toField      != null) toField.clear();
        if (subjectField != null) subjectField.clear();
        if (bodyArea     != null) bodyArea.clear();
        if (partenaireListView != null) partenaireListView.getSelectionModel().clearSelection();
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "...";
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void goDashboard()     { navigate("/fxml/admin/dashboard.fxml",                           "Tableau de bord"); }
    @FXML private void goUtilisateurs()  { navigate("/fxml/admin/utilisateurs.fxml",                        "Utilisateurs"); }
    @FXML private void goModules()       { navigate("/fxml/admin/modules.fxml",                              "Modules"); }
    @FXML private void goSeances()       { navigate("/fxml/admin/seances.fxml",                              "Seances"); }
    @FXML private void goNotes()         { navigate("/fxml/admin/notes.fxml",                                "Notes"); }
    @FXML private void goPresences()     { navigate("/fxml/admin/presences.fxml",                            "Presences"); }
    @FXML private void goFilieres()      { navigate("/fxml/admin/filieres.fxml",                             "Filieres"); }
    @FXML private void goEvenements()    { navigate("/fxml/admin/evenements.fxml",                           "Evenements"); }
    @FXML private void goRdv()           { navigate("/fxml/admin/rdv.fxml",                                  "RDV Medicaux"); }
    @FXML private void goCreneaux()      { navigate("/fxml/admin/creneaux.fxml",                             "Creneaux"); }
    @FXML private void goPartenaires()   { navigate("/fxml/admin/partenaires/partenaires.fxml",              "Partenaires"); }
    @FXML private void goOffresStage()   { navigate("/fxml/admin/offrestage/offres_stage.fxml",              "Offres de Stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/admin/demandestage/demandes_stage.fxml",          "Demandes de Stage"); }
    @FXML private void goMailing()       { navigate("/fxml/admin/partenaires/mailing_partenaires.fxml",      "Mailing"); }

    @FXML
    private void handleLogout() {
        oauth.logout();
        SessionManager.getInstance().logout();
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateToFixed(stage, "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void navigate(String fxml, String title) {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, fxml, title);
    }

    private Stage getStage() {
        if (partenaireListView != null && partenaireListView.getScene() != null)
            return (Stage) partenaireListView.getScene().getWindow();
        if (inboxTable != null && inboxTable.getScene() != null)
            return (Stage) inboxTable.getScene().getWindow();
        if (lblStatus != null && lblStatus.getScene() != null)
            return (Stage) lblStatus.getScene().getWindow();
        return null;
    }
}
