package com.learnhub.medical.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour l'intégration de Stripe.
 * Communique avec le backend Symfony pour générer des sessions de paiement.
 */
public class StripeIntegration {

    // URL du backend Symfony (à adapter selon l'environnement)
    private static final String SYMFONY_BASE_URL = "http://127.0.0.1:8000";
    private static final String CHECKOUT_ENDPOINT = "/payment/checkout";

    private final HttpClient httpClient;

    public StripeIntegration() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * Appelle l'API Symfony pour créer une session de paiement Stripe.
     * @param amount Le montant en centimes (ex: 5000 pour 50€)
     * @param rdvTitle Le motif du RDV
     * @return Future contenant l'URL de la session de paiement Stripe
     */
    public CompletableFuture<String> createCheckoutSession(int amount, String rdvTitle) {
        String jsonPayload = String.format("{\"amount\":%d, \"title\":\"%s\"}", amount, rdvTitle);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SYMFONY_BASE_URL + CHECKOUT_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        // On suppose que le backend renvoie directement l'URL ou un JSON avec le champ 'url'
                        // Pour l'intégration, on simule l'extraction si c'est du JSON
                        String body = response.body();
                        if (body.contains("\"url\":\"")) {
                            return body.split("\"url\":\"")[1].split("\"")[0];
                        }
                        return body; // Si c'est juste l'URL brute
                    } else {
                        throw new RuntimeException("Erreur lors de la création de la session Stripe : " + response.statusCode());
                    }
                });
    }

    /**
     * Méthode de simulation pour la présentation si le backend n'est pas encore prêt.
     */
    public String getMockCheckoutUrl() {
        // Nouvelle URL de démo officielle pour votre présentation
        return "https://stripe.com/docs/payments/checkout"; 
    }
}
