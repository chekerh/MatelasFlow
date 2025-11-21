package com.warehouse.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.warehouse.model.Transaction;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.TransactionDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportUtil {
    
    /**
     * Get the reports folder path on Desktop
     * Creates the folder if it doesn't exist
     */
    private static String getReportsFolderPath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        String reportsFolderPath = desktopPath + File.separator + "Rapports_MatelasPro";
        
        // Create directory if it doesn't exist
        File reportsFolder = new File(reportsFolderPath);
        if (!reportsFolder.exists()) {
            reportsFolder.mkdirs();
        }
        
        return reportsFolderPath;
    }
    
    /**
     * Get full path for a report file
     */
    private static String getReportFilePath(String filename) {
        return getReportsFolderPath() + File.separator + filename;
    }
    
    public static boolean generateDailyTransactionsReport(LocalDate date, String filename) {
        try {
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            
            document.add(new Paragraph("Rapport des transactions du jour", titleFont));
            document.add(new Paragraph("Date: " + date.format(DateTimeFormatter.ISO_DATE)));
            document.add(Chunk.NEWLINE);
            
            // Calculate totals
            double totalSales = 0;
            int totalQuantity = 0;
            int transactionCount = 0;
            
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    if ("Vente".equals(t.getType())) {
                        totalSales += t.getPrix() * t.getQuantity();
                    }
                    totalQuantity += t.getQuantity();
                    transactionCount++;
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé du jour:", headerFont));
            document.add(new Paragraph("Nombre de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Quantité totale: " + totalQuantity, cellFont));
            document.add(new Paragraph("Chiffre d'affaires: " + String.format("%.2f", totalSales) + "€", cellFont));
            document.add(Chunk.NEWLINE);
            
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Heure", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Matelas", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix Original (€)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix Vente (€)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Date Retour", headerFont)));
            
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalTime().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattress != null ? mattress.getType() : "Inconnu", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattress != null ? String.format("%.2f", mattress.getPrix()) : "0.00", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", t.getPrix()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getStoreOwnerId() != null ? "ID: " + t.getStoreOwnerId() : "", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getExpectedReturnDate() != null ? t.getExpectedReturnDate().toString() : "", cellFont)));
                }
            }
            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean generateMonthlyTransactionsReport(LocalDate date, String filename) {
        try {
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            document.add(new Paragraph("Rapport des transactions du mois", titleFont));
            document.add(new Paragraph("Mois: " + date.getYear() + "-" + String.format("%02d", date.getMonthValue())));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("ID Matelas", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("ID Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            for (Transaction t : transactions) {
                if (t.getDate().getYear() == date.getYear() && t.getDate().getMonthValue() == date.getMonthValue()) {
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getId()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getMattressId()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getStoreOwnerId() != null ? String.valueOf(t.getStoreOwnerId()) : "", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
                }
            }
            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean generateStockReport(String filename) {
        try {
            List<Mattress> mattresses = MattressDAO.getAllMattresses();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            document.add(new Paragraph("Rapport de Stock - MatelasPro", titleFont));
            document.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Taille", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Référence", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            for (Mattress m : mattresses) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(m.getId()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getSize(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getReference() != null ? m.getReference() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(m.getQuantity()), cellFont)));
            }
            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean generateTransactionReport(String filename) {
        try {
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            document.add(new Paragraph("Rapport de Transactions - MatelasPro", titleFont));
            document.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("ID Matelas", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix", headerFont)));
            table.addCell(new PdfPCell(new Phrase("ID Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            for (Transaction t : transactions) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getId()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getMattressId()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getPrix()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getStoreOwnerId() != null ? String.valueOf(t.getStoreOwnerId()) : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
            }
            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 