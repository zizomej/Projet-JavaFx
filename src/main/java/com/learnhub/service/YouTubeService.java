package com.learnhub.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class YouTubeService {
    // IMPORTANT: Replace with your actual Google API Key
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String APP_NAME = "LearnHub-University-Manager";

    private static YouTube youtubeService;
    private static final java.util.Map<String, String> FALLBACK_VIDEOS = new java.util.HashMap<>();

    static {
        // Fallback IDs for common educational topics
        // Now using YouTube IDs exclusively (Browser redirection handles playback perfectly)
        // Using ASCII keywords to avoid encoding issues with accents
        FALLBACK_VIDEOS.put("informatique", "zOjov-2OZ0E"); 
        FALLBACK_VIDEOS.put("computer", "zOjov-2OZ0E");
        FALLBACK_VIDEOS.put("business", "jUInXp8UAn0"); 
        FALLBACK_VIDEOS.put("management", "jUInXp8UAn0");
        FALLBACK_VIDEOS.put("ingenieur", "3p_AHe26-O8"); // No accent
        FALLBACK_VIDEOS.put("ingénieur", "3p_AHe26-O8"); // With accent
        FALLBACK_VIDEOS.put("engineering", "3p_AHe26-O8");
        FALLBACK_VIDEOS.put("math", "E0CazRHB07U");
        FALLBACK_VIDEOS.put("science", "E0CazRHB07U");
        FALLBACK_VIDEOS.put("universit", "zOjov-2OZ0E"); // "universite" or "university"
    }

    public static String searchVideo(String query) {
        // 1. Try Fallback first if query matches common keywords (zero-cost, no API key needed)
        String lowerQuery = query.toLowerCase();
        for (java.util.Map.Entry<String, String> entry : FALLBACK_VIDEOS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                System.out.println("YouTube: Using smart fallback for keyword '" + entry.getKey() + "'");
                return entry.getValue();
            }
        }

        // 2. Try official API only if Key is provided
        if (API_KEY == null || API_KEY.equals("YOUR_API_KEY_HERE")) {
            System.err.println("YouTube API Key missing. Fallback search also failed.");
            return null;
        }

        try {
            if (youtubeService == null) {
                youtubeService = new YouTube.Builder(new NetHttpTransport(), new GsonFactory(), null)
                        .setApplicationName(APP_NAME)
                        .build();
            }

            YouTube.Search.List search = youtubeService.search().list(Collections.singletonList("id,snippet"));
            search.setKey(API_KEY);
            search.setQ(query + " educational overview");
            search.setType(Collections.singletonList("video"));
            search.setMaxResults(1L);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            if (results != null && !results.isEmpty()) {
                return results.get(0).getId().getVideoId();
            }
        } catch (IOException e) {
            System.err.println("Error during YouTube API search: " + e.getMessage());
        }
        return null;
    }
}
