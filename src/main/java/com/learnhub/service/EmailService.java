package com.learnhub.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // IMPORTANT: Configuration SMTP (Exemple pour Gmail)
    // Pour que cela fonctionne avec Gmail, vous devez :
    // 1. Activer l'authentification à deux facteurs.
    // 2. Créer un "Mot de passe d'application".
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "mabroukeya149@gmail.com";
    private static final String SENDER_PASSWORD = "frclnslddmpnxpug";

    public static void sendConfirmationEmail(String recipientEmail, String fullName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Confirmation de votre candidature - LearnHub");

            String content = "Bonjour " + fullName + ",\n\n" +
                    "Nous vous informons que votre demande a été soumise avec succès.\n\n" +
                    "Nous vous remercions pour votre intérêt et restons à votre disposition pour toute information complémentaire.\n\n"
                    +
                    "Cordialement,\n" +
                    "L'équipe de LearnHub";

            message.setText(content);

            // Envoi en arrière-plan pour ne pas bloquer l'interface UI
            new Thread(() -> {
                try {
                    Transport.send(message);
                    System.out.println("Email envoyé avec succès à " + recipientEmail);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
                }
            }).start();

        } catch (MessagingException e) {
            System.err.println("Erreur de configuration du message : " + e.getMessage());
        }
    }
}
