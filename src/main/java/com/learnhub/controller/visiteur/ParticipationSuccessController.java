package com.learnhub.controller.visiteur;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.learnhub.models.Evenement;
import com.learnhub.models.Participation;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.sun.net.httpserver.HttpServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class ParticipationSuccessController {

    @FXML private Label eventTitleLabel;
    @FXML private Label userNameLabel;
    @FXML private ImageView qrCodeImage;
    @FXML private Label ticketIdLabel;

    private Evenement evenement;
    private Participation participation;
    private HttpServer server;

    public void setData(Evenement evenement, Participation participation) {
        this.evenement = evenement;
        this.participation = participation;

        String ticketId = "TK-" + evenement.getId() + "-" + (int)(Math.random() * 9000 + 1000);
        ticketIdLabel.setText("Ticket ID: #" + ticketId);
        eventTitleLabel.setText("Événement : " + evenement.getTitre());
        userNameLabel.setText("Participant : " + participation.getNom());

        // Always generate a QR code — use URL if server starts, text fallback otherwise
        String qrContent = "TICKET #" + ticketId + " - " + evenement.getTitre();

        try {
            // Try to start local HTML server
            String localIp = InetAddress.getLocalHost().getHostAddress();
            int port = 8888;

            // Stop any previous server instance safely
            try { if (server != null) server.stop(0); } catch (Exception ignored) {}

            String htmlTicket = buildHtmlTicket(ticketId, evenement, participation);

            // Try different ports if 8888 is busy
            HttpServer newServer = null;
            int usedPort = port;
            for (int p = 8888; p <= 8892; p++) {
                try {
                    newServer = HttpServer.create(new InetSocketAddress(p), 0);
                    usedPort = p;
                    break;
                } catch (Exception portEx) {
                    // port busy, try next
                }
            }

            if (newServer != null) {
                server = newServer;
                final String html = htmlTicket;
                server.createContext("/ticket", exchange -> {
                    byte[] response = html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                });
                server.setExecutor(null);
                server.start();
                qrContent = "http://" + localIp + ":" + usedPort + "/ticket";
                System.out.println("✅ Ticket accessible à : " + qrContent);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Serveur local non disponible, QR en mode texte : " + e.getMessage());
        }

        // Always load the QR code image
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" +
                            java.net.URLEncoder.encode(qrContent, "UTF-8");
            qrCodeImage.setImage(new Image(qrUrl, true));
        } catch (Exception e) {
            System.err.println("❌ Impossible de charger le QR code : " + e.getMessage());
        }
    }

    private String buildHtmlTicket(String ticketId, Evenement evenement, Participation participation) {
        String nom = participation.getNom() != null ? participation.getNom() : "---";
        String titre = evenement.getTitre() != null ? evenement.getTitre().toUpperCase() : "---";
        String date = evenement.getDateDebut() != null ? evenement.getDateDebut().toString() : "---";
        String lieu = evenement.getLieuNom() != null ? evenement.getLieuNom() : "---";

        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>Ticket LearnHub</title>" +
               "<style>" +
               "*{box-sizing:border-box;margin:0;padding:0;}" +
               "body{background:#d0d0d0;font-family:Arial,Helvetica,sans-serif;padding:20px;}" +
               ".page{background:white;max-width:440px;margin:0 auto;border:1px solid #ccc;}" +
               ".header{background:#1E3A8A;color:white;text-align:center;padding:18px 20px;font-size:16px;font-weight:900;letter-spacing:1px;text-transform:uppercase;}" +
               ".body{padding:30px 30px 20px 30px;}" +
               ".event-title{color:#1E3A8A;text-align:center;font-size:17px;font-weight:900;letter-spacing:2px;margin-bottom:22px;}" +
               ".grid{display:grid;grid-template-columns:1fr 1fr;gap:16px 20px;margin-bottom:20px;}" +
               ".field-label{font-size:11px;font-weight:900;text-transform:uppercase;color:#111;margin-bottom:2px;}" +
               ".field-value{font-size:13px;color:#222;}" +
               ".footer-line{border-top:1px solid #ccc;margin-top:10px;padding-top:14px;text-align:center;font-size:11px;font-style:italic;color:#666;}" +
               "</style></head><body>" +
               "<div class='page'>" +
               "<div class='header'>LEARNHUB - TICKET OFFICIEL</div>" +
               "<div class='body'>" +
               "<div class='event-title'>" + titre + "</div>" +
               "<div class='grid'>" +
               "<div><div class='field-label'>PARTICIPANT</div><div class='field-value'>" + nom + "</div></div>" +
               "<div><div class='field-label'>ID TICKET</div><div class='field-value'>" + ticketId + "</div></div>" +
               "<div><div class='field-label'>DATE</div><div class='field-value'>" + date + "</div></div>" +
               "<div><div class='field-label'>LIEU</div><div class='field-value'>" + lieu + "</div></div>" +
               "<div><div class='field-label'>NUMÉRO DE PLACE</div><div class='field-value'>ZONE A - RANGÉE 5 - SIÈGE 12</div></div>" +
               "<div><div class='field-label'>STATUT</div><div class='field-value'>CONFIRMÉ</div></div>" +
               "</div>" +
               "<div class='footer-line'>Ce ticket est strictement personnel. Veuillez le présenter à l'entrée.</div>" +
               "</div></div></body></html>";
    }

    @FXML
    private void handleSimulateScan() {
        try {
            File tempFile = File.createTempFile("Ticket_" + ticketIdLabel.getText().replace("#", "").replace(":", ""), ".pdf");
            generatePDF(tempFile);
            
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                new ProcessBuilder("cmd", "/c", "start", tempFile.getAbsolutePath()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le ticket");
            alert.setContentText("Le fichier a été généré mais ne peut pas être ouvert automatiquement.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDownloadPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Ticket");
        fileChooser.setInitialFileName("Ticket_" + evenement.getTitre().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(eventTitleLabel.getScene().getWindow());
        
        if (file != null) {
            try {
                generatePDF(file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("PDF Généré");
                alert.setContentText("Votre ticket a été enregistré sous : " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de la génération du PDF");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void generatePDF(File file) throws DocumentException, IOException {
        Document document = new Document(com.itextpdf.text.PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        com.itextpdf.text.BaseColor primaryColor = new com.itextpdf.text.BaseColor(30, 58, 138);
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
        Font sectionHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, primaryColor);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, com.itextpdf.text.BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, com.itextpdf.text.BaseColor.BLACK);

        com.itextpdf.text.pdf.PdfPTable mainTable = new com.itextpdf.text.pdf.PdfPTable(1);
        mainTable.setWidthPercentage(100);
        
        com.itextpdf.text.pdf.PdfPCell headerCell = new com.itextpdf.text.pdf.PdfPCell(new Paragraph("LEARNHUB - TICKET OFFICIEL", titleFont));
        headerCell.setBackgroundColor(primaryColor);
        headerCell.setPadding(20);
        headerCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        headerCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        mainTable.addCell(headerCell);

        com.itextpdf.text.pdf.PdfPCell contentCell = new com.itextpdf.text.pdf.PdfPCell();
        contentCell.setPadding(30);
        contentCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
        contentCell.setBorder(com.itextpdf.text.Rectangle.BOX);
        contentCell.setBorderWidth(1f);
        contentCell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);

        Paragraph pEvent = new Paragraph(evenement.getTitre().toUpperCase(), new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, primaryColor));
        pEvent.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        contentCell.addElement(pEvent);
        contentCell.addElement(new Paragraph("\n"));

        com.itextpdf.text.pdf.PdfPTable infoGrid = new com.itextpdf.text.pdf.PdfPTable(2);
        infoGrid.setWidthPercentage(100);
        infoGrid.setSpacingBefore(10);

        java.util.function.BiConsumer<String, String> addInfo = (label, value) -> {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell();
            cell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cell.setPadding(10);
            cell.addElement(new Paragraph(label, labelFont));
            cell.addElement(new Paragraph(value != null ? value : "---", valueFont));
            infoGrid.addCell(cell);
        };

        addInfo.accept("PARTICIPANT", participation.getNom());
        addInfo.accept("ID TICKET", ticketIdLabel.getText().replace("Ticket ID: #", ""));
        addInfo.accept("DATE", evenement.getDateDebut().toString());
        addInfo.accept("LIEU", evenement.getLieuNom());
        addInfo.accept("NUMÉRO DE PLACE", "ZONE A - RANGÉE 5 - SIÈGE 12");
        addInfo.accept("STATUT", "CONFIRMÉ");

        contentCell.addElement(infoGrid);
        
        Paragraph pFooter = new Paragraph("\n\nCe ticket est strictement personnel. Veuillez le présenter à l'entrée.", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, com.itextpdf.text.BaseColor.GRAY));
        pFooter.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        contentCell.addElement(pFooter);

        mainTable.addCell(contentCell);
        document.add(mainTable);
        document.close();
    }

    @FXML
    private void handleClose() {
        if (server != null) server.stop(0);
        ((Stage) eventTitleLabel.getScene().getWindow()).close();
    }
}
