package com.learnhub.controller.student;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;

public class AiAnalysisDialogController {

    private static final String GEMINI_API_KEY = "AIzaSyBkkKmmKQ6ecWjGVSQOxO2wPjDKvwPhUYY";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    // Désactiver les avertissements verbeux de PDFBox
    static {
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
    }

    @FXML private VBox uploadZone;
    @FXML private VBox loadingZone;
    @FXML private VBox resultZone;

    @FXML private TextArea txtResume;
    
    @FXML private VBox contentResume;
    @FXML private VBox contentVideo;
    @FXML private VBox contentPodcast;

    @FXML private ToggleButton btnTabResume;
    @FXML private ToggleButton btnTabVideo;
    @FXML private ToggleButton btnTabPodcast;

    @FXML private Label lblVideoTitle;

    @FXML private VBox hostAliceBox;
    @FXML private VBox hostBobBox;
    @FXML private Label podcastSubtitleLabel;
    @FXML private Button btnPlayPodcast;

    private JSONArray podcastScript;
    private String generatedVideoQuery = "";

    @FXML
    public void handleUploadClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un document à analyser");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.txt")
        );

        Stage stage = (Stage) uploadZone.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            startAnalysis(selectedFile);
        }
    }

    private void startAnalysis(File document) {
        // Transition to Loading
        uploadZone.setVisible(false);
        uploadZone.setManaged(false);
        loadingZone.setVisible(true);
        loadingZone.setManaged(true);

        Task<Void> analysisTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 1. Lire le document
                String docText = extractText(document);

                // Pour éviter d'exploser le quota de tokens Groq, on tronque si c'est énorme (ex: 10000 caractères)
                if (docText.length() > 10000) {
                    docText = docText.substring(0, 10000);
                }

                if (docText.trim().isEmpty()) {
                    throw new Exception("Le document est vide ou impossible à lire.");
                }

                // 2. Interroger Groq (Appel HTTP)
                String summary = fetchAIResponse(docText);

                // 3. Mettre à jour l'interface
                Platform.runLater(() -> displayResults(document.getName(), summary));
                return null;
            }
        };

        analysisTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable ex = analysisTask.getException();
                txtResume.setText("Erreur lors de l'analyse : " + (ex != null ? ex.getMessage() : "Erreur inconnue"));
                // Switch directly to result zone to show the error
                loadingZone.setVisible(false);
                loadingZone.setManaged(false);
                resultZone.setVisible(true);
                resultZone.setManaged(true);
            });
        });

        new Thread(analysisTask).start();
    }

    private String extractText(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if (name.endsWith(".txt")) {
            return Files.readString(file.toPath());
        }
        throw new Exception("Format non supporté pour l'extraction de texte.");
    }

    private String fetchAIResponse(String documentContent) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String systemPrompt = "Tu es un assistant IA pédagogique doté d'un duo de présentateurs (Alice et Bob). Ton but est d'analyser le cours fourni. Renvoie UNIQUEMENT un objet JSON valide (aucun bloc Markdown) avec cette structure exacte : { \"resume\": \"Résumé organisé avec des puces.\", \"motCleVideo\": \"Mots clés pertinents Youtube (ex: Tutoriel Java Spring)\", \"podcast\": [ { \"speaker\": \"Alice\", \"text\": \"Salut Bob !\" }, { \"speaker\": \"Bob\", \"text\": \"Bonjour !\" } ] }";

        JSONObject requestBody = new JSONObject();

        // System Instruction
        JSONObject systemText = new JSONObject().put("text", systemPrompt);
        JSONObject systemInstruction = new JSONObject().put("parts", new JSONArray().put(systemText));
        requestBody.put("system_instruction", systemInstruction);

        // User Content
        JSONObject userText = new JSONObject().put("text", "Voici le texte du cours :\n" + documentContent);
        JSONObject userContent = new JSONObject().put("role", "user").put("parts", new JSONArray().put(userText));
        requestBody.put("contents", new JSONArray().put(userContent));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            String aiAnswer = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            return aiAnswer;
        } else if (response.statusCode() == 403) {
            System.err.println(response.body());
            throw new Exception("La clé API configurée est invalide ou a été révoquée (403). Veuillez la mettre à jour.");
        } else {
            System.err.println(response.body());
            throw new Exception("Erreur de l'API (" + response.statusCode() + ")");
        }
    }

    private void displayResults(String fileName, String rawApiResponse) {
        loadingZone.setVisible(false);
        loadingZone.setManaged(false);
        resultZone.setVisible(true);
        resultZone.setManaged(true);

        try {
            int start = rawApiResponse.indexOf('{');
            int end = rawApiResponse.lastIndexOf('}');
            if (start != -1 && end != -1) {
                String jsonStr = rawApiResponse.substring(start, end + 1);
                JSONObject root = new JSONObject(jsonStr);
                
                String resumeText = root.optString("resume", "Aucun résumé généré.");
                txtResume.setText("=== Résumé ===\nDocument : " + fileName + "\n\n" + resumeText);
                
                generatedVideoQuery = root.optString("motCleVideo", fileName);
                lblVideoTitle.setText("Recherche YouTube : " + generatedVideoQuery);
                
                podcastScript = root.optJSONArray("podcast");
                if (podcastScript == null) podcastScript = new JSONArray();
                podcastSubtitleLabel.setText("Podcast de " + podcastScript.length() + " répliques prêt à être écouté !");
                
            } else {
                throw new Exception("JSON introuvable dans la réponse.");
            }
        } catch (Exception e) {
             txtResume.setText("Erreur d'analyse IA : " + e.getMessage() + "\n\nRéponse brute :\n" + rawApiResponse);
             lblVideoTitle.setText("Erreur");
             podcastScript = new JSONArray();
             podcastSubtitleLabel.setText("Erreur lors du parsing du podcast.");
        }
        
        showResumeTab(); // Onglet Résumé actif par défaut
    }

    @FXML
    public void showResumeTab() {
        btnTabResume.setSelected(true);
        updateTabStyles();
        contentResume.setVisible(true); contentResume.setManaged(true);
        contentVideo.setVisible(false); contentVideo.setManaged(false);
        contentPodcast.setVisible(false); contentPodcast.setManaged(false);
    }

    @FXML
    public void showVideoTab() {
        btnTabVideo.setSelected(true);
        updateTabStyles();
        contentResume.setVisible(false); contentResume.setManaged(false);
        contentVideo.setVisible(true); contentVideo.setManaged(true);
        contentPodcast.setVisible(false); contentPodcast.setManaged(false);
    }

    @FXML
    public void showPodcastTab() {
        btnTabPodcast.setSelected(true);
        updateTabStyles();
        contentResume.setVisible(false); contentResume.setManaged(false);
        contentVideo.setVisible(false); contentVideo.setManaged(false);
        contentPodcast.setVisible(true); contentPodcast.setManaged(true);
    }

    private void updateTabStyles() {
        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-color: #6366f1; -fx-border-width: 0 0 2 0; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-cursor: hand;";
        
        btnTabResume.setStyle(btnTabResume.isSelected() ? activeStyle : inactiveStyle);
        btnTabVideo.setStyle(btnTabVideo.isSelected() ? activeStyle : inactiveStyle);
        btnTabPodcast.setStyle(btnTabPodcast.isSelected() ? activeStyle : inactiveStyle);
    }

    @FXML
    public void playVideo() {
        if (generatedVideoQuery != null && !generatedVideoQuery.isEmpty()) {
            String queryUrl = "https://www.youtube.com/results?search_query=" + generatedVideoQuery.replace(" ", "+");
            try {
                java.awt.Desktop.getDesktop().browse(new URI(queryUrl));
            } catch (Exception e) {
                com.learnhub.util.DialogUtil.showSuccessMessage("Lien YouTube", "Requête : " + queryUrl);
            }
        }
    }

    @FXML
    public void resetDialog() {
        resultZone.setVisible(false);
        resultZone.setManaged(false);
        uploadZone.setVisible(true);
        uploadZone.setManaged(true);
        txtResume.clear();
        generatedVideoQuery = "";
        podcastScript = null;
    }

    private MediaPlayer currentPlayer; // Empêche le Garbage Collector de crasher le JVM

    @FXML
    public void downloadResume() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le résumé");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Texte", "*.txt"));
        File file = fileChooser.showSaveDialog(txtResume.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(txtResume.getText());
                com.learnhub.util.DialogUtil.showSuccessMessage("Succès", "Résumé sauvegardé avec succès !");
            } catch (Exception e) {
                com.learnhub.util.DialogUtil.showErrorMessage("Erreur", "Impossible de sauvegarder : " + e.getMessage());
            }
        }
    }

    @FXML
    public void playPodcast() {
        if (podcastScript == null || podcastScript.length() == 0) return;
        
        btnPlayPodcast.setDisable(true);
        
        Task<Void> playTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < podcastScript.length(); i++) {
                    JSONObject line = podcastScript.getJSONObject(i);
                    String speaker = line.has("speaker") ? line.getString("speaker") : "Alice";
                    String text = line.has("text") ? line.getString("text") : "";
                    
                    // 1. Télécharger le son localement d'abord pour éviter de faire crasher jfxmedia_dll (Code 0xC0000005)
                    File tempMp3 = File.createTempFile("tts_", ".mp3");
                    tempMp3.deleteOnExit();
                    try {
                        String urlEncodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
                        String ttsUrl = "https://translate.googleapis.com/translate_tts?ie=UTF-8&client=gtx&sl=fr&tl=fr&q=" + urlEncodedText;
                        
                        HttpRequest ttsReq = HttpRequest.newBuilder().uri(URI.create(ttsUrl)).GET().build();
                        HttpResponse<InputStream> ttsRes = HttpClient.newHttpClient().send(ttsReq, HttpResponse.BodyHandlers.ofInputStream());
                        Files.copy(ttsRes.body(), tempMp3.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CountDownLatch latch = new CountDownLatch(1);
                    
                    Platform.runLater(() -> {
                        hostAliceBox.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + (speaker.equalsIgnoreCase("Alice") ? "#a855f7" : "transparent") + "; -fx-border-width: 2;");
                        hostBobBox.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + (speaker.equalsIgnoreCase("Bob") ? "#3b82f6" : "transparent") + "; -fx-border-width: 2;");
                        podcastSubtitleLabel.setText("");
                        
                        // 2. TTS Audio Playback depuis le fichier local
                        try {
                            if (currentPlayer != null) {
                                currentPlayer.stop();
                                currentPlayer.dispose();
                            }
                            Media media = new Media(tempMp3.toURI().toString());
                            currentPlayer = new MediaPlayer(media); // Stocké au niveau de la classe
                            
                            currentPlayer.setOnEndOfMedia(() -> latch.countDown());
                            currentPlayer.setOnError(() -> latch.countDown());
                            currentPlayer.play();
                        } catch(Exception e) {
                            e.printStackTrace();
                            latch.countDown();
                        }
                    });
                    
                    String[] words = text.split(" ");
                    StringBuilder currentText = new StringBuilder();
                    for (String word : words) {
                        currentText.append(word).append(" ");
                        final String updateText = currentText.toString();
                        Platform.runLater(() -> podcastSubtitleLabel.setText(updateText));
                        Thread.sleep(150); // Simulation speech rate
                    }
                    
                    latch.await(); // Wait for actual audio to finish if text typed faster
                    Thread.sleep(500); // Pause between speakers
                }
                
                Platform.runLater(() -> {
                    btnPlayPodcast.setDisable(false);
                    btnPlayPodcast.setText("▶ Rejouer le Podcast");
                    hostAliceBox.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: transparent; -fx-border-width: 2;");
                    hostBobBox.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: transparent; -fx-border-width: 2;");
                    podcastSubtitleLabel.setText("Podcast terminé !");
                });
                return null;
            }
        };
        
        new Thread(playTask).start();
    }

    @FXML
    public void closeDialog() {
        Stage stage = (Stage) uploadZone.getScene().getWindow();
        stage.close();
    }
}
