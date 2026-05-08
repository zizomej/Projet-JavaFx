package com.learnhub.medical.util;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfService {

    private static final BaseColor COLOR_PRIMARY = new BaseColor(79, 70, 229); // Indigo
    private static final BaseColor COLOR_SUCCESS = new BaseColor(16, 185, 129); // Emerald
    private static final BaseColor COLOR_LIGHT = new BaseColor(248, 250, 252);
    private static final BaseColor COLOR_GRAY = new BaseColor(100, 116, 139);

    private static final Font FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, COLOR_SUCCESS);
    private static final Font FONT_BOLD = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, COLOR_GRAY);

    // --- REÇU ÉTUDIANT (DÉJÀ OK) ---
    public static void generateReceipt(RDV rdv, String dest) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            writer.setPageEvent(new WatermarkEvent());
            document.open();
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2, 1});
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("LearnHub Medical", FONT_HEADER));
            leftCell.addElement(new Paragraph("Service Médical Universitaire\nTunis, Tunisie", FONT_SMALL));
            headerTable.addCell(leftCell);
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            Paragraph pRecu = new Paragraph("REÇU MÉDICAL", FONT_TITLE);
            pRecu.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(pRecu);
            headerTable.addCell(rightCell);
            document.add(headerTable);
            document.add(new Paragraph("\n"));
            document.add(new LineSeparator(1f, 100, new BaseColor(226, 232, 240), Element.ALIGN_CENTER, -2));
            document.add(new Paragraph("\n"));
            PdfPTable rdvTable = new PdfPTable(2);
            rdvTable.setWidthPercentage(100);
            PdfPCell c1 = new PdfPCell(); c1.setBorder(Rectangle.NO_BORDER);
            c1.addElement(new Paragraph("PATIENT", FONT_SMALL));
            String pName = (rdv.getStudentName() != null && !rdv.getStudentName().contains("#")) ? rdv.getStudentName() : "Tasnim Araar";
            c1.addElement(new Paragraph(pName, FONT_BOLD));
            rdvTable.addCell(c1);
            PdfPCell c2 = new PdfPCell(); c2.setBorder(Rectangle.NO_BORDER);
            c2.addElement(new Paragraph("PROFESSIONNEL", FONT_SMALL));
            c2.addElement(new Paragraph("Dr. Khalil (SANTÉ)", FONT_BOLD));
            rdvTable.addCell(c2);
            document.add(rdvTable);
            document.add(new Paragraph("\n\n"));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 1});
            table.addCell(createStyledHeader("DESCRIPTION"));
            table.addCell(createStyledHeader("MONTANT"));
            table.addCell(createStyledCell("Consultation Médicale Universitaire\nMotif : " + rdv.getMotif(), false));
            table.addCell(createStyledCell("20.00 DT", true));
            document.add(table);
            Paragraph pTotal = new Paragraph("\nTOTAL PAYÉ   20.00 DT", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, COLOR_SUCCESS));
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTotal);
            document.close();
        }
    }

    // --- PLANNING MÉDECIN (DESIGN PREMIUM) ---
    public static void generatePlanning(List<Creneau> creneaux, String dest) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            
            // 1. HEADER BANNER
            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            PdfPCell bannerCell = new PdfPCell(new Phrase("PLANNING DES CONSULTATIONS - LEARNHUB MEDICAL", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE)));
            bannerCell.setBackgroundColor(new BaseColor(30, 58, 138)); // Dark Blue #1e3a8a
            bannerCell.setPadding(15);
            bannerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            bannerCell.setBorder(Rectangle.NO_BORDER);
            banner.addCell(bannerCell);
            document.add(banner);
            
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_SMALL));
            document.add(new Paragraph("\n"));

            // 2. STATS SECTION
            long total = creneaux.size();
            long dispo = creneaux.stream().filter(Creneau::isDisponibilite).count();
            long res = total - dispo;

            PdfPTable statsTable = new PdfPTable(3);
            statsTable.setWidthPercentage(60);
            statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            statsTable.addCell(createStatCell("TOTAL CRÉNEAUX", String.valueOf(total), new BaseColor(59, 130, 246)));
            statsTable.addCell(createStatCell("DISPONIBLES", String.valueOf(dispo), new BaseColor(16, 185, 129)));
            statsTable.addCell(createStatCell("RÉSERVÉS", String.valueOf(res), new BaseColor(239, 68, 68)));
            document.add(statsTable);
            document.add(new Paragraph("\n"));

            // 3. MAIN TABLE
            PdfPTable table = new PdfPTable(new float[]{1, 2, 2, 3, 2});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Headers
            String[] headers = {"ID", "DATE / JOUR", "HORAIRE", "PROFESSIONNEL", "STATUT"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
                cell.setBackgroundColor(new BaseColor(51, 65, 85)); // Slate 700
                cell.setPadding(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            // Data rows
            boolean alternate = false;
            for (Creneau c : creneaux) {
                BaseColor rowColor = alternate ? new BaseColor(248, 250, 252) : BaseColor.WHITE;
                
                table.addCell(createDataCell(String.valueOf(c.getId()), rowColor, Element.ALIGN_CENTER));
                table.addCell(createDataCell(c.getJour(), rowColor, Element.ALIGN_CENTER));
                table.addCell(createDataCell(c.getHeure().toString(), rowColor, Element.ALIGN_CENTER));
                table.addCell(createDataCell(c.getMedecinName() != null ? c.getMedecinName() : "Dr. Khalil", rowColor, Element.ALIGN_LEFT));
                
                // Status with color
                PdfPCell statusCell = new PdfPCell();
                statusCell.setBackgroundColor(rowColor);
                statusCell.setBorder(Rectangle.NO_BORDER);
                statusCell.setPadding(8);
                Font statusFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, c.isDisponibilite() ? COLOR_SUCCESS : new BaseColor(220, 38, 38));
                Paragraph p = new Paragraph(c.isDisponibilite() ? "DISPONIBLE" : "RÉSERVÉ", statusFont);
                p.setAlignment(Element.ALIGN_CENTER);
                statusCell.addElement(p);
                table.addCell(statusCell);
                
                alternate = !alternate;
            }
            
            document.add(table);
            document.close();
        }
    }

    // --- RAPPORT RDV (DESIGN PREMIUM) ---
    public static void generateRDVReport(List<RDV> rdvs, String dest) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            
            // 1. HEADER BANNER
            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            PdfPCell bannerCell = new PdfPCell(new Phrase("LISTE DES RENDEZ-VOUS MÉDICAUX - LEARNHUB", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE)));
            bannerCell.setBackgroundColor(new BaseColor(30, 58, 138)); // Dark Blue #1e3a8a
            bannerCell.setPadding(15);
            bannerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            bannerCell.setBorder(Rectangle.NO_BORDER);
            banner.addCell(bannerCell);
            document.add(banner);
            
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_SMALL));
            document.add(new Paragraph("\n"));

            // 2. STATS SECTION
            long total = rdvs.size();
            long confirmed = rdvs.stream().filter(r -> r.getStatut().toLowerCase().contains("confirm") || r.getStatut().toLowerCase().contains("pay")).count();
            long pending = rdvs.stream().filter(r -> r.getStatut().toLowerCase().contains("attente")).count();

            PdfPTable statsTable = new PdfPTable(3);
            statsTable.setWidthPercentage(60);
            statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            statsTable.addCell(createStatCell("TOTAL RDV", String.valueOf(total), new BaseColor(59, 130, 246)));
            statsTable.addCell(createStatCell("CONFIRMÉS / PAYÉS", String.valueOf(confirmed), new BaseColor(16, 185, 129)));
            statsTable.addCell(createStatCell("EN ATTENTE", String.valueOf(pending), new BaseColor(245, 158, 11)));
            document.add(statsTable);
            document.add(new Paragraph("\n"));

            // 3. MAIN TABLE
            PdfPTable table = new PdfPTable(new float[]{1, 2, 2, 3, 2});
            table.setWidthPercentage(100);

            // Headers
            String[] headers = {"ID", "DATE", "ÉTUDIANT", "MOTIF DE CONSULTATION", "STATUT"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
                cell.setBackgroundColor(new BaseColor(51, 65, 85));
                cell.setPadding(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            // Data rows
            boolean alternate = false;
            for (RDV r : rdvs) {
                BaseColor rowColor = alternate ? new BaseColor(248, 250, 252) : BaseColor.WHITE;
                
                table.addCell(createDataCell(String.valueOf(r.getId()), rowColor, Element.ALIGN_CENTER));
                table.addCell(createDataCell(r.getDateDemande().toString(), rowColor, Element.ALIGN_CENTER));
                table.addCell(createDataCell(r.getStudentName(), rowColor, Element.ALIGN_LEFT));
                table.addCell(createDataCell(r.getMotif(), rowColor, Element.ALIGN_LEFT));
                
                // Status with color
                PdfPCell statusCell = new PdfPCell();
                statusCell.setBackgroundColor(rowColor);
                statusCell.setBorder(Rectangle.NO_BORDER);
                statusCell.setPadding(8);
                String st = r.getStatut().toLowerCase();
                BaseColor stCol = st.contains("confirm") || st.contains("pay") ? COLOR_SUCCESS : st.contains("annul") ? new BaseColor(220, 38, 38) : new BaseColor(245, 158, 11);
                Font statusFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, stCol);
                Paragraph p = new Paragraph(r.getStatut().toUpperCase(), statusFont);
                p.setAlignment(Element.ALIGN_CENTER);
                statusCell.addElement(p);
                table.addCell(statusCell);
                
                alternate = !alternate;
            }
            
            document.add(table);
            document.close();
        }
    }

    private static PdfPCell createStatCell(String title, String value, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10);
        Paragraph pTitle = new Paragraph(title, new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, COLOR_GRAY));
        Paragraph pValue = new Paragraph(value, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, color));
        cell.addElement(pTitle);
        cell.addElement(pValue);
        return cell;
    }

    private static PdfPCell createDataCell(String text, BaseColor bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "--", FONT_NORMAL));
        cell.setBackgroundColor(bgColor);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new BaseColor(226, 232, 240));
        cell.setPadding(8);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    // --- ORDONNANCE MÉDECIN (CORRECTIF) ---
    public static void generatePrescription(RDV rdv, String notes, String dest) throws Exception {
        Document document = new Document(PageSize.A5, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            
            Paragraph dr = new Paragraph("DR. KHALIL\nService de Santé Universitaire\nTUNIS", FONT_BOLD);
            document.add(dr);
            document.add(new LineSeparator());
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("Date : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT_NORMAL));
            String pName = (rdv.getStudentName() != null) ? rdv.getStudentName() : "Patient";
            document.add(new Paragraph("Patient : " + pName, FONT_BOLD));
            document.add(new Paragraph("\n\nORDONNANCE :\n", FONT_TITLE));
            
            Paragraph content = new Paragraph(notes != null ? notes : "Aucune note.", FONT_NORMAL);
            content.setLeading(20f);
            document.add(content);
            
            document.add(new Paragraph("\n\n\nSignature :", FONT_BOLD));
            document.close();
        }
    }

    private static PdfPCell createStyledHeader(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_SMALL));
        cell.setBackgroundColor(COLOR_LIGHT);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new BaseColor(226, 232, 240));
        cell.setPadding(10);
        return cell;
    }

    private static PdfPCell createStyledCell(String text, boolean alignRight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, alignRight ? FONT_BOLD : FONT_NORMAL));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(15);
        if (alignRight) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    static class WatermarkEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Phrase watermark = new Phrase("PAYÉ - VALIDÉ", new Font(Font.FontFamily.HELVETICA, 60, Font.BOLD, new BaseColor(241, 245, 249)));
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, watermark, 297, 421, 45);
        }
    }
}
