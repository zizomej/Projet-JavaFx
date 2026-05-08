package com.learnhub.medical.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {

    private final String MY_EMAIL    = ConfigLoader.get("mail.email", "araartasnim7@gmail.com"); 
    private final String MY_PASSWORD = ConfigLoader.get("mail.password", "REMPLACER_PAR_CODE_APP_GMAIL"); 

    public void sendConfirmationEmail(String to, String studentName, String rdvDetails) {
        System.out.println(">>> [MAIL] Tentative d'envoi vers : " + to);
        
        if (MY_PASSWORD == null || MY_PASSWORD.isEmpty() || MY_PASSWORD.equals("REMPLACER_PAR_CODE_APP_GMAIL")) {
            System.err.println("!!! [MAIL] ERREUR : Le mot de passe d'application n'est pas configuré dans config.properties.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Utilisation de STARTTLS (plus moderne)
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Forcer TLS 1.2 pour Gmail
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MY_EMAIL, MY_PASSWORD);
            }
        });

        // session.setDebug(true); // Décommenter pour voir tout le dialogue SMTP dans la console

        new Thread(() -> {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(MY_EMAIL, "LearnHub Medical"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("✅ Confirmation de votre Rendez-vous - LearnHub Medical");

                String htmlContent = "<div style='font-family: Arial, sans-serif; color: #1e293b; max-width: 600px; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;'>"
                        + "<div style='background-color: #1e3a8a; padding: 20px; text-align: center;'>"
                        + "  <h1 style='color: white; margin: 0; font-size: 22px;'>Confirmation de Paiement & RDV</h1>"
                        + "</div>"
                        + "<div style='padding: 30px;'>"
                        + "  <p style='font-size: 16px;'>Bonjour <strong>" + studentName + "</strong>,</p>"
                        + "  <p>Nous vous confirmons que votre paiement a été validé et votre rendez-vous est officiellement enregistré.</p>"
                        + "  <div style='background-color: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #3b82f6;'>"
                        + "    <p style='margin: 5px 0;'><strong>Détails :</strong> " + rdvDetails + "</p>"
                        + "    <p style='margin: 5px 0;'><strong>Statut :</strong> <span style='color: #10b981; font-weight: bold;'>PAYÉ / CONFIRMÉ</span></p>"
                        + "  </div>"
                        + "  <p>Merci de vous présenter 5 minutes avant l'heure prévue muni de votre carte étudiant.</p>"
                        + "  <p style='margin-top: 30px;'>Cordialement,<br><strong>L'équipe LearnHub Medical</strong></p>"
                        + "</div>"
                        + "<div style='background-color: #f1f5f9; padding: 15px; text-align: center; font-size: 12px; color: #64748b;'>"
                        + "  Ceci est un message automatique, merci de ne pas y répondre."
                        + "</div>"
                        + "</div>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                System.out.println(">>> [MAIL] Envoi en cours...");
                Transport.send(message);
                System.out.println(">>> [MAIL] SUCCÈS : Email de confirmation envoyé à " + to + " ✅");

            } catch (Exception e) {
                System.err.println("!!! [MAIL] ÉCHEC : Erreur lors de l'envoi de l'email.");
                System.err.println("!!! Détail : " + e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("AuthenticationFailedException")) {
                    System.err.println("!!! [ASTUCE] Vérifiez que votre mot de passe d'application Google est toujours valide.");
                }
                e.printStackTrace();
            }
        }).start();
    }
}
