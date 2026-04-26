package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.CreneauRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StudentPlanningController {

    @FXML private Label    lblWeekRange;
    @FXML private Label    lblMonthYear;
    @FXML private GridPane gridPlanning;
    @FXML private TextField txtQuestion;
    @FXML private Button btnAskIA;
    @FXML private Button btnMicIA;
    @FXML private ProgressIndicator aiProgress;

    private final CreneauRepository creneauRepo = new CreneauRepository();
    private com.learnhub.medical.util.VoiceAssistantService voiceAssistant = new com.learnhub.medical.util.VoiceAssistantService();
    private LocalDate startOfWeek;
    private static final String[] DAYS = {"Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"};

    @FXML private javafx.scene.control.ScrollPane chatScrollPane;
    @FXML private VBox chatContainer;
    @FXML private VBox chatbotPanel;
    @FXML private Button btnToggleChat;

    @FXML
    private void toggleChat() {
        boolean isVisible = chatbotPanel.isVisible();
        chatbotPanel.setVisible(!isVisible);
        chatbotPanel.setManaged(!isVisible);
        // btnToggleChat.setVisible(isVisible); // Optionnel : cacher le bouton flottant quand le chat est ouvert
    }

    @FXML
    private void handleQuickAction(javafx.event.ActionEvent event) {
        javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
        String text = (String) btn.getUserData();
        if (text != null && !text.isEmpty()) {
            txtQuestion.setText(text);
            handleAskIA();
        }
    }

    private static final String STYLE_NORMAL = "-fx-background-color: #F3F4F6; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 10 15;";
    private static final String STYLE_LISTENING = "-fx-background-color: #FFF8E1; -fx-border-color: #FBC02D; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 10 15;";

    @FXML
    private void handleMicIA() {
        btnMicIA.setDisable(true);
        btnAskIA.setDisable(true);
        txtQuestion.setText("");
        txtQuestion.setStyle(STYLE_LISTENING);
        txtQuestion.setPromptText("Parlez maintenant... (5 secondes)");

        // Tâche asynchrone : appel Windows Speech API via PowerShell (gratuit, offline)
        javafx.concurrent.Task<String> speechTask = new javafx.concurrent.Task<String>() {
            @Override
            protected String call() throws Exception {
                // Script PowerShell qui utilise Windows SAPI (Speech Recognition intégré)
                String psScript =
                    "Add-Type -AssemblyName System.Speech; " +
                    "$engine = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                    "$engine.SetInputToDefaultAudioDevice(); " +
                    "$grammar = New-Object System.Speech.Recognition.DictationGrammar; " +
                    "$engine.LoadGrammar($grammar); " +
                    "$result = $engine.Recognize([System.TimeSpan]::FromSeconds(5)); " +
                    "if ($result -ne $null) { Write-Output $result.Text } else { Write-Output '__RIEN__' }";

                ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", psScript
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                String output = new String(process.getInputStream().readAllBytes(), "UTF-8").trim();
                process.waitFor();
                return output;
            }
        };

        speechTask.setOnSucceeded(e -> {
            String recognized = speechTask.getValue();
            if (recognized == null || recognized.isEmpty() || recognized.equals("__RIEN__") || recognized.contains("Exception")) {
                // Rien compris — afficher message
                txtQuestion.setText("");
                txtQuestion.setPromptText("Pas compris. Reessayez ou tapez votre question.");
            } else {
                // Afficher le texte reconnu lettre par lettre (effet visuel)
                txtQuestion.setText("");
                javafx.animation.Timeline anim = new javafx.animation.Timeline();
                for (int i = 0; i <= recognized.length(); i++) {
                    final String partial = recognized.substring(0, i);
                    anim.getKeyFrames().add(new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(40 * i),
                        ev -> txtQuestion.setText(partial)
                    ));
                }
                anim.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(40 * recognized.length() + 400),
                    ev -> handleAskIA()
                ));
                anim.play();
            }
            txtQuestion.setStyle(STYLE_NORMAL);
            txtQuestion.setPromptText("Tapez votre question ici...");
            btnMicIA.setDisable(false);
            btnAskIA.setDisable(false);
        });

        speechTask.setOnFailed(e -> {
            txtQuestion.setText("");
            txtQuestion.setStyle(STYLE_NORMAL);
            txtQuestion.setPromptText("Erreur micro. Tapez votre question manuellement.");
            btnMicIA.setDisable(false);
            btnAskIA.setDisable(false);
        });

        new Thread(speechTask).start();
    }

    private void addUserBubble(String text) {
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #10B981; -fx-padding: 10 15; -fx-background-radius: 15 15 0 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 2);");
        VBox wrapper = new VBox(lbl);
        wrapper.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        wrapper.setPadding(new javafx.geometry.Insets(5, 0, 5, 40));
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void addAIBubble(String text) {
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(250);
        // Pas de rectangle : fond transparent, texte gris foncé simple
        lbl.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 13; -fx-padding: 0;");

        // Bouton Son — Cercle bleu comme l'icône fournie
        String styleNormal = "-fx-background-color: #5BB8F5; -fx-background-radius: 50; -fx-min-width: 34; -fx-min-height: 34; -fx-max-width: 34; -fx-max-height: 34; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);";
        String styleHover  = "-fx-background-color: #3FA3E8; -fx-background-radius: 50; -fx-min-width: 34; -fx-min-height: 34; -fx-max-width: 34; -fx-max-height: 34; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 6, 0, 0, 3);";
        String styleClick  = "-fx-background-color: #2389CC; -fx-background-radius: 50; -fx-min-width: 34; -fx-min-height: 34; -fx-max-width: 34; -fx-max-height: 34; -fx-cursor: hand;";

        javafx.scene.control.Button btnSound = new javafx.scene.control.Button();
        btnSound.setStyle(styleNormal);

        javafx.scene.control.Label speakerIcon = new javafx.scene.control.Label("\uD83D\uDD0A");
        speakerIcon.setStyle("-fx-text-fill: white; -fx-font-size: 15;");
        btnSound.setGraphic(speakerIcon);

        btnSound.setOnMouseEntered(e -> btnSound.setStyle(styleHover));
        btnSound.setOnMouseExited(e -> btnSound.setStyle(styleNormal));
        btnSound.setOnAction(e -> {
            btnSound.setStyle(styleClick);
            voiceAssistant.playAudioForText(text);
            javafx.animation.PauseTransition reset = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
            reset.setOnFinished(ev -> btnSound.setStyle(styleNormal));
            reset.play();
        });

        javafx.scene.layout.HBox bubbleWithSound = new javafx.scene.layout.HBox(8, btnSound, lbl);
        bubbleWithSound.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox wrapper = new VBox(bubbleWithSound);
        wrapper.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        wrapper.setPadding(new javafx.geometry.Insets(5, 30, 5, 0));
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }


    private void scrollToBottom() {
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(e -> chatScrollPane.setVvalue(1.0));
        pause.play();
    }

    @FXML
    private void handleAskIA() {
        String q = txtQuestion.getText();
        if (q == null || q.trim().isEmpty()) return;

        addUserBubble(q);
        txtQuestion.clear();

        btnAskIA.setDisable(true);
        aiProgress.setVisible(true);

        int userId = com.learnhub.medical.util.SessionManager.getInstance().getCurrentUserId();

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() {
                voiceAssistant.generateResponse(q, userId, 
                    // onTextGenerated callback
                    (text) -> {
                        addAIBubble(text);
                        btnAskIA.setDisable(false);
                        aiProgress.setVisible(false);
                    }
                );
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void initialize() {
        startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        refreshPlanning();
    }

    @FXML private void handlePrevWeek() { startOfWeek = startOfWeek.minusWeeks(1); refreshPlanning(); }
    @FXML private void handleNextWeek() { startOfWeek = startOfWeek.plusWeeks(1);  refreshPlanning(); }

    @FXML
    public void refreshPlanning() {
        gridPlanning.getChildren().clear();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter dtfMonth = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        
        LocalDate end = startOfWeek.plusDays(6);
        lblWeekRange.setText("Semaine du " + startOfWeek.format(dtf) + " au " + end.format(dtf));
        lblMonthYear.setText(startOfWeek.format(dtfMonth).toUpperCase());

        // Create Day Headers
        for (int i = 0; i < 7; i++) {
            VBox header = new VBox(4);
            header.setAlignment(Pos.CENTER);
            header.getStyleClass().add("day-header");
            Label lDay  = new Label(DAYS[i]);
            lDay.getStyleClass().add("day-name");
            Label lDate = new Label(startOfWeek.plusDays(i).format(dtf));
            lDate.getStyleClass().add("day-date");
            header.getChildren().addAll(lDay, lDate);
            gridPlanning.add(header, i, 0);
        }

        // Fetch data
        Map<String, List<Creneau>> map = new HashMap<>();
        for (String d : DAYS) map.put(d, new ArrayList<>());

        try {
            for (Creneau c : creneauRepo.findAll()) {
                String jour = c.getJour();
                if (jour != null && jour.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate date = LocalDate.parse(jour);
                    for (int i = 0; i < 7; i++)
                        if (startOfWeek.plusDays(i).equals(date))
                            map.get(DAYS[i]).add(c);
                } else if (jour != null && map.containsKey(jour)) {
                    map.get(jour).add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fill columns
        for (int i = 0; i < 7; i++) {
            VBox col = new VBox(10);
            col.setAlignment(Pos.TOP_CENTER);
            col.setStyle("-fx-padding: 5;");
            List<Creneau> slots = map.get(DAYS[i]);
            if (slots.isEmpty()) {
                VBox card = createEmptyCard("Aucun créneau");
                col.getChildren().add(card);
            } else {
                for (Creneau c : slots) {
                    col.getChildren().add(createSlotUI(c));
                }
                // Add "Fermé" placeholder if few slots (following screenshot style)
                if (slots.size() < 2) {
                    col.getChildren().add(createEmptyCard("Fermé"));
                }
            }
            gridPlanning.add(col, i, 1);
        }
    }

    private VBox createEmptyCard(String text) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-padding: 15; -fx-border-style: dashed;");
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13; -fx-font-weight: bold;");
        Label icon = new Label("⚪");
        icon.setStyle("-fx-opacity: 0.3;");
        card.getChildren().addAll(icon, l);
        return card;
    }

    private VBox createSlotUI(Creneau c) {
        VBox slot = new VBox(8);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefHeight(90);
        
        Label lTime = new Label(c.getHeure().toString());
        lTime.setStyle("-fx-font-weight: 900; -fx-font-size: 16; -fx-text-fill: #1E293B;");
        
        Label lStatus = new Label();
        String style = "-fx-background-radius: 12; -fx-padding: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);";
        
        if (c.isDisponibilite()) {
            lStatus.setText("✓ RÉSERVER");
            lStatus.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 10; -fx-letter-spacing: 0.5px; -fx-background-color: #10B981; -fx-background-radius: 20; -fx-padding: 4 12;");
            slot.setStyle(style + "-fx-background-color: #F0FDF4; -fx-border-color: #86EFAC; -fx-border-width: 1.5;");
            slot.setOnMouseClicked(e -> showBookingDialog(c));
        } else {
            lStatus.setText("INDISPONIBLE");
            lStatus.setStyle("-fx-text-fill: #991B1B; -fx-font-weight: 900; -fx-font-size: 10; -fx-letter-spacing: 0.5px;");
            slot.setStyle(style + "-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-border-width: 1.5; -fx-opacity: 0.8;");
        }

        slot.getChildren().addAll(lTime, lStatus);
        
        // Hover effects
        slot.setOnMouseEntered(e -> {
            if (c.isDisponibilite()) slot.setStyle(style + "-fx-background-color: #DCFCE7; -fx-border-color: #22C55E; -fx-translate-y: -2;");
        });
        slot.setOnMouseExited(e -> {
            if (c.isDisponibilite()) slot.setStyle(style + "-fx-background-color: #F0FDF4; -fx-border-color: #86EFAC; -fx-translate-y: 0;");
        });

        return slot;
    }

    private void showBookingDialog(Creneau c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/learnhub/medical/view/AddRDVDialog.fxml"));
            Parent root = loader.load();
            AddRDVController ctrl = loader.getController();
            ctrl.setSessionData(this, c);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Prendre RDV");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le formulaire : " + e.getMessage());
            a.show();
        }
    }
}
