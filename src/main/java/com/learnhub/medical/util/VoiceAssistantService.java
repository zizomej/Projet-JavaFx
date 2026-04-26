package com.learnhub.medical.util;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.repository.RDVRepository;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceAssistantService {

    // Simple cache to avoid hitting the API multiple times for the same question
    private static final Map<String, String> audioCache = new HashMap<>();
    private MediaPlayer currentPlayer;

    public void generateResponse(String question, int userId, java.util.function.Consumer<String> onTextGenerated) {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() {
                try {
                    String responseText = analyzeQuestion(question.toLowerCase(), userId);
                    if (onTextGenerated != null) {
                        javafx.application.Platform.runLater(() -> onTextGenerated.accept(responseText));
                    }
                } catch (Exception e) {
                    System.err.println("!!! Erreur VoiceAssistant : " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void playAudioForText(String text) {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Nettoyer les emojis pour éviter de faire planter le TTS Google (qui les gère mal)
                    String cleanText = text.replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}]", "").trim();
                    String cacheKey = cleanText.hashCode() + "";

                    if (audioCache.containsKey(cacheKey)) {
                        playAudio(audioCache.get(cacheKey));
                        return null;
                    }

                    String encodedText = URLEncoder.encode(cleanText, StandardCharsets.UTF_8.toString());
                    if (encodedText.length() > 200) encodedText = encodedText.substring(0, 200);

                    String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=fr-FR&q=" + encodedText;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                    if (conn.getResponseCode() == 200) {
                        File tempFile = File.createTempFile("tts_" + cacheKey, ".mp3");
                        tempFile.deleteOnExit();

                        try (InputStream in = conn.getInputStream();
                             FileOutputStream out = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }

                        audioCache.put(cacheKey, tempFile.getAbsolutePath());
                        playAudio(tempFile.getAbsolutePath());
                    } else {
                        System.err.println("!!! Erreur API TTS Code : " + conn.getResponseCode());
                    }

                } catch (Exception e) {
                    System.err.println("!!! Erreur Synthèse Vocale : " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void playAudio(String filePath) {
        javafx.application.Platform.runLater(() -> {
            try {
                Media media = new Media(new File(filePath).toURI().toString());
                if (currentPlayer != null) {
                    currentPlayer.stop();
                }
                currentPlayer = new MediaPlayer(media);
                currentPlayer.setOnEndOfMedia(() -> currentPlayer.dispose());
                currentPlayer.play();
            } catch (Exception e) {
                System.err.println("!!! Erreur MediaPlayer : " + e.getMessage());
            }
        });
    }

    private int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];
        for (int i = 0; i <= lhs.length(); i++) distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++) distance[0][j] = j;
        for (int i = 1; i <= lhs.length(); i++) {
            for (int j = 1; j <= rhs.length(); j++) {
                distance[i][j] = Math.min(Math.min(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));
            }
        }
        return distance[lhs.length()][rhs.length()];
    }

    private boolean match(String input, String keyword) {
        if (input.contains(keyword)) return true;
        String[] words = input.split("\\s+");
        for (String w : words) {
            if (computeLevenshteinDistance(w, keyword) <= 2 && keyword.length() >= 4) return true;
        }
        return false;
    }

    private String analyzeQuestion(String q, int userId) {
        RDVRepository repo = new RDVRepository();
        com.learnhub.medical.repository.CreneauRepository creneauRepo = new com.learnhub.medical.repository.CreneauRepository();
        List<RDV> rdvs = new java.util.ArrayList<>();
        try {
            rdvs = repo.findAll(); // Pour la démo, on cherche le prochain
        } catch (Exception e) {
            System.err.println("Erreur fetch RDV: " + e.getMessage());
        }
        
        // Find next RDV for user
        RDV nextRdv = null;
        com.learnhub.medical.entity.Creneau nextCreneau = null;
        LocalDate today = LocalDate.now();

        for (RDV r : rdvs) {
            if (r.getEtudiantId() == userId && ("Confirmé".equals(r.getStatut()) || "En attente".equals(r.getStatut()))) {
                try {
                    com.learnhub.medical.entity.Creneau c = creneauRepo.findById(r.getCreneauId());
                    if (c != null && c.getJour() != null && !c.getJour().isEmpty()) {
                        LocalDate rdvDate;
                        try {
                            rdvDate = LocalDate.parse(c.getJour());
                        } catch (Exception parseEx) {
                            continue; // format de date invalide
                        }

                        if (!rdvDate.isBefore(today)) {
                            if (nextRdv == null || nextCreneau == null || rdvDate.isBefore(LocalDate.parse(nextCreneau.getJour()))) {
                                nextRdv = r;
                                nextCreneau = c;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur recherche créneau: " + e.getMessage());
                }
            }
        }

        // Reconnaissance des intentions - Réponses enrichies et professionnelles (avec Emojis)
        if (match(q, "bonjour") || match(q, "salut") || match(q, "coucou") || match(q, "bonsoir") || match(q, "hey") || match(q, "hello")) {
            return "Bonjour ! Comment puis-je faciliter votre parcours medical aujourd'hui ?";
        } else if ((match(q, "prochain") || match(q, "futur") || match(q, "quand") || match(q, "premier")) && (match(q, "rdv") || match(q, "rendez-vous"))) {
            if (nextCreneau != null) {
                String heure = nextCreneau.getHeure() != null ? nextCreneau.getHeure().toString() : "flexible";
                String jour = nextCreneau.getJour();
                return "Votre prochain rendez-vous confirme est le " + jour + " a " + heure + ". N'oubliez pas d'arriver 5 minutes a l'avance.";
            } else {
                return "Vous n'avez aucun rendez-vous clinique prevu pour le moment.";
            }
        } else if ((match(q, "combien") || match(q, "nombre") || match(q, "calcul")) && (match(q, "rdv") || match(q, "rendez-vous"))) {
            long count = rdvs.stream().filter(r -> r.getEtudiantId() == userId).count();
            if(count == 0) return "Votre dossier est vide. Aucun rendez-vous n'est enregistre.";
            if(count == 1) return "Je vois que vous avez un seul rendez-vous planifie dans le systeme.";
            return "Genial ! Vous avez " + count + " rendez-vous au total.";
        } else if (match(q, "dispos") || match(q, "disponible") || match(q, "disponibles") || match(q, "libre") || match(q, "place") || match(q, "creneaux") || match(q, "creneau")) {
            return "Pour voir les creneaux libres, consultez directement la grille verte sur le calendrier de gauche !";
        } else if (match(q, "annuler") || match(q, "supprimer") || match(q, "effacer") || match(q, "enlever") || match(q, "deplacer")) {
            return "Pour annuler un creneau, rendez-vous dans l'onglet Mes rendez-vous dans le menu de gauche, puis cliquez sur le bouton rouge Annuler.";
        } else if (match(q, "prendre") || match(q, "reserver") || match(q, "recommandation") || match(q, "nouveau") || match(q, "ajouter")) {
            return "Pour reserver, selectionnez un creneau disponible (vert) dans la grille du calendrier. Nous gerons le reste.";
        } else if (match(q, "urgence") || match(q, "grave") || match(q, "douleur") || match(q, "mal") || match(q, "vite") || match(q, "secours") || match(q, "sang")) {
            return "En cas d'urgence medicale grave, veuillez appeler le SAMU (15) ou le service europeen (112) immediatement.";
        } else if (match(q, "merci") || match(q, "super") || match(q, "parfait") || match(q, "bien")) {
            return "Je vous en prie ! C'est un plaisir de vous aider. Prenez soin de vous !";
        } else {
            return "Je n'ai pas tres bien saisi. Mon systeme comprend les requetes concernant vos rendez-vous, disponibilites et urgences. Pouvez-vous reformuler ?";
        }
    }
}
