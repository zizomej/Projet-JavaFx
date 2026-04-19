package com.learnhub.util;

import com.learnhub.models.MailMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Gmail REST API client.
 *
 * Reads and sends emails using only Google's HTTPS API + an OAuth2 access token.
 * No SMTP, no IMAP, no app-password required.
 *
 * API reference: https://developers.google.com/gmail/api/reference/rest
 */
public class GmailApiClient {

    private static final String BASE = "https://gmail.googleapis.com/gmail/v1/users/me";

    private final GmailOAuthService oauth;
    private final HttpClient        http  = HttpClient.newHttpClient();

    public GmailApiClient(GmailOAuthService oauth) { this.oauth = oauth; }

    // ── Inbox ─────────────────────────────────────────────────────────────────

    /**
     * Fetches the latest {@code maxResults} messages from the INBOX label.
     * Returns lightweight MailMessage objects (headers + snippet only, no full body download).
     */
    public List<MailMessage> fetchInbox(int maxResults) throws Exception {
        String token   = oauth.getAccessToken();
        String listUrl = BASE + "/messages?maxResults=" + maxResults
                + "&labelIds=INBOX&orderBy=date";

        HttpResponse<String> listResp = get(listUrl, token);
        List<String> ids = extractMessageIds(listResp.body());

        List<MailMessage> result = new ArrayList<>();
        for (String id : ids) {
            try { result.add(fetchMessageMeta(id, token)); }
            catch (Exception ignored) { /* skip malformed messages */ }
        }
        return result;
    }

    /** Fetches the full plain-text body of a single message. */
    public String fetchMessageBody(String messageId) throws Exception {
        String token = oauth.getAccessToken();
        String url   = BASE + "/messages/" + messageId + "?format=full";
        HttpResponse<String> resp = get(url, token);
        return extractPlainBody(resp.body());
    }

    // ── Send ─────────────────────────────────────────────────────────────────

    /**
     * Sends an email using Gmail API (POST /messages/send).
     * The message is formatted as RFC 2822 MIME, then base64url-encoded.
     * Returns the sent message ID.
     */
    public String sendEmail(String to, String subject, String body) throws Exception {
        String token     = oauth.getAccessToken();
        String fromEmail = oauth.getUserEmail();

        // Build RFC 2822 MIME
        String mime = "From: " + fromEmail + "\r\n"
                + "To: "      + to         + "\r\n"
                + "Subject: " + mimeEncode(subject) + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: quoted-printable\r\n"
                + "\r\n"
                + body;

        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mime.getBytes(StandardCharsets.UTF_8));
        String json = "{\"raw\":\"" + encoded + "\"}";

