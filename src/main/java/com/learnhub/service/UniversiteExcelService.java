package com.learnhub.service;

import com.learnhub.models.Universite;
import com.learnhub.models.Filiere;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class UniversiteExcelService {

    public static String exportSingleUniversite(Universite uni, List<Filiere> filieres, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(uni.getNom().length() > 30 ? uni.getNom().substring(0, 30) : uni.getNom());

            // --- Styles ---
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle labelStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            labelStyle.setFont(boldFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headFont = workbook.createFont();
            headFont.setColor(IndexedColors.WHITE.getIndex());
            headFont.setBold(true);
            headerStyle.setFont(headFont);

            // --- University Information (Header Section) ---
            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("FICHE ÉTABLISSEMENT : " + uni.getNom());
            titleRow.getCell(0).setCellStyle(titleStyle);
            rowIdx++; // Spacer

            String[][] info = {
                {"Type :", uni.getType()},
                {"Ville :", uni.getVille()},
                {"Adresse :", uni.getAdresse()},
                {"Téléphone :", uni.getTelephone()},
                {"Email :", uni.getEmail()}
            };

            for (String[] pair : info) {
                Row row = sheet.createRow(rowIdx++);
                Cell c1 = row.createCell(0);
                c1.setCellValue(pair[0]);
                c1.setCellStyle(labelStyle);
                row.createCell(1).setCellValue(pair[1]);
            }
            rowIdx += 2; // Spacer

            // --- Programs Section (Table) ---
            Row subTitleRow = sheet.createRow(rowIdx++);
            subTitleRow.createCell(0).setCellValue("LISTE DES PROGRAMMES / FILIÈRES");
            subTitleRow.getCell(0).setCellStyle(titleStyle);
            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            String[] columns = {"Code", "Nom", "Niveau", "Durée (Années)", "Capacité Max"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Filières
            if (filieres != null) {
                for (Filiere f : filieres) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(f.getCode());
                    row.createCell(1).setCellValue(f.getNom());
                    row.createCell(2).setCellValue(f.getNiveau());
                    row.createCell(3).setCellValue(f.getDureeAnnees());
                    row.createCell(4).setCellValue(f.getCapaciteMax());
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            return filePath;
        }
    }
}
