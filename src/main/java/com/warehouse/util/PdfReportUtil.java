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
            double totalRevenue = 0;
            double totalCost = 0;
            double totalProfit = 0;
            int totalQuantity = 0;
            int transactionCount = 0;
            int salesCount = 0;
            
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    if ("Vente".equals(t.getType())) {
                        double revenue = t.getPrix() * t.getQuantity();
                        totalRevenue += revenue;
                        
                        // Calculate cost and profit
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        if (mattress != null) {
                            double cost = mattress.getUnitPrice() * t.getQuantity();
                            totalCost += cost;
                            totalProfit += (revenue - cost);
                        }
                        salesCount++;
                    }
                    totalQuantity += t.getQuantity();
                    transactionCount++;
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé du jour:", headerFont));
            document.add(new Paragraph("Nombre de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount, cellFont));
            document.add(new Paragraph("Quantité totale: " + totalQuantity, cellFont));
            document.add(new Paragraph("Chiffre d'affaires: " + String.format("%.2f", totalRevenue) + " DT", cellFont));
            document.add(new Paragraph("Coût total d'achat: " + String.format("%.2f", totalCost) + " DT", cellFont));
            document.add(new Paragraph("Bénéfice net: " + String.format("%.2f", totalProfit) + " DT", cellFont));
            if (totalRevenue > 0) {
                double profitMargin = (totalProfit / totalRevenue) * 100;
                document.add(new Paragraph("Marge bénéficiaire: " + String.format("%.2f", profitMargin) + "%", cellFont));
            }
            document.add(Chunk.NEWLINE);
            
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Heure", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Matelas", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix d'achat (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix de vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Date Retour", headerFont)));
            
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    double unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                    double salePrice = t.getPrix();
                    double totalPrice = salePrice * t.getQuantity();
                    
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalTime().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattress != null ? mattress.getType() + " (" + mattress.getSize() + ")" : "Inconnu", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", salePrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
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
            
            // Calculate monthly totals
            double monthlyRevenue = 0;
            double monthlyCost = 0;
            double monthlyProfit = 0;
            int salesCount = 0;
            int transactionCount = 0;
            
            for (Transaction t : transactions) {
                if (t.getDate().getYear() == date.getYear() && t.getDate().getMonthValue() == date.getMonthValue()) {
                    transactionCount++;
                    if ("Vente".equals(t.getType())) {
                        double revenue = t.getPrix() * t.getQuantity();
                        monthlyRevenue += revenue;
                        
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        if (mattress != null) {
                            double cost = mattress.getUnitPrice() * t.getQuantity();
                            monthlyCost += cost;
                            monthlyProfit += (revenue - cost);
                        }
                        salesCount++;
                    }
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé du mois:", headerFont));
            document.add(new Paragraph("Nombre de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount, cellFont));
            document.add(new Paragraph("Chiffre d'affaires: " + String.format("%.2f", monthlyRevenue) + " DT", cellFont));
            document.add(new Paragraph("Coût total d'achat: " + String.format("%.2f", monthlyCost) + " DT", cellFont));
            document.add(new Paragraph("Bénéfice net: " + String.format("%.2f", monthlyProfit) + " DT", cellFont));
            if (monthlyRevenue > 0) {
                double profitMargin = (monthlyProfit / monthlyRevenue) * 100;
                document.add(new Paragraph("Marge bénéficiaire: " + String.format("%.2f", profitMargin) + "%", cellFont));
            }
            document.add(Chunk.NEWLINE);
            
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Matelas", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            
            for (Transaction t : transactions) {
                if (t.getDate().getYear() == date.getYear() && t.getDate().getMonthValue() == date.getMonthValue()) {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    double unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                    double salePrice = t.getPrix();
                    double totalPrice = salePrice * t.getQuantity();
                    
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattress != null ? mattress.getType() + " (" + mattress.getSize() + ")" : "ID: " + t.getMattressId(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", salePrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
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
            
            // Calculate stock summary
            int totalQuantity = mattresses.stream().mapToInt(Mattress::getQuantity).sum();
            double totalValue = mattresses.stream().mapToDouble(Mattress::getTotalPrice).sum();
            int lowStockCount = (int) mattresses.stream().filter(m -> m.getQuantity() <= 5).count();
            
            document.add(new Paragraph("Résumé du stock:", headerFont));
            document.add(new Paragraph("Nombre total de matelas: " + mattresses.size(), cellFont));
            document.add(new Paragraph("Quantité totale en stock: " + totalQuantity, cellFont));
            document.add(new Paragraph("Valeur totale du stock: " + String.format("%.2f", totalValue) + " DT", cellFont));
            document.add(new Paragraph("Articles à stock critique (≤5): " + lowStockCount, cellFont));
            document.add(Chunk.NEWLINE);
            
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Taille", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Référence", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Valeur totale (DT)", headerFont)));
            
            for (Mattress m : mattresses) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(m.getId()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getSize(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getReference() != null ? m.getReference() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(m.getQuantity()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", m.getUnitPrice()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", m.getTotalPrice()), cellFont)));
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
            
            // Calculate overall statistics
            long salesCount = transactions.stream().filter(t -> "Vente".equals(t.getType())).count();
            double totalRevenue = transactions.stream()
                .filter(t -> "Vente".equals(t.getType()))
                .mapToDouble(t -> t.getPrix() * t.getQuantity())
                .sum();
            double totalCost = 0;
            double totalProfit = 0;
            
            for (Transaction t : transactions) {
                if ("Vente".equals(t.getType())) {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    if (mattress != null) {
                        double cost = mattress.getUnitPrice() * t.getQuantity();
                        totalCost += cost;
                        totalProfit += (t.getPrix() * t.getQuantity() - cost);
                    }
                }
            }
            
            document.add(new Paragraph("Statistiques globales:", headerFont));
            document.add(new Paragraph("Nombre total de transactions: " + transactions.size(), cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount, cellFont));
            document.add(new Paragraph("Chiffre d'affaires total: " + String.format("%.2f", totalRevenue) + " DT", cellFont));
            document.add(new Paragraph("Coût total d'achat: " + String.format("%.2f", totalCost) + " DT", cellFont));
            document.add(new Paragraph("Bénéfice net total: " + String.format("%.2f", totalProfit) + " DT", cellFont));
            if (totalRevenue > 0) {
                double profitMargin = (totalProfit / totalRevenue) * 100;
                document.add(new Paragraph("Marge bénéficiaire moyenne: " + String.format("%.2f", profitMargin) + "%", cellFont));
            }
            document.add(Chunk.NEWLINE);
            
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Matelas", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            
            for (Transaction t : transactions) {
                Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                double unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                double salePrice = t.getPrix();
                double totalPrice = salePrice * t.getQuantity();
                
                table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(mattress != null ? mattress.getType() + " (" + mattress.getSize() + ")" : "ID: " + t.getMattressId(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", salePrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
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