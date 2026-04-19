package com.learnhub.controller.admin.partenaires;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Partenaire;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PartenaireFormController {

    @FXML private TextField searchNominatimField;
    @FXML private ListView<String> nominatimResultsList;
    @FXML private WebView mapWebView;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> secteurBox;
    @FXML private ComboBox<String> statutBox;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextField adresseField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField websiteField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Label mapInfoLabel;
    @FXML private Label nomError;
    @FXML private Label emailError;
    @FXML private Label secteurError;
    @FXML private Button saveButton;
    @FXML private Label titleIcon;
    @FXML private Label titleLabel;

    private final List<Map<String, String>> parsedResults = new ArrayList<>();
    private Partenaire partenaire;
    private boolean edit;
    private Stage stage;
    private Runnable onSaveCallback;
    private final PartenaireDAO dao = new PartenaireDAO();
    private MapBridge mapBridge;

    public void initData(Partenaire partenaire, boolean edit, Stage stage, Runnable onSaveCallback) {
        this.partenaire = partenaire;
        this.edit = edit;
        this.stage = stage;
        this.onSaveCallback = onSaveCallback;

        secteurBox.getItems().addAll("Technologie", "Finance", "Commerce", "Sante", "Education", "General");
        statutBox.getItems().addAll("actif", "inactif", "en_attente");

        if (edit && partenaire != null) {
            if (titleIcon != null) titleIcon.setText("✏");
            if (titleLabel != null) titleLabel.setText("Modifier le Partenaire");
            if (saveButton != null) saveButton.setText("Enregistrer les modifications");

            nomField.setText(safe(partenaire.getNom()));
            secteurBox.setValue(safe(partenaire.getSecteur()));
            statutBox.setValue(partenaire.getStatut() != null ? partenaire.getStatut() : "actif");
            villeField.setText(safe(partenaire.getVille()));
            paysField.setText(safe(partenaire.getPays()));
            adresseField.setText(safe(partenaire.getAdresse()));
            emailField.setText(safe(partenaire.getEmail()));
            telephoneField.setText(safe(partenaire.getTelephone()));
            websiteField.setText(safe(partenaire.getWebsite()));
            descriptionArea.setText(safe(partenaire.getDescription()));
            latitudeField.setText(safe(partenaire.getLatitude()));
            longitudeField.setText(safe(partenaire.getLongitude()));
        } else {
            statutBox.setValue("actif");
        }

        setupNominatimSearch();
        Platform.runLater(this::loadMap);
    }

    private void setupNominatimSearch() {
        if (searchNominatimField != null) {
            searchNominatimField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.trim().length() >= 3) {
                    searchNominatim(newVal.trim());
                } else {
                    hideNominatimList();
                }
            });
        }

        if (nominatimResultsList != null) {
            nominatimResultsList.setVisible(false);
            nominatimResultsList.setManaged(false);
            nominatimResultsList.setOnMouseClicked(event -> {
                int idx = nominatimResultsList.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && idx < parsedResults.size()) {
                    fillFromNominatim(parsedResults.get(idx));
                    hideNominatimList();
                }
            });
        }
    }

    private void loadMap() {
        if (mapWebView == null) return;

        WebEngine engine = mapWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        mapBridge = new MapBridge(this);

        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", mapBridge);

                // Force multiple invalidateSize to fix gray areas initially
                for (int i = 0; i < 12; i++) {
                    final int delay = i * 100;
                    Platform.runLater(() -> {
                        try {
                            engine.executeScript("if (typeof map !== 'undefined') { map.invalidateSize(); }");
                        } catch (Exception ignored) {}
                    });
                }

                // Center map if coordinates exist
                if (!latitudeField.getText().isEmpty() && !longitudeField.getText().isEmpty()) {
                    try {
                        double lat = Double.parseDouble(latitudeField.getText());
                        double lng = Double.parseDouble(longitudeField.getText());
                        engine.executeScript("setView(" + lat + ", " + lng + ")");
                    } catch (Exception ignored) {}
                }
            }
        });

        // 🚀 BULLETPROOF FIX FOR GRAY TILES: Listen to JavaFX UI resizing and push it instantly to Leaflet
        mapWebView.widthProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                try { engine.executeScript("if (typeof map !== 'undefined') { map.invalidateSize(); }"); } catch(Exception ignored){}
            });
        });
        mapWebView.heightProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                try { engine.executeScript("if (typeof map !== 'undefined') { map.invalidateSize(); }"); } catch(Exception ignored){}
            });
        });

        engine.loadContent(buildLeafletHtml());
    }

    private String buildLeafletHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.15.1/css/ol.css" type="text/css">
                <script src="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.15.1/build/ol.js"></script>
                <style>
                    html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background: #f8fafc; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    window.addEventListener('load', function() {
                        var vectorSource = new ol.source.Vector();
                        var vectorLayer = new ol.layer.Vector({ source: vectorSource, zIndex: 100 });

                        var map = new ol.Map({
                            target: 'map',
                            layers: [
                                new ol.layer.Tile({ 
                                    source: new ol.source.XYZ({ 
                                        url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
                                        attributions: 'Tiles &copy; Esri'
                                    }) 
                                }),
                                vectorLayer
                            ],
                            view: new ol.View({
                                center: ol.proj.fromLonLat([10.1815, 36.8065]),
                                zoom: 7
                            })
                        });

                        window.map = map;

                        var markerFeature = new ol.Feature();
                        var markerStyle = new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 8,
                                fill: new ol.style.Fill({ color: '#2563eb' }),
                                stroke: new ol.style.Stroke({ color: 'white', width: 2 })
                            })
                        });
                        markerFeature.setStyle(markerStyle);
                        vectorSource.addFeature(markerFeature);
                        
                        // Default position offscreen until placed
                        markerFeature.setGeometry(null);

                        window.setView = function(lat, lng) {
                            var coord = ol.proj.fromLonLat([lng, lat]);
                            map.getView().setCenter(coord);
                            map.getView().setZoom(15);
                            markerFeature.setGeometry(new ol.geom.Point(coord));
                            if (window.javaBridge) window.javaBridge.onMapClick(lat, lng);
                        };

                        map.on('singleclick', function(evt) {
                            var lonLat = ol.proj.toLonLat(evt.coordinate);
                            var lon = lonLat[0];
                            var lat = lonLat[1];
                            markerFeature.setGeometry(new ol.geom.Point(evt.coordinate));
                            if (window.javaBridge) window.javaBridge.onMapClick(lat, lon);
                        });
                        
                        window.addEventListener('resize', function() {
                            setTimeout(function() { map.updateSize(); }, 50);
                        });
                    });
                </script>
            </body>
            </html>
            """;
    }

    public class MapBridge {
        private final PartenaireFormController controller;
        public MapBridge(PartenaireFormController controller) {
            this.controller = controller;
        }

        public void onMapClick(double lat, double lng) {
            Platform.runLater(() -> {
                controller.latitudeField.setText(String.valueOf(lat));
                controller.longitudeField.setText(String.valueOf(lng));
                controller.mapInfoLabel.setText("📍 Position sélectionnée : " +
                        String.format("%.6f", lat) + ", " + String.format("%.6f", lng));
            });
        }
    }

    // ==================== GEOLOCATION SEARCH (ARCGIS) ====================
    private void searchNominatim(String query) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates?f=json&SingleLine=" + encoded + "&outFields=Match_addr,City,Type,Country,Region,Postal&maxLocations=6";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "LearnHub-JavaFX/1.0")
                        .GET()
                        .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                List<Map<String, String>> results = parseArcGisJson(resp.body());
                Platform.runLater(() -> showNominatimResults(results));
                return null;
            }
        };
        new Thread(task).start();
    }

    private List<Map<String, String>> parseArcGisJson(String json) {
        List<Map<String, String>> list = new ArrayList<>();
        if (json == null || json.isBlank() || !json.contains("\"candidates\"")) return list;

        int startPos = json.indexOf("\"candidates\"");
        int arrStart = json.indexOf("[", startPos);
        int arrEnd = json.lastIndexOf("]");
        if (arrStart < 0 || arrEnd < 0 || arrEnd <= arrStart) return list;
        
        String content = json.substring(arrStart + 1, arrEnd).trim();
        if (content.isEmpty()) return list;

        List<String> objects = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (char c : content.toCharArray()) {
            if (c == '{') depth++;
            if (depth > 0) current.append(c);
            if (c == '}') {
                depth--;
                if (depth == 0) {
                    objects.add(current.toString());
                    current.setLength(0);
                }
            }
        }

        for (String part : objects) {
            Map<String, String> map = new LinkedHashMap<>();
            
            // ArcGIS returns general 'address' which is good for display_name
            String address = extractJsonString(part, "\"address\"");
            map.put("display_name", address.isEmpty() ? extractJsonString(part, "\"Match_addr\"") : address);
            
            map.put("name", map.get("display_name").split(",")[0].trim());
            map.put("city", extractJsonString(part, "\"City\""));
            map.put("country", extractJsonString(part, "\"Country\""));
            map.put("road", extractJsonString(part, "\"Match_addr\"").split(",")[0].trim());
            map.put("postcode", extractJsonString(part, "\"Postal\""));
            
            // Extract coordinates from Location block
            map.put("lon", extractJsonString(part, "\"x\""));
            map.put("lat", extractJsonString(part, "\"y\""));
            
            if (!map.getOrDefault("display_name", "").isBlank()) {
                list.add(map);
            }
            if (list.size() >= 6) break;
        }
        parsedResults.clear();
        parsedResults.addAll(list);
        return list;
    }

    private String extractJsonString(String json, String key) {
        // If key doesn't have quotes already, wrap it
        String search = key.startsWith("\"") ? key : "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        
        int colon = json.indexOf(":", idx + search.length());
        if (colon < 0) return "";
        
        String rest = json.substring(colon + 1).trim();
        if (rest.startsWith("\"")) {
            int endQuote = rest.indexOf("\"", 1);
            if (endQuote > 0) return rest.substring(1, endQuote).replace("\\\"", "\"");
        } else {
            // Might be a number, boolean, or null
            int comma = rest.indexOf(",");
            int brace = rest.indexOf("}");
            int end = -1;
            if (comma >= 0 && brace >= 0) end = Math.min(comma, brace);
            else if (comma >= 0) end = comma;
            else if (brace >= 0) end = brace;
            
            if (end > 0) {
                String val = rest.substring(0, end).trim();
                return val.equals("null") ? "" : val;
            }
        }
        return "";
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }

    private void showNominatimResults(List<Map<String, String>> results) {
        if (nominatimResultsList == null) return;
        nominatimResultsList.getItems().clear();
        if (results.isEmpty()) {
            hideNominatimList();
            return;
        }
        for (Map<String, String> r : results) {
            String display = r.get("display_name");
            if (display.length() > 70) display = display.substring(0, 67) + "...";
            nominatimResultsList.getItems().add(display);
        }
        nominatimResultsList.setVisible(true);
        nominatimResultsList.setManaged(true);
        nominatimResultsList.setPrefHeight(Math.min(results.size() * 35 + 10, 200));
    }

    private void hideNominatimList() {
        if (nominatimResultsList != null) {
            nominatimResultsList.setVisible(false);
            nominatimResultsList.setManaged(false);
        }
    }

    private void fillFromNominatim(Map<String, String> result) {
        if (result == null) return;

        String lat = result.getOrDefault("lat", "");
        String lon = result.getOrDefault("lon", "");

        if (!lat.isEmpty() && !lon.isEmpty()) {
            latitudeField.setText(lat);
            longitudeField.setText(lon);
            if (mapWebView != null) {
                try {
                    mapWebView.getEngine().executeScript("setView(" + lat + ", " + lon + ")");
                } catch (Exception ignored) {}
            }
        }

        if (villeField != null) {
            villeField.setText(result.getOrDefault("city", ""));
        }
        if (paysField != null) {
            paysField.setText(result.getOrDefault("country", ""));
        }
        if (adresseField != null) {
            String road = result.getOrDefault("road", "");
            String pc = result.getOrDefault("postcode", "");
            adresseField.setText((road + " " + pc).trim());
        }

        String dn = result.getOrDefault("display_name", "");
        if (searchNominatimField != null) {
            searchNominatimField.setText(dn.length() > 60 ? dn.substring(0, 57) + "..." : dn);
        }
        
        // Auto-fill optional details like phone and name if they exist
        if (nomField != null && (nomField.getText() == null || nomField.getText().isBlank())) {
            nomField.setText(result.getOrDefault("name", ""));
        }
        if (telephoneField != null && (telephoneField.getText() == null || telephoneField.getText().isBlank())) {
            telephoneField.setText(result.getOrDefault("telephone", ""));
        }

        hideNominatimList();
    }

    @FXML
    public void handleCreate() {
        boolean valid = true;
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomError.setVisible(true);
            valid = false;
        } else {
            nomError.setVisible(false);
        }

        if (emailField.getText() == null || emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            emailError.setVisible(true);
            valid = false;
        } else {
            emailError.setVisible(false);
        }

        if (!valid) return;

        partenaire.setNom(nomField.getText().trim());
        partenaire.setSecteur(secteurBox.getValue());
        partenaire.setStatut(statutBox.getValue());
        partenaire.setVille(villeField.getText().trim());
        partenaire.setPays(paysField.getText().trim());
        partenaire.setAdresse(adresseField.getText().trim());
        partenaire.setEmail(emailField.getText().trim());
        partenaire.setTelephone(telephoneField.getText().trim());
        partenaire.setWebsite(websiteField.getText().trim());
        partenaire.setDescription(descriptionArea.getText().trim());
        partenaire.setLatitude(latitudeField.getText().trim());
        partenaire.setLongitude(longitudeField.getText().trim());

        try {
            if (edit) dao.update(partenaire);
            else dao.insert(partenaire);

            if (onSaveCallback != null) onSaveCallback.run();
            if (stage != null) stage.close();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Enregistrement impossible : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        if (stage != null) stage.close();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}