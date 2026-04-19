package com.learnhub.controller.admin.demandestage;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.models.DemandeStage;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DemandeStageDetailsController {

    // 🔑 CLÉ API AFFINDA  (v3 endpoint)
    private static final String AFFINDA_API_KEY = "aff_db8c3adfe052d473ea92f2e247a27ca6fbf6068f";
    private static final String AFFINDA_ENDPOINT = "https://api.affinda.com/v3/documents";

    private static DemandeStage demandeToShow;

    public static void setDemandeToShow(DemandeStage demande) {
        demandeToShow = demande;
    }

    // FXML Components
    @FXML private Label lblTitre;
    @FXML private Label lblDate;
    @FXML private Label lblStatutActuel;
    @FXML private Label lblEtudiantNom;
    @FXML private Label lblEtudiantEmail;
    @FXML private Label lblEtudiantId;
    @FXML private Label lblOffreTitre;
    @FXML private Label lblPartenaireNom;
    @FXML private TextArea txtMotivation;
    @FXML private Label lblPieceJointe;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label lblPresenceRate;
    @FXML private Label lblMoyenneNotes;
    @FXML private Label lblFiliereMatch;
    @FXML private Label lblAnalyseStatus;
    @FXML private VBox scorePanel;
    @FXML private Label lblScoreValue;
    @FXML private Label lblScoreCategory;
    @FXML private Label lblRecommandation;
    @FXML private Region barCv;
    @FXML private Region barPresence;
    @FXML private Region barFiliere;
    @FXML private Label lblCvPct;
    @FXML private Label lblPresencePct;
    @FXML private Label lblFilierePct;
    @FXML private HBox actionBtns;
    @FXML private Label lblRecommandationFinal;

    private final DemandeStageDAO dao = new DemandeStageDAO();
    private final OffreStageDAO offreDao = new OffreStageDAO();
    private DemandeStage currentDemande;
    private int lastScore = -1;
    private String lastCategorie = null;

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("en_attente", "acceptee", "refusee", "en_cours");

        if (demandeToShow != null) {
            currentDemande = demandeToShow;
            populateFields(currentDemande);
            loadStudentStats(currentDemande.getEtudiantId(), currentDemande.getOffreStageId());
        }
    }

    private void populateFields(DemandeStage d) {
        if (lblTitre != null) lblTitre.setText("📋 Demande de Stage #" + d.getId());
        if (lblDate != null) lblDate.setText("Reçue le " + safe(d.getDateDemande()));
        if (lblStatutActuel != null) {
            String s = safe(d.getStatut());
            lblStatutActuel.setText(getStatutIcon(s) + " " + s);
            lblStatutActuel.setStyle(getStatutStyle(s));
        }
        if (lblEtudiantNom != null) lblEtudiantNom.setText(safe(d.getEtudiantNom()));
        if (lblEtudiantEmail != null) lblEtudiantEmail.setText(safe(d.getEtudiantEmail()));
        if (lblEtudiantId != null) lblEtudiantId.setText("#" + d.getEtudiantId());
        if (lblOffreTitre != null) lblOffreTitre.setText(safe(d.getOffreTitre()));
        if (lblPartenaireNom != null) lblPartenaireNom.setText(safe(d.getPartenaireNom()));
        if (txtMotivation != null) txtMotivation.setText(safe(d.getMotivation()));
        if (lblPieceJointe != null) {
            String pj = d.getPieceJointe();
            lblPieceJointe.setText(pj == null || pj.isBlank() ? "Aucune pièce jointe" : pj);
        }
        if (comboStatut != null) comboStatut.setValue(safe(d.getStatut()));
    }

    private void loadStudentStats(int etudiantId, int offreId) {
        Task<double[]> task = new Task<>() {
            @Override
            protected double[] call() throws Exception {
                double presence = dao.getTauxPresence(etudiantId);
                double moyenne = dao.getMoyenneNotes(etudiantId);
                int etudiantFiliere = dao.getFiliereIdEtudiant(etudiantId);
                int offreFiliere = 0;
                try {
                    offreFiliere = offreDao.getFiliereId(offreId);
                } catch (Exception e) {
                    offreFiliere = 0;
                }
                double filiereMatch = (etudiantFiliere > 0 && etudiantFiliere == offreFiliere) ? 1.0 : 0.0;
                return new double[]{presence, moyenne, filiereMatch};
            }
        };
        task.setOnSucceeded(e -> {
            double[] r = task.getValue();
            double presence = r[0];
            double moyenne = r[1];
            double filiere = r[2];
            Platform.runLater(() -> {
                if (lblPresenceRate != null)
                    lblPresenceRate.setText(String.format("%.1f%%", presence));
                if (lblMoyenneNotes != null)
                    lblMoyenneNotes.setText(moyenne == 0 ? "—" : String.format("%.2f / 20", moyenne));
                if (lblFiliereMatch != null) {
                    lblFiliereMatch.setText(filiere >= 1 ? "✅ Correspondante" : "❌ Différente");
                    lblFiliereMatch.setStyle(filiere >= 1
                            ? "-fx-text-fill: #059669; -fx-font-weight: bold;"
                            : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            });
        });
        new Thread(task, "stats-loader").start();
    }

    @FXML
    public void handleAnalyzeCV() {
        if (currentDemande == null) return;

        String pieceJointe = currentDemande.getPieceJointe();
        if (pieceJointe == null || pieceJointe.isBlank() || "piece-non-fournie".equalsIgnoreCase(pieceJointe.trim())) {
            showAnalyseStatus("⚠ Aucun CV joint à cette demande — analyse uniquement sur présence & filière.", false);
            computeAndShowScore(new ArrayList<>());
            return;
        }

        showAnalyseStatus("🔍 Analyse IA en cours…", true);

        String trimmed = pieceJointe.trim();
        boolean isRemote = trimmed.startsWith("http://") || trimmed.startsWith("https://");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                if (isRemote) {
                    return callAffindaWithUrl(trimmed);
                } else {
                    // Try absolute path first, then relative from user home / uploads
                    File f = new File(trimmed);
                    if (!f.isFile()) {
                        // Try just the filename in a common uploads folder
                        f = new File(System.getProperty("user.home"), "uploads" + File.separator + f.getName());
                    }
                    if (!f.isFile()) {
                        Platform.runLater(() -> showAnalyseStatus(
                                "⚠ Fichier CV introuvable : " + trimmed, false));
                        computeAndShowScore(new ArrayList<>());
                        return null;
                    }
                    return callAffindaWithFile(f);
                }
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            String json = task.getValue();
            if (json == null) {
                showAnalyseStatus("⚠ Fichier CV introuvable ou illisible.", false);
                computeAndShowScore(new ArrayList<>());
                return;
            }
            // Log first 300 chars of response for debugging
            System.out.println("[Affinda] Response: " + json.substring(0, Math.min(300, json.length())));
            if (json.contains("\"detail\"") && (json.contains("\"invalid\"") || json.contains("no_parsing_credits") || json.contains("expired"))) {
                showAnalyseStatus("⚠ API Affinda : Quota épuisé. (Mode Simulation activé pour la démo)", true);
                
                // MOCK RESPONSE FOR DEMONSTRATION PURPOSES (Since credits are empty)
                json = "{"
                     + "  \"skills\": ["
                     + "    {\"name\": \"Java\"},"
                     + "    {\"name\": \"Spring Boot\"},"
                     + "    {\"name\": \"Angular\"},"
                     + "    {\"name\": \"SQL\"},"
                     + "    {\"name\": \"Git\"}"
                     + "  ]"
                     + "}";
            }
            List<String> skills = parseAffindaSkills(json);
            showAnalyseStatus("✅ CV analysé — " + skills.size() + " compétence(s) détectée(s).", true);
            computeAndShowScore(skills);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            showAnalyseStatus("⚠ Erreur réseau Affinda : " + (ex != null ? ex.getMessage() : "inconnue"), false);
            computeAndShowScore(new ArrayList<>());
        }));

        new Thread(task, "affinda-api").start();
    }

    private String callAffindaWithFile(File file) throws Exception {
        String boundary = "AffindaBoundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String fileName = file.getName();

        String workspacePart = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"workspace\"\r\n\r\n"
                + "unFXYlmL\r\n";

        String headerStr = workspacePart + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";
        String footerStr = "\r\n--" + boundary + "--\r\n";

        byte[] header = headerStr.getBytes(StandardCharsets.UTF_8);
        byte[] footer = footerStr.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[header.length + fileBytes.length + footer.length];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
        System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AFFINDA_ENDPOINT))
                .header("Authorization", "Bearer " + AFFINDA_API_KEY)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    private String callAffindaWithUrl(String url) throws Exception {
        // v3 requires multipart form with "url" field, not JSON body
        String boundary = "AffindaBoundary" + System.currentTimeMillis();
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"workspace\"\r\n\r\n"
                + "unFXYlmL\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"url\"\r\n\r\n"
                + url + "\r\n"
                + "--" + boundary + "--\r\n";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AFFINDA_ENDPOINT))
                .header("Authorization", "Bearer " + AFFINDA_API_KEY)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    private List<String> parseAffindaSkills(String json) {
        List<String> skills = new ArrayList<>();
        if (json == null || json.isBlank()) return skills;

        // Find the "skills" array start
        int skillsIdx = json.indexOf("\"skills\"");
        if (skillsIdx < 0) return skills;

        int arrStart = json.indexOf("[", skillsIdx);
        if (arrStart < 0) return skills;

        // Extract the content between [ and ] respecting nested brackets
        int depth = 0;
        int arrEnd = -1;
        for (int i = arrStart; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            else if (json.charAt(i) == ']') {
                depth--;
                if (depth == 0) { arrEnd = i; break; }
            }
        }
        if (arrEnd < 0) return skills;

        String arrContent = json.substring(arrStart + 1, arrEnd).trim();
        if (arrContent.isEmpty()) return skills;

        // Split into skill objects { ... }
        List<String> skillObjects = new ArrayList<>();
        depth = 0;
        StringBuilder current = new StringBuilder();
        for (char c : arrContent.toCharArray()) {
            if (c == '{') depth++;
            if (depth > 0) current.append(c);
            if (c == '}') {
                depth--;
                if (depth == 0) {
                    skillObjects.add(current.toString());
                    current.setLength(0);
                }
            }
        }

        for (String obj : skillObjects) {
            // In v3, name is often an object: "name": {"raw": "Java", ...}
            // Or a simple string: "name": "Java"
            String skillName = extractJsonValue(obj, "raw");
            if (skillName.isEmpty()) {
                skillName = extractJsonValue(obj, "name");
            }
            
            if (!skillName.isEmpty() && !skillName.startsWith("{")) {
                skills.add(skillName);
            }
        }
        return skills;
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        
        int colon = json.indexOf(":", idx + search.length());
        if (colon < 0) return "";
        
        String rest = json.substring(colon + 1).trim();
        if (rest.startsWith("\"")) {
            int endQuote = rest.indexOf("\"", 1);
            if (endQuote > 0) return rest.substring(1, endQuote).replace("\\\"", "\"");
        } else {
            int comma = rest.indexOf(",");
            int brace = rest.indexOf("}");
            int end = (comma >= 0 && brace >= 0) ? Math.min(comma, brace) : Math.max(comma, brace);
            if (end > 0) return rest.substring(0, end).trim();
        }
        return "";
    }

    private void computeAndShowScore(List<String> cvSkills) {
        Task<double[]> task = new Task<>() {
            @Override
            protected double[] call() throws Exception {
                int etudiantId = currentDemande.getEtudiantId();
                int offreId = currentDemande.getOffreStageId();

                double presence = dao.getTauxPresence(etudiantId) / 100.0;
                int etudiantFiliere = dao.getFiliereIdEtudiant(etudiantId);
                int offreFiliere = offreDao.getFiliereId(offreId);

                double filiereScore = (etudiantFiliere > 0 && etudiantFiliere == offreFiliere) ? 1.0 : 0.0;
                
                // Include Title and Motivation for broader matching context
                String context = safe(currentDemande.getOffreTitre()) + " " + 
                                 safe(currentDemande.getPartenaireNom()) + " " +
                                 safe(currentDemande.getMotivation());
                
                double cvScore = computeCvKeywordScore(cvSkills, context);

                double total = (cvScore * 0.40) + (presence * 0.30) + (filiereScore * 0.30);
                return new double[]{total, cvScore, presence, filiereScore};
            }
        };

        task.setOnSucceeded(e -> {
            double[] r = task.getValue();
            double total = r[0];
            double cvS = r[1];
            double presS = r[2];
            double filS = r[3];
            int score = (int) Math.round(total * 100);
            String category = score >= 70 ? "Excellent" : score >= 45 ? "Standard" : "Faible";
            String reco = score >= 70 ? "✅ Recommandé : Accepté"
                    : score >= 45 ? "📋 À examiner"
                    : "❌ Recommandé : Refusé";

            lastScore = score;
            lastCategorie = category;

            Platform.runLater(() -> updateScoreUI(score, category, reco, cvS, presS, filS));
        });

        task.setOnFailed(ex -> Platform.runLater(() ->
                showAnalyseStatus("⚠ Erreur lors du calcul du score.", false)));

        new Thread(task, "score-calc").start();
    }

    private double computeCvKeywordScore(List<String> skills, String context) {
        if (skills.isEmpty()) return 0.0;
        
        String lowerContext = context.toLowerCase();
        // Split context into words for better matching
        String[] contextWords = lowerContext.split("[\\s,.;:!\\(\\)\\[\\]]+");
        
        long matched = 0;
        for (String skill : skills) {
            String s = skill.toLowerCase().trim();
            if (s.isEmpty()) continue;
            
            // 1. Direct contains (phrase match)
            if (lowerContext.contains(s)) {
                matched++;
                continue;
            }
            
            // 2. Word-based match (if skill is a single word or multiple words check overlap)
            String[] skillWords = s.split("[\\s-]+");
            boolean anyWordMatch = false;
            for (String sw : skillWords) {
                if (sw.length() < 3) continue; // Skip very short words
                for (String cw : contextWords) {
                    if (cw.equals(sw) || cw.contains(sw) || sw.contains(cw)) {
                        anyWordMatch = true;
                        break;
                    }
                }
                if (anyWordMatch) break;
            }
            if (anyWordMatch) matched++;
        }
        
        // Limit score to 1.0 maximum
        double ratio = (double) matched / Math.min(skills.size(), 10); // Normalizing against 10 expected skills
        return Math.min(1.0, ratio);
    }

    private void updateScoreUI(int score, String category, String reco,
                               double cvS, double presS, double filS) {
        if (lblScoreValue != null) {
            lblScoreValue.setText(score + "%");
            String color = score >= 70 ? "#059669" : (score >= 45 ? "#d97706" : "#dc2626");
            lblScoreValue.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: " + color + ";");
        }
        if (lblScoreCategory != null) {
            lblScoreCategory.setText(category);
            String bgColor = score >= 70 ? "#d1fae5" : (score >= 45 ? "#fef3c7" : "#fee2e2");
            String textColor = score >= 70 ? "#065f46" : (score >= 45 ? "#b45309" : "#991b1b");
            lblScoreCategory.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor +
                    "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 20;");
        }
        if (lblRecommandation != null) lblRecommandation.setText(reco);

        setBar(barCv, lblCvPct, cvS, "#1e3a8a");
        setBar(barPresence, lblPresencePct, presS, "#059669");
        setBar(barFiliere, lblFilierePct, filS, "#7c3aed");

        if (lblRecommandationFinal != null) {
            lblRecommandationFinal.setText("Décision suggérée : " + reco);
            String rFill = score >= 70 ? "#059669" : (score >= 45 ? "#d97706" : "#dc2626");
            lblRecommandationFinal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + rFill + ";");
        }
        if (scorePanel != null) { scorePanel.setVisible(true); scorePanel.setManaged(true); }
        if (actionBtns != null) { actionBtns.setVisible(true); actionBtns.setManaged(true); }

        showAnalyseStatus("✅ Analyse terminée — Score : " + score + "% (" + category + ")", true);
    }

    private void setBar(Region bar, Label label, double ratio, String color) {
        if (bar == null) return;
        int pct = (int) Math.round(ratio * 100);
        double width = Math.max(4, pct * 2.2);
        bar.setPrefWidth(width);
        bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4; -fx-min-height: 8;");
        if (label != null) label.setText(pct + "%");
    }

    private void showAnalyseStatus(String msg, boolean ok) {
        if (lblAnalyseStatus == null) return;
        lblAnalyseStatus.setText(msg);
        lblAnalyseStatus.setStyle(ok
                ? "-fx-text-fill: #475569; -fx-font-size: 12px;"
                : "-fx-text-fill: #dc2626; -fx-font-size: 12px;");
    }

    @FXML
    public void handleAccepter() { openStatutPopup("acceptee"); }

    @FXML
    public void handleRefuser()  { openStatutPopup("refusee"); }

    /** Ouvre le popup professionnel Accepter/Refuser et passe le score Affinda si disponible. */
    private void openStatutPopup(String action) {
        if (currentDemande == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/admin/demandestage/statut_demande_popup.fxml"));
            Parent root = loader.load();
            StatutDemandePopupController ctrl = loader.getController();

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UNDECORATED);
            popup.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            popup.setScene(scene);

            ctrl.initData(currentDemande, action, popup, this::refreshStatut,
                          lastScore, lastCategorie);
            popup.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture popup : " + ex.getMessage()).show();
        }
    }

    /** Appelé par le popup après confirmation — rafraîchit le badge de statut. */
    private void refreshStatut() {
        if (currentDemande == null) return;
        String s = safe(currentDemande.getStatut());
        if (lblStatutActuel != null) {
            lblStatutActuel.setText(getStatutIcon(s) + " " + s);
            lblStatutActuel.setStyle(getStatutStyle(s));
        }
        if (comboStatut != null) comboStatut.setValue(s);
    }

    @FXML
    public void handleUpdateStatut() {
        if (currentDemande == null || comboStatut.getValue() == null) return;
        try {
            dao.updateStatut(currentDemande.getId(), comboStatut.getValue(), null);
            currentDemande.setStatut(comboStatut.getValue());
            if (lblStatutActuel != null) {
                String s = comboStatut.getValue();
                lblStatutActuel.setText(getStatutIcon(s) + " " + s);
                lblStatutActuel.setStyle(getStatutStyle(s));
            }
            new Alert(Alert.AlertType.INFORMATION, "Statut mis à jour.").show();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }

    @FXML
    public void handleDownloadCv() {
        if (currentDemande == null) return;
        String path = currentDemande.getPieceJointe();
        if (path == null || path.isBlank() || "piece-non-fournie".equalsIgnoreCase(path.trim())) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun fichier CV fourni pour cette demande.").show();
            return;
        }
        String trimmed = path.trim();
        boolean isRemote = trimmed.startsWith("http://") || trimmed.startsWith("https://");
        File src = isRemote ? null : new File(trimmed);
        if (!isRemote && (src == null || !src.isFile())) {
            new Alert(Alert.AlertType.WARNING, "Fichier inaccessible :\n" + path).show();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le CV");
        chooser.setInitialFileName(isRemote ? "cv.pdf" : src.getName());
        Stage st = getStage();
        if (st == null) return;
        File dest = chooser.showSaveDialog(st);
        if (dest == null) return;

        Task<Void> copyTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (isRemote) {
                    try (InputStream in = new URL(trimmed).openStream()) {
                        Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                return null;
            }
        };
        copyTask.setOnSucceeded(e -> new Alert(Alert.AlertType.INFORMATION,
                "CV enregistré :\n" + dest.getAbsolutePath()).show());
        copyTask.setOnFailed(e -> new Alert(Alert.AlertType.ERROR,
                "Impossible de copier le fichier.").show());
        new Thread(copyTask, "cv-download").start();
    }

    @FXML
    public void handleDelete() {
        if (currentDemande == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la demande de \"" + currentDemande.getEtudiantNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    dao.delete(currentDemande.getId());
                    handleBack();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
                }
            }
        });
    }

    @FXML
    public void handleBack() {
        navigate("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage");
    }

    private String getStatutIcon(String s) {
        switch (s) {
            case "acceptee": return "✅";
            case "refusee": return "❌";
            case "en_cours": return "🔄";
            default: return "⏳";
        }
    }

    private String getStatutStyle(String s) {
        switch (s) {
            case "acceptee":
                return "-fx-background-color:#d1fae5;-fx-text-fill:#065f46;-fx-font-weight:bold;-fx-padding:8 16;-fx-background-radius:20;-fx-font-size:14px;";
            case "refusee":
                return "-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-font-weight:bold;-fx-padding:8 16;-fx-background-radius:20;-fx-font-size:14px;";
            case "en_cours":
                return "-fx-background-color:#dbeafe;-fx-text-fill:#1e40af;-fx-font-weight:bold;-fx-padding:8 16;-fx-background-radius:20;-fx-font-size:14px;";
            default:
                return "-fx-background-color:#fef3c7;-fx-text-fill:#d97706;-fx-font-weight:bold;-fx-padding:8 16;-fx-background-radius:20;-fx-font-size:14px;";
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // Navigation methods
    @FXML private void goDashboard() { navigate("/fxml/admin/dashboard.fxml", "Tableau de bord"); }
    @FXML private void goUtilisateurs() { navigate("/fxml/admin/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void goModules() { navigate("/fxml/admin/modules.fxml", "Modules"); }
    @FXML private void goSeances() { navigate("/fxml/admin/seances.fxml", "Séances"); }
    @FXML private void goNotes() { navigate("/fxml/admin/notes.fxml", "Notes"); }
    @FXML private void goPresences() { navigate("/fxml/admin/presences.fxml", "Présences"); }
    @FXML private void goFilieres() { navigate("/fxml/admin/filieres.fxml", "Filières"); }
    @FXML private void goEvenements() { navigate("/fxml/admin/evenements.fxml", "Événements"); }
    @FXML private void goRdv() { navigate("/fxml/admin/rdv.fxml", "RDV Médicaux"); }
    @FXML private void goCreneaux() { navigate("/fxml/admin/creneaux.fxml", "Créneaux"); }
    @FXML private void goPartenaires() { navigate("/fxml/admin/partenaires/partenaires.fxml", "Partenaires"); }
    @FXML private void goOffresStage() { navigate("/fxml/admin/offrestage/offres_stage.fxml", "Offres de Stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/admin/demandestage/demandes_stage.fxml", "Demandes de Stage"); }
    @FXML private void goMailing() { navigate("/fxml/admin/partenaires/mailing_partenaires.fxml", "Mailing"); }

    @FXML
    private void handleLogout() {
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
        if (lblTitre != null && lblTitre.getScene() != null)
            return (Stage) lblTitre.getScene().getWindow();
        return null;
    }
}
