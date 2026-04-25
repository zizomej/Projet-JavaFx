package com.learnhub.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.learnhub.models.Filiere;
import java.io.FileOutputStream;
import java.net.URL;

public class FilierePDFService {

    private static final BaseColor COLOR_PRIMARY = new BaseColor(30, 41, 59); // Slate 800
    private static final BaseColor COLOR_ACCENT = new BaseColor(245, 158, 11); // Amber 500
    private static final BaseColor COLOR_TEXT = new BaseColor(51, 65, 85);  // Slate 700

    public static String generateFilierePDF(Filiere f) {
        String fileName = "Filiere_" + f.getCode() + ".pdf";
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Font styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, COLOR_PRIMARY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12, COLOR_TEXT);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);

            // 1. Logo
            try {
                URL logoUrl = FilierePDFService.class.getResource("/images/logo.png");
                if (logoUrl != null) {
                    Image logo = Image.getInstance(logoUrl);
                    logo.scaleToFit(150, 75);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                }
            } catch (Exception e) {
                System.err.println("Could not load logo for PDF: " + e.getMessage());
            }

            document.add(new Paragraph("\n"));

            // 2. Main Title Header (Styled Table)
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("BROCHURE OFFICIELLE", headerFont));
            titleCell.setBackgroundColor(COLOR_PRIMARY);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setPadding(10);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleTable.addCell(titleCell);
            document.add(titleTable);

            document.add(new Paragraph("\n"));

            // 3. Filiere Name
            Paragraph title = new Paragraph(f.getNom().toUpperCase(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 4. Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(20);
            table.setWidths(new float[]{1, 2});

            addTableCell(table, "Code Programme", f.getCode(), labelFont, textFont);
            addTableCell(table, "Niveau d'Études", f.getNiveau(), labelFont, textFont);
            addTableCell(table, "Durée de Formation", f.getDureeAnnees() + " ans", labelFont, textFont);
            addTableCell(table, "Capacité d'Accueil", f.getCapaciteMax() + " étudiants", labelFont, textFont);

            document.add(table);

            // 5. Description Section
            Paragraph descTitle = new Paragraph("PRÉSENTATION DU PROGRAMME", labelFont);
            descTitle.setSpacingAfter(10);
            document.add(descTitle);

            String desc = f.getDescription() != null && !f.getDescription().isEmpty() 
                          ? f.getDescription() 
                          : "Ce programme d'excellence offre une formation complète et pointue, alliant théorie et pratique pour préparer les étudiants aux défis du monde professionnel moderne.";
            
            Paragraph description = new Paragraph(desc, textFont);
            description.setAlignment(Element.ALIGN_JUSTIFIED);
            description.setLeading(18f); // Line spacing
            document.add(description);

            // 6. Footer
            document.add(new Paragraph("\n\n\n"));
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(COLOR_ACCENT);
            document.add(new Chunk(ls));
            
            Paragraph footer = new Paragraph("© 2026 LearnHub - Plateforme d'apprentissage intelligente\nDocument généré automatiquement le " + java.time.LocalDate.now(), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addTableCell(PdfPTable table, String label, String value, Font labelFont, Font textFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setPadding(8);
        cell1.setBackgroundColor(new BaseColor(241, 245, 249)); // Slate 100
        cell1.setBorderColor(BaseColor.LIGHT_GRAY);
        
        PdfPCell cell2 = new PdfPCell(new Phrase(value, textFont));
        cell2.setPadding(8);
        cell2.setBorderColor(BaseColor.LIGHT_GRAY);
        
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
