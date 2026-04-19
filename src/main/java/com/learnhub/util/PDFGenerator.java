package com.learnhub.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.learnhub.models.BulletinRow;
import com.learnhub.models.Utilisateur;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFGenerator {

    private static final BaseColor BLUE_HEADER = new BaseColor(79, 70, 229); // Indigo 600
    private static final BaseColor ROW_EVEN = new BaseColor(249, 250, 251);
    private static final BaseColor ROW_ODD = BaseColor.WHITE;
    private static final BaseColor TEXT_MAIN = new BaseColor(31, 41, 55);
    private static final BaseColor TEXT_GRAY = new BaseColor(107, 114, 128);
    private static final BaseColor BORDER_COLOR = new BaseColor(229, 231, 235);

    public boolean genererBulletinPdf(File file, Utilisateur enfant, List<BulletinRow> bulletin,
                                      double moyenneGenerale, int creditsValides, double meilleureNote, int rang) {
        Document document = new Document(PageSize.A4, 30, 30, 40, 40);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Fonts
            Font fontHeaderTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE);
            Font fontHeaderSub = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.WHITE);
            Font fontCardValue = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, TEXT_MAIN);
            Font fontCardLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_GRAY);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_GRAY);
            Font fontTableCell = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MAIN);
            Font fontTableCellBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_MAIN);

            // --- 1. HEADER (BACKGROUND COLOR BLOCK USING PDFCONTENTBYTE) ---
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.setColorFill(BLUE_HEADER);
            cb.rectangle(0, document.getPageSize().getHeight() - 100, document.getPageSize().getWidth(), 100);
            cb.fill();

            // Text inside header
            Paragraph title = new Paragraph("📈 Mon tableau des notes", fontHeaderTitle);
            title.setSpacingBefore(0);
            document.add(title);
            
            String prenomNom = (enfant.getPrenom() != null ? enfant.getPrenom() : "") + " " + 
                               (enfant.getNom() != null ? enfant.getNom() : "");
            Paragraph subTitle = new Paragraph("Semestre 1 – Année académique 2025/2026  |  Étudiant: " + prenomNom, fontHeaderSub);
            document.add(subTitle);
            
            document.add(new Paragraph("\n\n"));

            // --- 2. STATS CARDS ---
            PdfPTable statsTable = new PdfPTable(4);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingBefore(10);
            statsTable.setSpacingAfter(20);
            statsTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            statsTable.addCell(createStatCard("MOYENNE GÉNÉRALE", String.format("%.2f", moyenneGenerale) + "/20", fontCardLabel, fontCardValue));
            statsTable.addCell(createStatCard("MEILLEURE NOTE", meilleureNote > 0 ? String.format("%.2f", meilleureNote) : "—", fontCardLabel, fontCardValue));
            statsTable.addCell(createStatCard("CRÉDITS VALIDÉS", creditsValides + "/30", fontCardLabel, fontCardValue));
            statsTable.addCell(createStatCard("RANG", rang + "ème sur 42", fontCardLabel, fontCardValue));

            document.add(statsTable);

            // --- 3. BULLETIN TABLE ---
            PdfPTable table = new PdfPTable(new float[]{3.5f, 1.5f, 1f, 1f, 1f, 1.2f, 1.5f, 2f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // Headers
            String[] headers = {"MODULE", "CODE", "CRÉDITS", "NOTE CC", "NOTE TP", "NOTE EXAMEN", "NOTE FINALE", "MENTION"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontTableHeader));
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(BORDER_COLOR);
                cell.setBorderWidth(1.5f);
                cell.setPaddingBottom(8);
                cell.setPaddingTop(5);
                table.addCell(cell);
            }

            // Rows
            boolean isEven = false;
            for (BulletinRow row : bulletin) {
                BaseColor bgColor = isEven ? ROW_EVEN : ROW_ODD;
                isEven = !isEven;

                addCell(table, row.getModuleIntitule(), fontTableCellBold, bgColor);
                addCell(table, row.getModuleCode(), fontTableCell, bgColor);
                addCell(table, String.valueOf(row.getCredits()), fontTableCell, bgColor);
                addCell(table, row.getNoteCCStr(), fontTableCell, bgColor);
                addCell(table, row.getNoteTPStr(), fontTableCell, bgColor);
                addCell(table, row.getNoteExamenStr(), fontTableCell, bgColor);
                addCell(table, row.getMoyenneStr(), fontTableCellBold, bgColor);
                
                // MENTION Badge Cell
                PdfPCell mentionCell = new PdfPCell();
                mentionCell.setBackgroundColor(bgColor);
                mentionCell.setBorder(Rectangle.BOTTOM);
                mentionCell.setBorderColor(BORDER_COLOR);
                mentionCell.setPadding(6);
                
                Phrase mentionPhrase = new Phrase(row.getMention(), getBadgeFont(row.getMention()));
                mentionCell.addElement(mentionPhrase);
                table.addCell(mentionCell);
            }
            document.add(table);

            // --- 4. BAR CHART (Using PdfContentByte) ---
            document.add(new Paragraph("\n\n"));
            Paragraph chartTitle = new Paragraph("📊 Distribution des notes", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TEXT_MAIN));
            chartTitle.setSpacingAfter(40);
            document.add(chartTitle);

            if (!bulletin.isEmpty()) {
                drawSimpleBarChart(writer.getDirectContent(), writer.getVerticalPosition(false), bulletin);
            }
            
            document.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n\n")); // Spacer for the chart

            // --- 5. APPRECIATIONS ---
            Paragraph apprTitle = new Paragraph("💬 Appréciations par module", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TEXT_MAIN));
            apprTitle.setSpacingAfter(10);
            document.add(apprTitle);

            for (BulletinRow row : bulletin) {
                if (row.getMoyenneFinal() < 0) continue;
                
                PdfPTable apprTable = new PdfPTable(1);
                apprTable.setWidthPercentage(100);
                apprTable.setSpacingAfter(10);
                
                PdfPCell acell = new PdfPCell();
                acell.setBorder(Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM | Rectangle.RIGHT);
                acell.setBorderColor(BORDER_COLOR);
                acell.setBorderColorLeft(BLUE_HEADER);
                acell.setBorderWidthLeft(3f);
                acell.setPadding(10);
                acell.setBackgroundColor(ROW_EVEN);
                
                Paragraph p1 = new Paragraph(row.getModuleIntitule() + " - Note: " + row.getMoyenneStr(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_MAIN));
                Paragraph p2 = new Paragraph("❝ " + row.getAppreciation() + " ❞", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, TEXT_GRAY));
                
                acell.addElement(p1);
                acell.addElement(p2);
                apprTable.addCell(acell);
                
                document.add(apprTable);
            }

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private PdfPCell createStatCard(String labelStr, String valueStr, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(1f);
        cell.setPadding(15);
        cell.setBackgroundColor(BaseColor.WHITE);

        Paragraph label = new Paragraph(labelStr, labelFont);
        Paragraph value = new Paragraph(valueStr, valueFont);
        
        cell.addElement(label);
        cell.addElement(value);
        
        // Add slightly wider spacing between cards by wrapping it
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(95);
        wrapper.addCell(cell);
        
        PdfPCell finalCell = new PdfPCell(wrapper);
        finalCell.setBorder(Rectangle.NO_BORDER);
        return finalCell;
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(1f);
        cell.setPadding(8);
        cell.setPaddingTop(5);
        table.addCell(cell);
    }

    private Font getBadgeFont(String mention) {
        BaseColor color = TEXT_GRAY;
        if (mention.contains("Très Bien") || mention.contains("Bien")) {
            color = new BaseColor(16, 185, 129); // Green
        } else if (mention.contains("Assez Bien")) {
            color = new BaseColor(245, 158, 11); // Orange
        } else if (mention.contains("Passable")) {
            color = new BaseColor(217, 119, 6); // Orange-amber
        } else if (mention.contains("Non Admis")) {
            color = new BaseColor(239, 68, 68); // Red
        }
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color);
    }

    // Custom simple procedural Bar chart using absolute positioning
    private void drawSimpleBarChart(PdfContentByte cb, float yStart, List<BulletinRow> bulletin) {
        float chartWidth = 400;
        float chartHeight = 150;
        float xStart = 100;
        
        // Axes
        cb.setColorStroke(BORDER_COLOR);
        cb.setLineWidth(1f);
        cb.moveTo(xStart, yStart);
        cb.lineTo(xStart + chartWidth, yStart); // X axis
        cb.moveTo(xStart, yStart);
        cb.lineTo(xStart, yStart + chartHeight); // Y axis
        cb.stroke();
        
        // Labels Y
        cb.beginText();
        cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA).getBaseFont(), 8);
        cb.setColorFill(TEXT_GRAY);
        for (int i = 0; i <= 20; i += 5) {
            float yPos = yStart + (chartHeight * ((float)i / 20f));
            cb.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(i), xStart - 5, yPos - 3, 0);
            
            // Grid line
            if (i > 0) {
                cb.endText();
                cb.setColorStroke(new BaseColor(243, 244, 246));
                cb.moveTo(xStart, yPos);
                cb.lineTo(xStart + chartWidth, yPos);
                cb.stroke();
                cb.beginText();
                cb.setColorFill(TEXT_GRAY);
            }
        }
        cb.endText();
        
        // Bars
        int barCount = bulletin.size();
        float barWidth = Math.min(30, (chartWidth - 50) / barCount);
        float spacing = ((chartWidth - 50) - (barCount * barWidth)) / (barCount + 1);
        
        float currentX = xStart + spacing;
        
        for (BulletinRow row : bulletin) {
            double note = row.getMoyenneFinal();
            if (note < 0) note = 0;
            
            float barTotalHeight = (float) (chartHeight * (note / 20.0));
            
            // Draw bar
            cb.setColorFill(new BaseColor(167, 139, 250)); // purple-400
            cb.rectangle(currentX, yStart, barWidth, barTotalHeight);
            cb.fill();
            
            // Label X
            cb.beginText();
            cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA).getBaseFont(), 8);
            cb.setColorFill(TEXT_MAIN);
            String code = row.getModuleCode() != null && row.getModuleCode().length() > 6 
                ? row.getModuleCode().substring(0, 6) : row.getModuleCode();
            cb.showTextAligned(Element.ALIGN_CENTER, code, currentX + (barWidth/2), yStart - 15, 0);
            cb.endText();
            
            currentX += barWidth + spacing;
        }
    }
}
