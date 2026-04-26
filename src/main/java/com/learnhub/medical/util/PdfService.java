package com.learnhub.medical.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;

import java.awt.Color;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfService {

    private static final Color COLOR_PRIMARY = new Color(79, 70, 229); // Indigo
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129); // Emerald
    private static final Color COLOR_LIGHT = new Color(248, 250, 252);
    private static final Color COLOR_GRAY = new Color(100, 116, 139);

    private static final Font FONT_HEADER = new Font(Font.HELVETICA, 24, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 18, Font.BOLD, COLOR_SUCCESS);
    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_GRAY);

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
            document.add(new LineSeparator(1f, 100, new Color(226, 232, 240), Element.ALIGN_CENTER, -2));
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
            Paragraph pTotal = new Paragraph("\nTOTAL PAYÉ   20.00 DT", new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_SUCCESS));
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTotal);
            document.close();
        }
    }

    // --- PLANNING MÉDECIN (CORRECTIF) ---
    public static void generatePlanning(List<Creneau> creneaux, String dest) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            
            document.add(new Paragraph("Planning de Consultations - LearnHub Medical", FONT_HEADER));
            document.add(new Paragraph("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_SMALL));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            String[] headers = {"ID", "Jour", "Heure", "Professionnel", "Statut"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FONT_BOLD));
                cell.setBackgroundColor(COLOR_LIGHT);
                cell.setPadding(8);
                table.addCell(cell);
            }

            for (Creneau c : creneaux) {
                table.addCell(new Phrase(String.valueOf(c.getId()), FONT_NORMAL));
                table.addCell(new Phrase(c.getJour() != null ? c.getJour() : "--", FONT_NORMAL));
                table.addCell(new Phrase(c.getHeure() != null ? c.getHeure().toString() : "--:--", FONT_NORMAL));
                table.addCell(new Phrase(c.getMedecinName() != null ? c.getMedecinName() : "Dr. Khalil", FONT_NORMAL));
                table.addCell(new Phrase(c.isDisponibilite() ? "DISPONIBLE" : "RÉSERVÉ", FONT_NORMAL));
            }
            document.add(table);
            document.close();
        }
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
        cell.setBorderColor(new Color(226, 232, 240));
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
            Phrase watermark = new Phrase("PAYÉ - VALIDÉ", new Font(Font.HELVETICA, 60, Font.BOLD, new Color(241, 245, 249)));
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, watermark, 297, 421, 45);
        }
    }
}
