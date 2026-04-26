package com.learnhub.medical.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {

    private final String MY_EMAIL = "araartasnim7@gmail.com"; 
    private final String MY_PASSWORD = "YOUR_GMAIL_APP_PASSWORD"; 

    public void sendConfirmationEmail(String to, String studentName, String rdvDetails) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MY_EMAIL, MY_PASSWORD);
            }
        });

        new Thread(() -> {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(MY_EMAIL, "LearnHub Medical"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("Confirmation de demande de rendez-vous médical");

                // Construction du contenu HTML identique à la capture d'écran
                String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px;'>"
                        + "<h1 style='color: #1a1a1a; font-size: 28px;'>Confirmation de Demande de Rendez-vous</h1>"
                        + "<p>Bonjour,</p>"
                        + "<p>Votre demande de rendez-vous médical a bien été reçue avec les détails suivants :</p>"
                        + "<ul>"
                        + "  <li><strong>Créneau :</strong> 📅 " + rdvDetails + "</li>"
                        + "  <li><strong>Motif :</strong> " + "Consultation Médicale" + "</li>"
                        + "  <li><strong>Description :</strong> " + "Enregistrée avec succès" + "</li>"
                        + "</ul>"
                        + "<p>Vous recevrez un nouveau mail dès que votre rendez-vous sera validé.</p>"
                        + "<p>Cordialement,<br><strong>Le service médical universitaire</strong></p>"
                        + "</div>";

                // On définit le contenu comme étant du HTML
                message.setContent(htmlContent, "text/html; charset=utf-8");

                System.out.println(">>> Envoi de l'email HTML Premium...");
                Transport.send(message);
                System.out.println(">>> EMAIL ENVOYÉ ! ✅");

            } catch (Exception e) {
                System.err.println("!!! Erreur Email : " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
