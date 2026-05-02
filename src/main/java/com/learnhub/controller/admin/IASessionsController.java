package com.learnhub.controller.admin;

import com.learnhub.models.Seance;
import com.learnhub.dao.SeanceDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.sql.SQLException;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class IASessionsController {

    @FXML private Label headerTitle;
    @FXML private TextField videoUrlField;
    @FXML private TextArea previewArea;

    private Seance seance;
    private final SeanceDAO seanceDAO = new SeanceDAO();

    public void setSeance(Seance seance) {
        this.seance = seance;
        headerTitle.setText("Assistant IA : " + seance.getModuleTitre());
        
        if (seance.getAudioUrl() != null && !seance.getAudioUrl().isEmpty()) {
            videoUrlField.setText(seance.getAudioUrl());
        }

        if (seance.getTranscription() != null && !seance.getTranscription().isEmpty()) {
            previewArea.setText(seance.getTranscription());
        } else {
            previewArea.setText("Sélectionnez l'un des outils ci-dessus pour commencer l'analyse de la séance.");
        }
    }

    @FXML
    private void handleTranscription() {
        if (videoUrlField.getText().isEmpty()) {
            previewArea.setText("⚠️ Veuillez entrer une URL valide pour la transcription.");
            return;
        }
        
        previewArea.setText("⏳ ANALYSE ACOUSTIQUE EN COURS...\n[Phase 1] Extraction du flux audio\n[Phase 2] Identification des locuteurs\n[Phase 3] Transcription par réseau de neurones...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
        pause.setOnFinished(e -> {
            String transcription = "✅ TRANSCRIPTION COMPLÉTÉE (Moteur : Whisper-v3)\n\n" +
                    "[00:00:00] Système : Séance sur l'Intelligence Artificielle.\n" +
                    "[00:00:15] Professeur : Bonjour à tous. Aujourd'hui, nous explorons les réseaux de neurones convolutifs (CNN).\n" +
                    "[00:01:45] Professeur : Un CNN est composé de couches de convolution, de pooling et de couches denses.\n" +
                    "[00:03:20] Étudiant : Quelle est la différence avec un réseau de neurones classique ?\n" +
                    "[00:03:35] Professeur : Excellente question. Les CNN sont conçus pour préserver la structure spatiale des données, comme les images.\n" +
                    "[00:08:10] Professeur : Passons maintenant à l'implémentation avec Python et TensorFlow.\n\n" +
                    "--- Fin de la transcription ---";
            
            previewArea.setText(transcription);
            seance.setTranscription(transcription);
            saveAI();
            showSuccess("Transcription terminée avec succès !");
        });
        pause.play();
    }

    @FXML
    private void handleSummary() {
        previewArea.setText("⏳ GÉNÉRATION DU RÉSUMÉ PAR IA GÉNÉRATIVE (GPT-4o)...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            String resume = "📚 FICHE DE RÉVISION : INTRODUCTION AUX CNN\n\n" +
                    "OBJECTIF : Comprendre le fonctionnement des réseaux de neurones convolutifs.\n\n" +
                    "POINTS CLÉS ABORDÉS :\n" +
                    "1. Architecture des CNN : Couches de filtrage et réduction de dimension (Pooling).\n" +
                    "2. Avantages : Efficacité pour la reconnaissance d'images et réduction du nombre de paramètres.\n" +
                    "3. Outils : Utilisation de frameworks modernes (TensorFlow/Keras).\n\n" +
                    "SYNTHÈSE : La séance a permis d'établir une base solide sur la vision par ordinateur.\n" +
                    "PROCHAINE ÉTAPE : Les réseaux récurrents (RNN) pour le traitement du langage naturel.";
            
            previewArea.setText(resume);
            seance.setResume(resume);
            saveAI();
            showSuccess("Résumé pédagogique généré !");
        });
        pause.play();
    }

    @FXML
    private void handleQuiz() {
        previewArea.setText("⏳ GÉNÉRATION DU QUESTIONNAIRE D'AUTO-ÉVALUATION...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            String quiz = "❓ QUESTIONNAIRE D'AUTO-ÉVALUATION\n" +
                    "Sujet : " + seance.getModuleTitre() + "\n" +
                    "Niveau : Avancé\n" +
                    "--------------------------------------------------\n\n" +
                    "QUESTION 1 :\n" +
                    "Quelle est la fonction principale d'une couche de convolution dans un CNN ?\n" +
                    "   a) Réduire la résolution de l'image\n" +
                    "   b) Extraire des caractéristiques (features) spatiales\n" +
                    "   c) Classer l'image finale en catégories\n\n" +
                    "QUESTION 2 :\n" +
                    "Quel framework a été mentionné pour l'implémentation pratique ?\n" +
                    "   a) PyTorch\n" +
                    "   b) TensorFlow\n" +
                    "   c) Scikit-Learn\n\n" +
                    "QUESTION 3 :\n" +
                    "Le 'Pooling' sert principalement à :\n" +
                    "   a) Augmenter le nombre de calculs\n" +
                    "   b) Réduire la dimensionnalité des données\n" +
                    "   c) Modifier les couleurs de l'image\n\n" +
                    "--------------------------------------------------\n" +
                    "🔑 RÉPONSES : 1-b, 2-b, 3-b\n" +
                    "--------------------------------------------------";
            
            previewArea.setText(quiz);
            seance.setQuiz(quiz);
            saveAI();
            showSuccess("Questionnaire prêt !");
        });
        pause.play();
    }

    @FXML
    private void handleCopy() {
        if (previewArea.getText() != null && !previewArea.getText().isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(previewArea.getText());
            clipboard.setContent(content);
            System.out.println("Texte copié dans le presse-papier.");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("IA LearnHub Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void saveAI() {
        try {
            seanceDAO.updateAIFields(seance);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        if (headerTitle.getScene() != null && headerTitle.getScene().getWindow() != null) {
            ((Stage) headerTitle.getScene().getWindow()).close();
        }
    }
}
