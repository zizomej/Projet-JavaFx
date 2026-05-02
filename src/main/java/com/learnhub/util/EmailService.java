package com.learnhub.util;

import com.learnhub.models.Utilisateur;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private static final String FROM_EMAIL = "notifications@learnhub.com";
    
    public static void sendEliminationAlert(Utilisateur student, String moduleName, int absenceCount) {
        String subject = "";
        String body = "";

        if (absenceCount >= 4) {
            subject = "🚫 ALERTE CRITIQUE : Élimination confirmée - " + moduleName;
            body = "Bonjour " + student.getPrenom() + " " + student.getNom() + ",\n\n" +
                   "Nous vous informons que vous avez atteint " + absenceCount + " absences dans le module : " + moduleName + ".\n" +
                   "Conformément au règlement, vous êtes officiellement ÉLIMINÉ de cette matière et ne pourrez plus passer les examens.\n\n" +
                   "Cordialement,\n" +
                   "L'administration LearnHub";
        } else if (absenceCount == 3) {
            subject = "⚠️ AVERTISSEMENT : Risque d'élimination - " + moduleName;
            body = "Bonjour " + student.getPrenom() + " " + student.getNom() + ",\n\n" +
                   "Ceci est un avertissement concernant votre taux d'absentéisme dans le module : " + moduleName + ".\n" +
                   "Vous avez actuellement 3 absences. Une absence supplémentaire entraînera votre élimination automatique.\n\n" +
                   "Merci de régulariser votre situation au plus vite.\n\n" +
                   "Cordialement,\n" +
                   "L'administration LearnHub";
        }

        if (!subject.isEmpty()) {
            System.out.println("--------------------------------------------------");
            System.out.println("SIMULATION ENVOI EMAIL À: " + student.getEmail());
            System.out.println("SUJET: " + subject);
            System.out.println("MESSAGE:\n" + body);
            System.out.println("--------------------------------------------------");
            
            // Note: En environnement réel, on configurerait une Session JavaMail ici.
            // Pour le projet, on simule l'envoi réussi.
        }
    }
}
