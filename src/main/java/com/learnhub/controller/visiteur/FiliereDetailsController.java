package com.learnhub.controller.visiteur;

import com.learnhub.models.Universite;
import com.learnhub.models.Filiere;
import com.learnhub.service.FilierePDFService;
import com.learnhub.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.awt.Desktop;
import java.net.URI;

public class FiliereDetailsController {

    @FXML private BorderPane rootPane;
    @FXML private Label fNameLabel;
    @FXML private Label fCodeLabel;
    @FXML private Label fDescriptionLabel;
    @FXML private Label fDurationLabel;
    @FXML private Label fLevelLabel;
    @FXML private Button videoBtn;

    private Filiere filiere;

    public void setFiliere(Filiere f) {
        this.filiere = f;
        if (f != null) {
            fNameLabel.setText(f.getNom());
            fCodeLabel.setText(f.getCode());
            fDescriptionLabel.setText(f.getDescription() != null && !f.getDescription().isEmpty() 
                                    ? f.getDescription() 
                                    : "Programme d'excellence académique combinant théorie et pratique pour former les futurs cadres du secteur.");
            fDurationLabel.setText(f.getDureeAnnees() + " ans");
            fLevelLabel.setText(f.getNiveau());
            
            if (f.getVideoUrl() == null || f.getVideoUrl().isEmpty()) {
                videoBtn.setDisable(false);
                videoBtn.setOpacity(1.0);
                videoBtn.setText("🎬 Rechercher une vidéo");
            } else {
                videoBtn.setDisable(false);
                videoBtn.setOpacity(1.0);
                videoBtn.setText("🎬 Voir la vidéo");
            }
        }
    }

    @FXML
    private void handlePlayVideo() {
        if (filiere == null) return;

        String videoUrl = filiere.getVideoUrl();
        String videoId = null;

        // 1. Check if we need to search YouTube/Fallback
        if (videoUrl == null || videoUrl.isEmpty() || videoUrl.contains("oceans.mp4") || videoUrl.contains("vimeo.com/")) {
            System.out.println("Searching/Fallback for relevant video: " + filiere.getNom());
            videoId = com.learnhub.service.YouTubeService.searchVideo(filiere.getNom());
            
            if (videoId != null) {
                if (videoId.startsWith("vi:")) {
                    videoUrl = "https://vimeo.com/" + videoId.substring(3);
                } else if (videoId.startsWith("dm:")) {
                    videoUrl = "https://www.dailymotion.com/video/" + videoId.substring(3);
                } else {
                    videoUrl = "https://www.youtube.com/watch?v=" + videoId;
                }
            } else {
                // If API key is missing or no fallback found, redirect to a YouTube Search results page
                try {
                    String searchQuery = java.net.URLEncoder.encode(filiere.getNom() + " formation filière", "UTF-8");
                    videoUrl = "https://www.youtube.com/results?search_query=" + searchQuery;
                } catch (java.io.UnsupportedEncodingException e) {
                    videoUrl = "https://www.youtube.com/results?search_query=" + filiere.getNom().replace(" ", "+");
                }
                System.out.println("Redirecting to YouTube Search: " + videoUrl);
            }
        } else if (videoUrl.contains("youtube.com/embed/")) {
            // 2. Convert embed URL to watch URL if it's a YouTube link
            videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            if (videoId.contains("?")) videoId = videoId.substring(0, videoId.indexOf("?"));
            videoUrl = "https://www.youtube.com/watch?v=" + videoId;
        }

        // 3. Open in System Browser
        if (videoUrl != null && !videoUrl.isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(videoUrl));
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to old WebView if browser fails (rare)
                showVideoInWebView(videoUrl);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Vidéo non trouvée");
            alert.setHeaderText(null);
            alert.setContentText("Aucune vidéo n'a pu être trouvée pour cette filière.");
            alert.show();
        }
    }

    private void showVideoInWebView(String url) {
        Stage videoStage = new Stage();
        videoStage.setTitle("Vidéo explicative - " + filiere.getNom());
        videoStage.initModality(Modality.APPLICATION_MODAL);
        videoStage.initOwner(rootPane.getScene().getWindow());

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        
        // Basic embed wrapper
        String content = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<style>body, html { margin: 0; padding: 0; height: 100%; overflow: hidden; background: #000; }</style>" +
                "</head><body>" +
                "<iframe width='100%' height='100%' src='" + url + "?autoplay=1' " +
                "frameborder='0' allow='autoplay; fullscreen' allowfullscreen></iframe>" +
                "</body></html>";
        
        engine.loadContent(content);
        Scene scene = new Scene(webView, 950, 600);
        videoStage.setScene(scene);
        videoStage.show();
    }

    @FXML
    private void handleDownloadPDF() {
        if (filiere == null) return;
        String filePath = FilierePDFService.generateFilierePDF(filiere);
        if (filePath != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Téléchargement réussi");
            alert.setHeaderText("La brochure PDF a été générée.");
            alert.setContentText("Fichier : " + filePath);
            alert.show();
            
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleApply() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/visiteur/candidature_form.fxml"));
            Parent root = loader.load();
            CandidatureFormController controller = loader.getController();
            controller.setFiliere(filiere);

            Stage stage = new Stage();
            stage.setTitle("Postuler - " + filiere.getNom());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (filiere == null) {
            goHome();
            return;
        }
        
        try {
            Universite u = new com.learnhub.dao.UniversiteDAO().findById(filiere.getUniversiteId());
            if (u != null) {
                FXMLLoader loader = NavigationUtil.loadFXML("/fxml/visiteur/universite_details.fxml");
                Parent root = loader.load();
                UniversiteDetailsController controller = loader.getController();
                controller.setUniversite(u);
                
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.getScene().setRoot(root);
            } else {
                goHome();
            }
        } catch (Exception e) {
            e.printStackTrace();
            goHome();
        }
    }

    @FXML private void goHome() {
        NavigationUtil.navigateTo((Stage) rootPane.getScene().getWindow(), "/fxml/visiteur/home.fxml", "LearnHub - Accueil");
    }

    @FXML private void goLogin() {
        NavigationUtil.navigateTo((Stage) rootPane.getScene().getWindow(), "/fxml/auth/login.fxml", "Connexion");
    }
}
