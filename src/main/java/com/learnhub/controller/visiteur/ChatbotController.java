package com.learnhub.controller.visiteur;

import com.learnhub.service.ChatbotService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatbotController {

    @FXML private VBox chatBox;
    @FXML private TextField inputField;
    @FXML private ScrollPane scrollPane;
    @FXML private Button micBtn; // New button for voice

    private final ChatbotService chatbotService = new ChatbotService();
    private javafx.scene.web.WebEngine voiceEngine;

    @FXML
    public void initialize() {
        // Welcome message
        addMessage("Bonjour ! Je suis **LearnBot**. Je peux vous écouter et vous répondre vocalement. Cliquez sur le micro ! 🎙️", false);
        
        // Initialize hidden WebView for Speech API
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        voiceEngine = webView.getEngine();
        voiceEngine.loadContent("<html><body></body></html>");
        
        // Auto-scroll to bottom
        chatBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void handleSend() {
        String text = inputField.getText();
        if (text == null || text.isBlank()) return;

        addMessage(text, true);
        inputField.clear();

        // Simulate bot thinking
        new Thread(() -> {
            try { Thread.sleep(600); } catch (Exception ignored) {}
            String response = chatbotService.generateResponse(text);
            Platform.runLater(() -> {
                addMessage(response, false);
                speak(response);
            });
        }).start();
    }

    @FXML
    private void handleMic() {
        micBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40;");
        voiceEngine.executeScript(
            "if (!(window.SpeechRecognition || window.webkitSpeechRecognition)) {" +
            "  alert('VOICE_NOT_SUPPORTED');" +
            "} else {" +
            "  var recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();" +
            "  recognition.lang = 'fr-FR';" +
            "  recognition.onresult = function(event) {" +
            "    var text = event.results[0][0].transcript;" +
            "    alert(text);" +
            "  };" +
            "  recognition.onend = function() { alert('MIC_STOPPED'); };" +
            "  recognition.start();" +
            "}"
        );
        
        voiceEngine.setOnAlert(event -> {
            String data = event.getData();
            Platform.runLater(() -> {
                if (data.equals("VOICE_NOT_SUPPORTED")) {
                    addMessage("Désolé, la reconnaissance vocale n'est pas supportée sur ce système. Vous pouvez toujours m'écrire !", false);
                    micBtn.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40;");
                } else if (data.equals("MIC_STOPPED")) {
                    micBtn.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40;");
                } else {
                    inputField.setText(data);
                    micBtn.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40;");
                    handleSend();
                }
            });
        });
    }

    private void speak(String text) {
        String plainText = text.replaceAll("\\*\\*", "").replaceAll("\\n", " ");
        voiceEngine.executeScript("window.speechSynthesis.speak(new SpeechSynthesisUtterance('" + plainText.replace("'", "\\'") + "'))");
    }

    private void addMessage(String text, boolean isUser) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(260);

        HBox wrapper = new HBox(label);
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        if (isUser) {
            label.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 15 15 0 15; -fx-font-size: 14px;");
        } else {
            label.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #111827; -fx-padding: 10 15; -fx-background-radius: 15 15 15 0; -fx-font-size: 14px;");
        }

        chatBox.getChildren().add(wrapper);
    }

    @FXML
    private void closeChat() {
        Stage stage = (Stage) chatBox.getScene().getWindow();
        stage.close();
    }
}
