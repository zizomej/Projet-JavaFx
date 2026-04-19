package com.learnhub.controller.admin;

import com.learnhub.models.Seance;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class IASessionsController {

    @FXML private Label headerTitle;
    @FXML private TextField videoUrlField;
    @FXML private TextArea previewArea;

    private Seance seance;

    public void setSeance(Seance seance) {
        this.seance = seance;
        headerTitle.setText("Assistant IA : " + seance.getModuleTitre());
        previewArea.setText("Sélectionnez l'un des outils ci-dessus pour commencer l'analyse de la séance.");
    }

    @FXML
    private void handleTranscription() {
        if (videoUrlField.getText().isEmpty()) {
            previewArea.setText("⚠️ Veuillez entrer une URL valide pour la transcription.");
            return;
        }
        
        previewArea.setText("⏳ Analyse du flux audio [" + videoUrlField.getText() + "]...\n\n" +
                "[00:00:15] Professeur : Bienvenue à tous pour cette séance sur " + seance.getModuleTitre() + ".\n" +
                "[00:01:20] Aujourd'hui nous allons aborder les concepts fondamentaux du chapitre.\n" +
                "[00:05:45] Question étudiant : Est-ce que ce point sera abordé à l'examen ?\n" +
                "[00:05:55] Professeur : Oui, c'est un point crucial de la " + seance.getType() + ".\n\n" +
                "--- Transcription complétée par l'IA ---");
    }

    @FXML
    private void handleSummary() {
        previewArea.setText("📚 GÉNÉRATION DU RÉSUMÉ PÉDAGOGIQUE\n\n" +
                "Module : " + seance.getModuleTitre() + "\n" +
                "Type : " + seance.getType() + "\n" +
                "Date : " + seance.getDate() + "\n\n" +
                "POINTS CLÉS :\n" +
                "1. Introduction aux objectifs du module.\n" +
                "2. Analyse approfondie des méthodes théoriques.\n" +
                "3. Applications pratiques en " + seance.getType() + ".\n" +
                "4. Synthèse des prérequis pour la séance suivante.\n\n" +
                "Conclusion : La séance a permis de couvrir 85% des objectifs fixés.");
    }

    @FXML
    private void handleQuiz() {
        previewArea.setText("❓ GÉNÉRATION DU QCM (FORMAT JSON)\n\n" +
                "{\n" +
                "  \"quiz_id\": \"q_" + seance.getId() + "\",\n" +
                "  \"title\": \"Auto-évaluation - " + seance.getModuleTitre() + "\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"q\": \"Quel était l'objectif principal de la séance du " + seance.getDate() + " ?\",\n" +
                "      \"options\": [\"Option A\", \"Option B\", \"Option C\"],\n" +
                "      \"answer\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"q\": \"Le contenu abordé en " + seance.getType() + " est-il validé ?\",\n" +
                "      \"options\": [\"Oui\", \"Non\", \"En partie\"],\n" +
                "      \"answer\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}");
    }

    @FXML
    private void handleClose() {
        ((Stage) headerTitle.getScene().getWindow()).close();
    }
}
