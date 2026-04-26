package com.learnhub.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ChatbotController {

    @FXML private VBox chatWindow;
    @FXML private Button fabBtn;
    @FXML private VBox messagesContainer;
    @FXML private TextField inputField;
    @FXML private Button btnSend;
    @FXML private ScrollPane scrollPane;

    // TODO: Remplacez cette clé par votre propre clé API Google Gemini
    private static final String API_KEY = "AIzaSyD2Uy7MqFheLHUprL-xe2H5Q7bcKX8Bzh0";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private JSONArray chatHistory = new JSONArray();

    private final String SYSTEM_PROMPT = "Tu es LearnHub AI, l'assistant officiel de la plateforme éducative LearnHub. " +
            "LearnHub permet de gérer les inscriptions, les programmes (Licence et Master en Informatique, Télécoms, Gestion), " +
            "les modules, les notes, les présences, et inclut un service médical pour les étudiants. " +
            "Tu dois répondre de façon chaleureuse, courte et concise en français. Si on te pose une question générale hors-sujet, " +
            "RAMÈNE TOUJOURS délicatement le sujet vers LearnHub. N'utilise pas de markdown complexe, juste du texte normal.";

    @FXML
    public void initialize() {
        // Ajouter un message de bienvenue par défaut
        addBotMessage("Bienvenue ! Je suis LearnHub AI 🎓\nComment puis-je vous aider à découvrir nos programmes et services ?");

        // Gérer la touche Entrée
        inputField.setOnAction(e -> handleSend());

        // Garder le scroll en bas quand de nouveaux messages arrivent
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));
    }

    @FXML
    private void toggleChat() {
        if (chatWindow.isVisible()) {
            chatWindow.setVisible(false);
            fabBtn.setVisible(true);
        } else {
            chatWindow.setVisible(true);
            fabBtn.setVisible(false);
            Platform.runLater(() -> inputField.requestFocus());
        }
    }

    @FXML
    private void handleSuggestion(ActionEvent event) {
        Button source = (Button) event.getSource();
        String question = source.getText();
        inputField.setText(question);
        handleSend();
    }

    @FXML
    private void handleSend() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        inputField.clear();
        addUserMessage(msg);

        // Si la clé API n'est pas configurée
        if (API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI") || API_KEY.isEmpty()) {
            addBotMessage("⚠️ L'administrateur n'a pas encore configuré la clé API Gemini.");
            return;
        }

        // Bloquer l'entrée pendant le chargement
        inputField.setDisable(true);
        btnSend.setDisable(true);

        callGeminiApi(msg).thenAccept(response -> {
            Platform.runLater(() -> {
                addBotMessage(response);
                inputField.setDisable(false);
                btnSend.setDisable(false);
                inputField.requestFocus();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                String causeMsg = e.getCause() != null ? e.getCause().getMessage() : "";
                if (causeMsg.contains("503") || (e.getMessage() != null && e.getMessage().contains("503"))) {
                    addBotMessage("Le serveur IA est actuellement surchargé (Erreur 503). Veuillez réessayer dans un instant !");
                } else {
                    addBotMessage("Désolé, une erreur technique est survenue. Veuillez réessayer !");
                }
                inputField.setDisable(false);
                btnSend.setDisable(false);
            });
            System.err.println("Erreur Chatbot: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            return null;
        });
    }

    private void addUserMessage(String text) {
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #1f2937; -fx-padding: 10 15; -fx-background-radius: 15 15 0 15; -fx-font-size: 13px;");
        msgLabel.setMaxWidth(250);

        HBox wrapper = new HBox(msgLabel);
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        messagesContainer.getChildren().add(wrapper);

        // Add to history
        appendHistory("user", text);
    }

    private void addBotMessage(String text) {
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #1e3a8a; -fx-padding: 10 15; -fx-background-radius: 15 15 15 0; -fx-border-color: #d1d5db; -fx-border-radius: 15 15 15 0; -fx-font-size: 13px;");
        msgLabel.setMaxWidth(250);

        HBox wrapper = new HBox(msgLabel);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        messagesContainer.getChildren().add(wrapper);

        // Add to history (unless it's an error message)
        if (!text.startsWith("⚠️") && !text.startsWith("Désolé")) {
            appendHistory("model", text);
        }
    }

    private void appendHistory(String role, String text) {
        JSONObject parts = new JSONObject().put("text", text);
        JSONArray partsArray = new JSONArray().put(parts);
        JSONObject message = new JSONObject().put("role", role).put("parts", partsArray);
        chatHistory.put(message);
    }

    private CompletableFuture<String> callGeminiApi(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject requestBody = new JSONObject();

                // 1. System Instruction (pour donner la personnalité et le contexte)
                JSONObject systemText = new JSONObject().put("text", SYSTEM_PROMPT);
                JSONObject systemInstruction = new JSONObject().put("parts", new JSONArray().put(systemText));
                requestBody.put("system_instruction", systemInstruction);

                // 2. Historique des conversations
                requestBody.put("contents", chatHistory);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject jsonResp = new JSONObject(response.body());
                    return jsonResp.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                } else {
                    System.err.println("Gemini API Error (" + response.statusCode() + "): " + response.body());
                    throw new RuntimeException("API request failed with status: " + response.statusCode());
                }

            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException("Erreur inattendue: " + e.getMessage(), e);
            }
        });
    }
}
