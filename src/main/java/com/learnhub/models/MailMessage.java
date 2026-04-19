package com.learnhub.models;

/**
 * Représente un email (reçu ou envoyé) dans le système de mailing partenaires.
 * Stocké en mémoire via MailHistory — pas de persistance DB.
 *
 * Le champ {@code gmailId} est renseigné lorsque l'email provient de l'API Gmail
 * (pour récupérer le corps complet à la demande).
 */
public class MailMessage {

    private final String  from;
    private final String  to;
    private final String  subject;
    private final String  body;
    private final String  date;
    private final boolean received;   // true = reçu, false = envoyé
    private final String  gmailId;    // ID Gmail API (peut être null)

    // ── Constructeur complet (avec ID Gmail) ─────────────────────────────────
    public MailMessage(String from, String to, String subject, String body,
                       String date, boolean received, String gmailId) {
        this.from     = from     == null ? "" : from;
        this.to       = to       == null ? "" : to;
        this.subject  = subject  == null ? "(Sans objet)" : subject;
        this.body     = body     == null ? "" : body;
        this.date     = date     == null ? "" : date;
        this.received = received;
        this.gmailId  = gmailId;
    }

    // ── Constructeur legacy (sans ID Gmail) ──────────────────────────────────
    public MailMessage(String from, String to, String subject, String body,
                       String date, boolean received) {
        this(from, to, subject, body, date, received, null);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getFrom()     { return from; }
    public String  getTo()       { return to; }
    public String  getSubject()  { return subject; }
    public String  getBody()     { return body; }
    public String  getDate()     { return date; }
    public boolean isReceived()  { return received; }
    public String  getGmailId()  { return gmailId; }

    /** Affiche l'expéditeur pour les messages reçus, le destinataire pour les envoyés. */
    public String getContactLabel() { return received ? from : to; }

    /**
     * Résumé court du corps (snippet) pour les colonnes de tableau.
     * Supprime les sauts de ligne et tronque à 90 caractères.
     */
    public String getBodyPreview() {
        String stripped = body.replace('\n', ' ').replace('\r', ' ').trim();
        return stripped.length() <= 90 ? stripped : stripped.substring(0, 87) + "…";
    }

    @Override
    public String toString() { return "[" + date + "] " + subject; }
}
