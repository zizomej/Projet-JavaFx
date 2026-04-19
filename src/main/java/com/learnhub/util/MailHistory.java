package com.learnhub.util;

import com.learnhub.models.MailMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton en mémoire pour stocker l'historique des emails partenaires.
 * La boîte de réception est pré-remplie avec des messages de démonstration.
 * Les emails envoyés sont ajoutés dynamiquement via addSent().
 */
public class MailHistory {

    private static MailHistory instance;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<MailMessage> inbox = new ArrayList<>();
    private final List<MailMessage> sent  = new ArrayList<>();

    // ─── Singleton ───────────────────────────────────────────────────────────────

    private MailHistory() {
        seedInbox();
    }

    public static synchronized MailHistory getInstance() {
        if (instance == null) instance = new MailHistory();
        return instance;
    }

    // ─── API publique ─────────────────────────────────────────────────────────────

    public List<MailMessage> getInbox() { return Collections.unmodifiableList(inbox); }
    public List<MailMessage> getSent()  { return Collections.unmodifiableList(sent);  }

    public void addSent(MailMessage msg) {
        sent.add(0, msg);     // plus récent en premier
    }

    public void addReceived(MailMessage msg) {
        inbox.add(0, msg);
    }

    // ─── Données de démonstration ─────────────────────────────────────────────────

    private void seedInbox() {
        inbox.add(new MailMessage(
            "contact@microsoft.tn", "admin@learnhub.tn",
            "Confirmation de partenariat 2025-2026",
            "Bonjour,\n\n"
            + "Nous confirmons notre intérêt pour un partenariat avec LearnHub pour l'année académique "
            + "2025-2026. Nous proposons 5 postes de stage en développement logiciel (Java, .NET, Cloud Azure).\n\n"
            + "Veuillez nous faire parvenir la convention de stage pour signature.\n\n"
            + "Cordialement,\nSofia Ben Abid\nDRH — Microsoft Tunisie",
            "2025-04-10 09:30", true
        ));
        inbox.add(new MailMessage(
            "rh@google.tn", "admin@learnhub.tn",
            "Candidature stage — Ahmed Ben Ali",
            "Madame, Monsieur,\n\n"
            + "Nous avons bien reçu la candidature d'Ahmed Ben Ali (3ème année Génie Logiciel) "
            + "pour le poste de stagiaire développeur Cloud.\n\n"
            + "Nous souhaitons planifier un entretien en visioconférence la semaine prochaine. "
            + "Pourriez-vous transmettre ses disponibilités ?\n\n"
            + "Bien cordialement,\nYassine Trabelsi\nGoogle Tunisie — Talent Acquisition",
            "2025-04-08 14:15", true
        ));
        inbox.add(new MailMessage(
            "partenariat@orange.tn", "admin@learnhub.tn",
            "Nouvelles offres de stage — Juillet 2025",
            "Bonjour,\n\n"
            + "Nous souhaitons publier 3 nouvelles offres de stage pour la période juillet–septembre 2025 "
            + "dans les domaines : Réseaux & Télécoms, Cybersécurité, IoT & Embarqué.\n\n"
            + "Merci de nous indiquer la procédure de publication sur la plateforme LearnHub.\n\n"
            + "Cordialement,\nOrange Tunisie — Service Partenariats",
            "2025-04-05 11:00", true
        ));
        inbox.add(new MailMessage(
            "contact@accenture.tn", "admin@learnhub.tn",
            "Demande d'informations — Programme Stage",
            "Bonjour,\n\n"
            + "Nous souhaiterions obtenir plus d'informations sur votre programme de stage et "
            + "les profils étudiants disponibles en Génie Logiciel et Data Science pour le S2 2025.\n\n"
            + "Pouvez-vous nous envoyer une plaquette de présentation ?\n\nCordialement,\nAccenture Tunisie",
            "2025-04-02 16:45", true
        ));
        inbox.add(new MailMessage(
            "direction@vermeg.com", "admin@learnhub.tn",
            "Renouvellement convention cadre",
            "Bonjour,\n\n"
            + "Notre convention cadre de partenariat arrivant à expiration le 30 juin 2025, "
            + "nous souhaitons procéder à son renouvellement pour 2 ans.\n\n"
            + "Veuillez trouver ci-joint le projet de convention révisé.\n\n"
            + "Bien à vous,\nVermeg SA — Direction Générale",
            "2025-03-28 10:20", true
        ));
    }
}
