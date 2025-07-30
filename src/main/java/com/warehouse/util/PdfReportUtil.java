package com.warehouse.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.warehouse.model.Transaction;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportUtil {
    public static void generateDailyTransactionsReport(List<Transaction> transactions, LocalDate date) throws Exception {
        Document document = new Document();
        String filename = "Daily_Transactions_" + date.toString() + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
        document.add(new Paragraph("Rapport des transactions du jour", titleFont));
        document.add(new Paragraph("Date: " + date.format(DateTimeFormatter.ISO_DATE)));
        document.add(Chunk.NEWLINE);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Heure", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
        table.addCell(new PdfPCell(new Phrase("ID Matelas", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
        table.addCell(new PdfPCell(new Phrase("ID Propriétaire", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
        for (Transaction t : transactions) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getId()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalTime().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getMattressId()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(t.getStoreOwnerId() != null ? String.valueOf(t.getStoreOwnerId()) : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
        }
        document.add(table);
        document.close();
    }

    public static void generateMonthlyTransactionsReport(List<Transaction> transactions, int year, int month) throws Exception {
        Document document = new Document();
        String filename = "Monthly_Transactions_" + year + "_" + String.format("%02d", month) + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
        document.add(new Paragraph("Rapport des transactions du mois", titleFont));
        document.add(new Paragraph("Mois: " + year + "-" + String.format("%02d", month)));
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
            if (t.getDate().getYear() == year && t.getDate().getMonthValue() == month) {
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
    }
} 