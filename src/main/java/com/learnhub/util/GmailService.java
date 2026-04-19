package com.learnhub.util;

import com.learnhub.models.MailMessage;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Service to handle real Gmail IMAP (fetching messages) and SMTP (sending messages).
 * Requires an App Password from Google if 2FA is enabled.
 */
public class GmailService {

    private static String currentEmail;
    private static String currentAppPassword;
    private static boolean authenticated = false;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static boolean setCredentials(String email, String appPassword) {
        currentEmail = email;
        currentAppPassword = appPassword;
        // Verify connection by attempting purely to connect to SMTP
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(currentEmail, currentAppPassword);
                }
            });
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", currentEmail, currentAppPassword);
            transport.close();
            authenticated = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            authenticated = false;
            return false;
        }
    }

    public static boolean isAuthenticated() {
        return authenticated;
    }

    public static String getCurrentEmail() {
        return currentEmail;
    }

    public static void logout() {
        currentEmail = null;
        currentAppPassword = null;
        authenticated = false;
    }

    /**
     * Send an email natively via java mail SMTP.
     */
    public static void sendEmail(String to, String subject, String body) throws MessagingException {
        if (!authenticated) throw new MessagingException("Not authenticated. Please configure Gmail credentials.");
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(currentEmail, currentAppPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(currentEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    /**
     * Parse plaintext from a multipart message or generic Part
     */
    private static String getTextFromMessage(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/plain")) {
            return (String) p.getContent();
        } else if (p.isMimeType("text/html")) {
            return (String) p.getContent();
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getTextFromMessage(mp.getBodyPart(i));
                if (s != null) return s;
            }
        }
        return "";
    }

    private static String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * Fetch recent emails from a specific IMAP folder.
     */
    public static Task<List<MailMessage>> fetchRecently(String folderName, int maxResults, boolean isSentBox) {
        return new Task<>() {
            @Override
            protected List<MailMessage> call() throws Exception {
                if (!authenticated) throw new Exception("Not authenticated. Please configure your Gmail credentials.");
                
                List<MailMessage> messagesOut = new ArrayList<>();
                Properties props = new Properties();
                props.put("mail.store.protocol", "imaps");
                
                Session session = Session.getInstance(props, null);
                Store store = null;
                Folder folder = null;
                
                try {
                    store = session.getStore("imaps");
                    store.connect("imap.gmail.com", currentEmail, currentAppPassword);
                    
                    folder = store.getFolder(folderName);
                    if (!folder.exists()) {
                        // Fallback translation if needed
                        if (folderName.equalsIgnoreCase("[Gmail]/Envoyés") || folderName.equalsIgnoreCase("[Gmail]/Sent Mail")) {
                            Folder[] all = store.getDefaultFolder().list("*");
                            for (Folder f : all) {
                                if (f.getName().contains("Sent") || f.getName().contains("Envoy")) {
                                    folder = f;
                                    break;
                                }
                            }
                        }
                    }
                    if (!folder.exists()) return messagesOut;

                    folder.open(Folder.READ_ONLY);
                    int total = folder.getMessageCount();
                    if (total == 0) return messagesOut;
                    
                    int start = Math.max(1, total - maxResults + 1);
                    Message[] msgs = folder.getMessages(start, total);
                    // Iterate backwards for newest first
                    for (int i = msgs.length - 1; i >= 0; i--) {
                        if (isCancelled()) break;
                        Message m = msgs[i];
                        
                        String from = "-";
                        if (m.getFrom() != null && m.getFrom().length > 0) {
                            from = m.getFrom()[0].toString();
                        }
                        
                        String to = "-";
                        if (m.getRecipients(Message.RecipientType.TO) != null && m.getRecipients(Message.RecipientType.TO).length > 0) {
                            to = m.getRecipients(Message.RecipientType.TO)[0].toString();
                        }

                        String subject = m.getSubject() != null ? m.getSubject() : "(Sans objet)";
                        String dateStr = "";
                        if (m.getReceivedDate() != null) {
                            dateStr = m.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).format(FMT);
                        } else if (m.getSentDate() != null) {
                            dateStr = m.getSentDate().toInstant().atZone(ZoneId.systemDefault()).format(FMT);
                        }

                        String body = "";
                        try {
                            body = stripHtml(getTextFromMessage(m));
                        } catch (Exception ignored) { }
                        
                        messagesOut.add(new MailMessage(from, to, subject, body, dateStr, false)); // We ignore Read flag for simplicity
                    }
                    
                    return messagesOut;
                } finally {
                    try { if (folder != null && folder.isOpen()) folder.close(false); } catch (Exception e) {}
                    try { if (store != null) store.close(); } catch (Exception e) {}
                }
            }
        };
    }
}
