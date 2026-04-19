package com.learnhub.controller.admin;

import com.learnhub.models.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public class PromotionController {

    @FXML private Label mainTitle;
    @FXML private TextArea emailArea;
    @FXML private TextArea socialArea;

    private Evenement evenement;

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        generateContent();
    }

    private void generateContent() {
        if (evenement == null) return;

        mainTitle.setText("Promotion : " + evenement.getTitre());

        // Generation de l'email
        StringBuilder email = new StringBuilder();
        email.append("Objet : [Invitation] Participez à l'événement : ").append(evenement.getTitre()).append("\n\n");
        email.append("Cher(e) étudiant(e),\n\n");
        email.append("Nous sommes ravis de vous inviter à notre prochain événement intitulé \"").append(evenement.getTitre()).append("\".\n\n");
        if (evenement.getDescription() != null && !evenement.getDescription().isEmpty()) {
            email.append(evenement.getDescription()).append("\n\n");
        }
        email.append("Quand ? ").append(evenement.getDateDebut()).append(" à ").append(evenement.getHeureDebut()).append("\n");
        email.append("Où ? ").append(evenement.getLieuNom()).append("\n\n");
        email.append("Nous espérons vous y voir nombreux !\n\nL'équipe LearnHub");
        emailArea.setText(email.toString());

        // Generation du post Social Media
        StringBuilder social = new StringBuilder();
        social.append("🚀 Ne manquez pas notre prochain événement : ").append(evenement.getTitre()).append(" !\n\n");
        social.append("Un moment d'échange unique pour booster vos compétences. 🎓\n\n");
        social.append("📍ieu : ").append(evenement.getLieuNom()).append("\n");
        social.append("📅 Date : ").append(evenement.getDateDebut()).append("\n\n");
        social.append("#LearnHub #EvenementUniversitaire #Success #").append(evenement.getTitre().replace(" ", ""));
        socialArea.setText(social.toString());
    }

    @FXML
    private void copyEmail() {
        copyToClipboard(emailArea.getText());
    }

    @FXML
    private void copySocial() {
        copyToClipboard(socialArea.getText());
    }

    private void copyToClipboard(String content) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent cbContent = new ClipboardContent();
        cbContent.putString(content);
        clipboard.setContent(cbContent);
    }

    @FXML
    private void handleClose() {
        ((Stage) mainTitle.getScene().getWindow()).close();
    }
}