        HttpResponse<String> resp = http.send(
                HttpRequest.newBuilder(URI.create(BASE + "/messages/send"))
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (resp.statusCode() >= 300) {
            throw new Exception("Gmail send error " + resp.statusCode()
                    + ": " + extractErrorMessage(resp.body()));
        }
        return GmailOAuthService.extractJson(resp.body(), "id");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private MailMessage fetchMessageMeta(String id, String token) throws Exception {
        String url = BASE + "/messages/" + id
                + "?format=metadata"
                + "&metadataHeaders=From"
                + "&metadataHeaders=To"
                + "&metadataHeaders=Subject"
                + "&metadataHeaders=Date";

        HttpResponse<String> resp = get(url, token);
        String raw = resp.body();

        String from    = extractHeader(raw, "From");
        String to      = extractHeader(raw, "To");
        String subject = extractHeader(raw, "Subject");
        String date    = formatDate(extractHeader(raw, "Date"));
        String snippet = unescapeJson(GmailOAuthService.extractJson(raw, "snippet"));

        return new MailMessage(from, to, subject, snippet, date, false, id);
    }

    private HttpResponse<String> get(String url, String token) throws Exception {
        return http.send(
                HttpRequest.newBuilder(URI.create(url))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // ── JSON parsing (no external library) ────────────────────────────────────

    private List<String> extractMessageIds(String json) {
        List<String> ids = new ArrayList<>();
        // The response looks like: {"messages":[{"id":"abc","threadId":"xyz"},…]}
        int start = json.indexOf("\"messages\"");
        if (start < 0) return ids;
        int arrStart = json.indexOf("[", start);
        if (arrStart < 0) return ids;
        // Walk through each {"id":"..."} object
        int pos = arrStart;
        while (true) {
            int idIdx = json.indexOf("\"id\"", pos);
            if (idIdx < 0) break;
            int colon = json.indexOf(":", idIdx);
            int q1    = json.indexOf("\"", colon + 1);
            int q2    = json.indexOf("\"", q1 + 1);
            if (q1 < 0 || q2 < 0) break;
            ids.add(json.substring(q1 + 1, q2));
            pos = q2 + 1;
        }
        return ids;
    }

    /** Extracts a header value from the Gmail metadata JSON. */
    private String extractHeader(String json, String headerName) {
        // headers array: [{"name":"From","value":"..."},…]
        int headersStart = json.indexOf("\"headers\"");
        if (headersStart < 0) return "";
        int arrStart = json.indexOf("[", headersStart);
        if (arrStart < 0) return "";
        int arrEnd = findMatchingBracket(json, arrStart);
        String headers = json.substring(arrStart, arrEnd);

        int pos = 0;
        while (true) {
            int nameIdx = headers.indexOf("\"name\"", pos);
            if (nameIdx < 0) break;
            int nc = headers.indexOf(":", nameIdx);
            int nq1 = headers.indexOf("\"", nc + 1) + 1;
            int nq2 = headers.indexOf("\"", nq1);
            if (nq2 < 0) break;
            String name = headers.substring(nq1, nq2);
            if (name.equalsIgnoreCase(headerName)) {
                int valIdx = headers.indexOf("\"value\"", nq2);
                if (valIdx < 0) { pos = nq2 + 1; continue; }
                int vc  = headers.indexOf(":", valIdx);
                int vq1 = headers.indexOf("\"", vc + 1) + 1;
                int vq2 = vq1;
                while (vq2 < headers.length()) {
                    if (headers.charAt(vq2) == '"' && headers.charAt(vq2 - 1) != '\\') break;
                    vq2++;
                }
                return unescapeJson(headers.substring(vq1, vq2));
            }
            pos = nq2 + 1;
        }
        return "";
    }

    /** Extracts plain text from a full-format Gmail message. */
    private String extractPlainBody(String json) {
        // Look for "mimeType":"text/plain" and its data
        int pos = 0;
        while (true) {
            int idx = json.indexOf("\"mimeType\"", pos);
            if (idx < 0) break;
            int colon = json.indexOf(":", idx);
            int q1    = json.indexOf("\"", colon + 1) + 1;
            int q2    = json.indexOf("\"", q1);
            String mime = json.substring(q1, q2);
            if ("text/plain".equals(mime)) {
                int dataIdx = json.indexOf("\"data\"", idx);
                if (dataIdx >= 0) {
                    int dc  = json.indexOf(":", dataIdx);
                    int dq1 = json.indexOf("\"", dc + 1) + 1;
                    int dq2 = json.indexOf("\"", dq1);
                    String b64 = json.substring(dq1, dq2)
                            .replace('-', '+').replace('_', '/');
                    try {
                        return new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
                    } catch (Exception ignored) {}
                }
            }
            pos = q2 + 1;
        }
        return "";
    }

    private int findMatchingBracket(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            if (s.charAt(i) == '[') depth++;
            else if (s.charAt(i) == ']') { if (--depth == 0) return i + 1; }
        }
        return s.length();
    }

    private String unescapeJson(String s) {
        return s.replace("\\n", "\n").replace("\\r", "").replace("\\\"", "\"")
                .replace("\\t", "\t").replace("\\\\", "\\").replace("\\u003e", ">")
                .replace("\\u003c", "<").replace("\\u0026", "&");
    }

    /**
     * Converts RFC 2822 date strings to "dd/MM HH:mm" for compact display.
     * Falls back to the raw string on parse failure.
     */
    private String formatDate(String raw) {
        if (raw == null || raw.isBlank()) return "";
        try {
            // RFC 2822: "Mon, 10 Apr 2026 14:23:45 +0000"
            java.text.SimpleDateFormat in  = new java.text.SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd/MM HH:mm");
            return out.format(in.parse(raw.trim()));
        } catch (Exception ignored) {
            // Try without day-of-week
            try {
                java.text.SimpleDateFormat in  = new java.text.SimpleDateFormat(
                        "dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
                java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd/MM HH:mm");
                return out.format(in.parse(raw.trim()));
            } catch (Exception e2) {
                return raw.length() > 16 ? raw.substring(0, 16) : raw;
            }
        }
    }

    /** RFC 2047 encoding for non-ASCII subjects. */
    private String mimeEncode(String subject) {
        for (char c : subject.toCharArray()) {
            if (c > 127) {
                return "=?UTF-8?B?"
                        + Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8))
                        + "?=";
            }
        }
        return subject;
    }

    private String extractErrorMessage(String json) {
        String msg = GmailOAuthService.extractJson(json, "message");
        return msg.isBlank() ? json : msg;
    }
}
