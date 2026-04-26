package com.learnhub.medical.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioWhatsAppService {

    // --- VOS CLÉS TWILIO RÉELLES ---
    public static final String ACCOUNT_SID = "YOUR_ACCOUNT_SID";
    public static final String AUTH_TOKEN  = "YOUR_AUTH_TOKEN";
    public static final String TWILIO_WHATSAPP_NUMBER = "+14155238886"; 
    // --------------------------------

    public void sendWhatsAppConfirmation(String to, String studentName, String date, String time) {
        new Thread(() -> {
            try {
                Twilio.init(ACCOUNT_SID.trim(), AUTH_TOKEN.trim());

                String target = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
                String sender = "whatsapp:" + TWILIO_WHATSAPP_NUMBER;

                String text = "✅ *LearnHub Medical* : Bonjour " + studentName + ",\n\n"
                            + "Votre rendez-vous est *CONFIRMÉ* ! \n"
                            + "📅 Date : *" + date + "* \n"
                            + "🕒 Heure : *" + time + "* \n\n"
                            + "Nous vous attendons avec impatience ! ✨";

                System.out.println(">>> TWILIO API : Envoi vers " + target + "...");

                Message message = Message.creator(
                        new PhoneNumber(target), 
                        new PhoneNumber(sender),
                        text
                ).create();

                System.out.println(">>> TWILIO API : SUCCÈS ! ID: " + message.getSid());

            } catch (Exception e) {
                System.err.println("!!! ERREUR TWILIO API : " + e.getMessage());
            }
        }).start();
    }
}
