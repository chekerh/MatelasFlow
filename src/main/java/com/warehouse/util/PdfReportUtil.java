package com.warehouse.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.warehouse.model.Transaction;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.PackItem;
import com.warehouse.model.PackItemDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class PdfReportUtil {
    
    /**
     * Open a PDF file using the system default PDF viewer
     */
    public static void openPdfFile(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(pdfFile);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not open PDF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Open a folder in the system default file manager
     */
    public static void openFolder(String folderPath) {
        try {
            File folder = new File(folderPath);
            if (folder.exists()) {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                
                if (os.contains("win")) {
                    // Windows: open folder in Explorer
                    pb = new ProcessBuilder("explorer.exe", folder.getAbsolutePath());
                } else if (os.contains("mac")) {
                    // macOS: open folder in Finder
                    pb = new ProcessBuilder("open", folder.getAbsolutePath());
                } else {
                    // Linux: open folder in default file manager
                    pb = new ProcessBuilder("xdg-open", folder.getAbsolutePath());
                }
                
                pb.start();
            }
        } catch (Exception e) {
            System.err.println("Could not open folder: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the daily reports folder path on Desktop
     * Creates the folder if it doesn't exist
     */
    private static String getDailyReportsFolderPath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        String folderPath = desktopPath + File.separator + "LES RAPPORT QUOTIDIEN";
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        return folderPath;
    }
    
    /**
     * Get the monthly reports folder path on Desktop
     * Creates the folder if it doesn't exist
     */
    private static String getMonthlyReportsFolderPath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        String folderPath = desktopPath + File.separator + "MONSUEL";
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        return folderPath;
    }
    
    /**
     * Get the stock reports folder path on Desktop
     * Creates the folder if it doesn't exist
     */
    private static String getStockReportsFolderPath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        String folderPath = desktopPath + File.separator + "RAPPORT DE STOCK";
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        return folderPath;
    }
    
    /**
     * Get the transaction reports folder path on Desktop
     * Creates the folder if it doesn't exist
     */
    private static String getTransactionReportsFolderPath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        String folderPath = desktopPath + File.separator + "RAPPORT DE TRANSACTION";
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        return folderPath;
    }
    
    /**
     * Generate daily transactions report and return the file path
     * Returns null if generation fails
     */
    public static String generateDailyTransactionsReport(LocalDate date, String filename) {
        try {
            // Ensure folder exists
            getDailyReportsFolderPath();
            
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            String filePath = getDailyReportsFolderPath() + File.separator + filename;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
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
            int packCount = 0;
            
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                    
                    if ("vente".equals(type)) {
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
                        totalQuantity += t.getQuantity();
                    } else if (type.startsWith("pack")) {
                        // For packs, prix contains the total pack price
                        double packRevenue = t.getPrix();
                        totalRevenue += packRevenue;
                        
                        // Calculate cost based on individual pack items
                        List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                        double packCost = 0.0;
                        for (PackItem item : packItems) {
                            Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                            if (mattress != null) {
                                packCost += mattress.getUnitPrice() * item.getQuantity();
                                totalQuantity += item.getQuantity();
                            }
                        }
                        totalCost += packCost;
                        totalProfit += (packRevenue - packCost);
                        packCount++;
                        salesCount++;
                    } else {
                        totalQuantity += t.getQuantity();
                    }
                    transactionCount++;
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé du jour:", headerFont));
            document.add(new Paragraph("Nombre de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount + (packCount > 0 ? " (dont " + packCount + " pack(s))" : ""), cellFont));
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
            table.addCell(new PdfPCell(new Phrase("Matelas / Pack", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix d'achat (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix de vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Date Retour", headerFont)));
            
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                    String mattressDisplay = "";
                    double unitPrice = 0.0;
                    double salePrice = t.getPrix();
                    double totalPrice;
                    
                    if (type.startsWith("pack")) {
                        // For packs, show pack details
                        List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                        if (packItems != null && !packItems.isEmpty()) {
                            StringBuilder packDetails = new StringBuilder("Pack: ");
                            for (int i = 0; i < packItems.size(); i++) {
                                PackItem item = packItems.get(i);
                                Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                                if (mattress != null) {
                                    if (i > 0) packDetails.append(", ");
                                    packDetails.append(item.getQuantity()).append("x ");
                                    packDetails.append(mattress.getSize()).append(" - ").append(mattress.getReference());
                                }
                            }
                            mattressDisplay = packDetails.toString();
                        } else {
                            mattressDisplay = "Pack";
                        }
                        totalPrice = salePrice; // Pack price is already total
                        unitPrice = 0.0; // Not applicable for packs
                    } else {
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                        totalPrice = salePrice * t.getQuantity();
                        mattressDisplay = mattress != null ? mattress.getSize() + " - " + mattress.getReference() : "Inconnu";
                    }
                    
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalTime().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattressDisplay, cellFont)));
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
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating daily report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the weekly reports folder path on Desktop
     * Creates the folder if it doesn't exist
     */
    private static String getWeeklyReportsFolderPath() {
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + File.separator + "Desktop";
        String folderPath = desktopPath + File.separator + "LES RAPPORT QUOTIDIEN"; // Weekly reports go in daily folder
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        return folderPath;
    }
    
    /**
     * Generate weekly transactions report and return the file path
     * Returns null if generation fails
     */
    public static String generateWeeklyTransactionsReport(LocalDate date, String filename) {
        try {
            // Ensure folder exists
            getWeeklyReportsFolderPath();
            
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            String filePath = getWeeklyReportsFolderPath() + File.separator + filename;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            
            // Calculate week start and end dates
            LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            document.add(new Paragraph("Rapport des transactions hebdomadaire", titleFont));
            document.add(new Paragraph("Semaine: " + weekStart.format(DateTimeFormatter.ISO_DATE) + " - " + weekEnd.format(DateTimeFormatter.ISO_DATE)));
            document.add(Chunk.NEWLINE);
            
            // Calculate weekly totals
            double weeklyRevenue = 0;
            double weeklyCost = 0;
            double weeklyProfit = 0;
            int salesCount = 0;
            int packCount = 0;
            int transactionCount = 0;
            
            for (Transaction t : transactions) {
                LocalDate transactionDate = t.getDate().toLocalDate();
                if (!transactionDate.isBefore(weekStart) && !transactionDate.isAfter(weekEnd)) {
                    transactionCount++;
                    String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                    
                    if (type.startsWith("vente")) {
                        double revenue = t.getPrix() * t.getQuantity();
                        weeklyRevenue += revenue;
                        
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        if (mattress != null) {
                            double cost = mattress.getUnitPrice() * t.getQuantity();
                            weeklyCost += cost;
                            weeklyProfit += (revenue - cost);
                        }
                        salesCount++;
                    } else if (type.startsWith("pack")) {
                        // For packs, prix contains the total pack price
                        double packRevenue = t.getPrix();
                        weeklyRevenue += packRevenue;
                        
                        // Calculate cost based on individual pack items
                        List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                        double packCost = 0.0;
                        for (PackItem item : packItems) {
                            Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                            if (mattress != null) {
                                packCost += mattress.getUnitPrice() * item.getQuantity();
                            }
                        }
                        weeklyCost += packCost;
                        weeklyProfit += (packRevenue - packCost);
                        packCount++;
                        salesCount++;
                    }
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé de la semaine:", headerFont));
            document.add(new Paragraph("Nombre de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount + (packCount > 0 ? " (dont " + packCount + " pack(s))" : ""), cellFont));
            document.add(new Paragraph("Chiffre d'affaires: " + String.format("%.2f", weeklyRevenue) + " DT", cellFont));
            document.add(new Paragraph("Coût total d'achat: " + String.format("%.2f", weeklyCost) + " DT", cellFont));
            document.add(new Paragraph("Bénéfice net: " + String.format("%.2f", weeklyProfit) + " DT", cellFont));
            if (weeklyRevenue > 0) {
                double profitMargin = (weeklyProfit / weeklyRevenue) * 100;
                document.add(new Paragraph("Marge bénéficiaire: " + String.format("%.2f", profitMargin) + "%", cellFont));
            }
            document.add(Chunk.NEWLINE);
            
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Date", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Matelas / Pack", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            
            for (Transaction t : transactions) {
                LocalDate transactionDate = t.getDate().toLocalDate();
                if (!transactionDate.isBefore(weekStart) && !transactionDate.isAfter(weekEnd)) {
                    String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                    String mattressDisplay = "";
                    double unitPrice = 0.0;
                    double salePrice = t.getPrix();
                    double totalPrice;
                    
                    if (type.startsWith("pack")) {
                        // For packs, show pack details
                        List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                        if (packItems != null && !packItems.isEmpty()) {
                            StringBuilder packDetails = new StringBuilder("Pack: ");
                            for (int i = 0; i < packItems.size(); i++) {
                                PackItem item = packItems.get(i);
                                Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                                if (mattress != null) {
                                    if (i > 0) packDetails.append(", ");
                                    packDetails.append(item.getQuantity()).append("x ");
                                    packDetails.append(mattress.getSize()).append(" - ").append(mattress.getReference());
                                }
                            }
                            mattressDisplay = packDetails.toString();
                        } else {
                            mattressDisplay = "Pack";
                        }
                        totalPrice = salePrice; // Pack price is already total
                    } else {
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                        totalPrice = salePrice * t.getQuantity();
                        mattressDisplay = mattress != null ? mattress.getSize() + " - " + mattress.getReference() : "ID: " + t.getMattressId();
                    }
                    
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattressDisplay, cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", salePrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
                }
            }
            document.add(table);
            document.close();
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating weekly report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate monthly transactions report and return the file path
     * Returns null if generation fails
     */
    public static String generateMonthlyTransactionsReport(LocalDate date, String filename) {
        try {
            // Ensure folder exists
            getMonthlyReportsFolderPath();
            
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            String filePath = getMonthlyReportsFolderPath() + File.separator + filename;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
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
            int packCount = 0;
            int transactionCount = 0;
            
            for (Transaction t : transactions) {
                if (t.getDate().getYear() == date.getYear() && t.getDate().getMonthValue() == date.getMonthValue()) {
                    transactionCount++;
                    String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                    
                    if (type.startsWith("vente")) {
                        double revenue = t.getPrix() * t.getQuantity();
                        monthlyRevenue += revenue;
                        
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        if (mattress != null) {
                            double cost = mattress.getUnitPrice() * t.getQuantity();
                            monthlyCost += cost;
                            monthlyProfit += (revenue - cost);
                        }
                        salesCount++;
                    } else if (type.startsWith("pack")) {
                        // For packs, prix contains the total pack price
                        double packRevenue = t.getPrix();
                        monthlyRevenue += packRevenue;
                        
                        // Calculate cost based on individual pack items
                        List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                        double packCost = 0.0;
                        for (PackItem item : packItems) {
                            Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                            if (mattress != null) {
                                packCost += mattress.getUnitPrice() * item.getQuantity();
                            }
                        }
                        monthlyCost += packCost;
                        monthlyProfit += (packRevenue - packCost);
                        packCount++;
                        salesCount++;
                    }
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé du mois:", headerFont));
            document.add(new Paragraph("Nombre de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount + (packCount > 0 ? " (dont " + packCount + " pack(s))" : ""), cellFont));
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
            table.addCell(new PdfPCell(new Phrase("Matelas / Pack", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            
            for (Transaction t : transactions) {
                if (t.getDate().getYear() == date.getYear() && t.getDate().getMonthValue() == date.getMonthValue()) {
                    String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                    String mattressDisplay = "";
                    double unitPrice = 0.0;
                    double salePrice = t.getPrix();
                    double totalPrice;
                    
                    if (type.startsWith("pack")) {
                        // For packs, show pack details
                        List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                        if (packItems != null && !packItems.isEmpty()) {
                            StringBuilder packDetails = new StringBuilder("Pack: ");
                            for (int i = 0; i < packItems.size(); i++) {
                                PackItem item = packItems.get(i);
                                Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                                if (mattress != null) {
                                    if (i > 0) packDetails.append(", ");
                                    packDetails.append(item.getQuantity()).append("x ");
                                    packDetails.append(mattress.getSize()).append(" - ").append(mattress.getReference());
                                }
                            }
                            mattressDisplay = packDetails.toString();
                        } else {
                            mattressDisplay = "Pack";
                        }
                        totalPrice = salePrice; // Pack price is already total
                    } else {
                        Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                        unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                        totalPrice = salePrice * t.getQuantity();
                        mattressDisplay = mattress != null ? mattress.getSize() + " - " + mattress.getReference() : "ID: " + t.getMattressId();
                    }
                    
                    table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(mattressDisplay, cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", salePrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
                }
            }
            document.add(table);
            document.close();
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating monthly report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate stock report and return the file path
     * Returns null if generation fails
     */
    public static String generateStockReport(String filename) {
        try {
            // Ensure folder exists
            getStockReportsFolderPath();
            
            List<Mattress> mattresses = MattressDAO.getAllMattresses();
            Document document = new Document();
            String filePath = getStockReportsFolderPath() + File.separator + filename;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
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
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating stock report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate transaction report and return the file path
     * Returns null if generation fails
     */
    public static String generateTransactionReport(String filename) {
        try {
            // Ensure folder exists
            getTransactionReportsFolderPath();
            
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Document document = new Document();
            String filePath = getTransactionReportsFolderPath() + File.separator + filename;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            document.add(new Paragraph("Rapport de Transactions - MatelasPro", titleFont));
            document.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
            document.add(Chunk.NEWLINE);
            
            // Calculate overall statistics
            int salesCount = 0;
            int packCount = 0;
            double totalRevenue = 0;
            double totalCost = 0;
            double totalProfit = 0;
            
            for (Transaction t : transactions) {
                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                if (type.startsWith("vente")) {
                    double revenue = t.getPrix() * t.getQuantity();
                    totalRevenue += revenue;
                    
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    if (mattress != null) {
                        double cost = mattress.getUnitPrice() * t.getQuantity();
                        totalCost += cost;
                        totalProfit += (revenue - cost);
                    }
                    salesCount++;
                } else if (type.startsWith("pack")) {
                    // For packs, prix contains the total pack price
                    double packRevenue = t.getPrix();
                    totalRevenue += packRevenue;
                    
                    // Calculate cost based on individual pack items
                    List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                    double packCost = 0.0;
                    for (PackItem item : packItems) {
                        Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                        if (mattress != null) {
                            packCost += mattress.getUnitPrice() * item.getQuantity();
                        }
                    }
                    totalCost += packCost;
                    totalProfit += (packRevenue - packCost);
                    packCount++;
                    salesCount++;
                }
            }
            
            document.add(new Paragraph("Statistiques globales:", headerFont));
            document.add(new Paragraph("Nombre total de transactions: " + transactions.size(), cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount + (packCount > 0 ? " (dont " + packCount + " pack(s))" : ""), cellFont));
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
            table.addCell(new PdfPCell(new Phrase("Matelas / Pack", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix vente (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            
            for (Transaction t : transactions) {
                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                String mattressDisplay = "";
                double unitPrice = 0.0;
                double salePrice = t.getPrix();
                double totalPrice;
                
                if (type.startsWith("pack")) {
                    // For packs, show pack details
                    List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                    if (packItems != null && !packItems.isEmpty()) {
                        StringBuilder packDetails = new StringBuilder("Pack: ");
                        for (int i = 0; i < packItems.size(); i++) {
                            PackItem item = packItems.get(i);
                            Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                            if (mattress != null) {
                                if (i > 0) packDetails.append(", ");
                                packDetails.append(item.getQuantity()).append("x ");
                                packDetails.append(mattress.getSize()).append(" - ").append(mattress.getReference());
                            }
                        }
                        mattressDisplay = packDetails.toString();
                    } else {
                        mattressDisplay = "Pack";
                    }
                    totalPrice = salePrice; // Pack price is already total
                    unitPrice = 0.0; // Not applicable for packs
                } else {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                    totalPrice = salePrice * t.getQuantity();
                    mattressDisplay = mattress != null ? mattress.getSize() + " - " + mattress.getReference() : "ID: " + t.getMattressId();
                }
                
                table.addCell(new PdfPCell(new Phrase(t.getDate().toLocalDate().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(mattressDisplay, cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", salePrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getStoreOwnerId() != null ? String.valueOf(t.getStoreOwnerId()) : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
            }
            document.add(table);
            document.close();
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating transaction report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate transactions report for a date range and return the file path
     * Returns null if generation fails
     */
    public static String generateDateRangeTransactionsReport(LocalDate startDate, LocalDate endDate, String filename) {
        try {
            // Ensure folder exists
            getTransactionReportsFolderPath();
            
            List<Transaction> allTransactions = TransactionDAO.getAllTransactions();
            // Filter transactions by date range (inclusive: startDate 00:00:00 to endDate 23:59:59)
            List<Transaction> transactions = new ArrayList<>();
            LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 23:59:59
            for (Transaction t : allTransactions) {
                LocalDateTime transactionDateTime = t.getDate();
                if (!transactionDateTime.isBefore(startDateTime) && !transactionDateTime.isAfter(endDateTime)) {
                    transactions.add(t);
                }
            }
            
            Document document = new Document();
            String filePath = getTransactionReportsFolderPath() + File.separator + filename;
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
            
            document.add(new Paragraph("Rapport de Transactions - MatelasPro", titleFont));
            document.add(new Paragraph("Période: " + startDate.format(DateTimeFormatter.ISO_DATE) + 
                                      " au " + endDate.format(DateTimeFormatter.ISO_DATE)));
            document.add(new Paragraph("Généré le: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
            document.add(Chunk.NEWLINE);
            
            // Calculate summary statistics
            int transactionCount = transactions.size();
            int salesCount = 0;
            int packCount = 0;
            double totalRevenue = 0;
            double totalCost = 0;
            double totalProfit = 0;
            int totalQuantity = 0;
            
            // Group by type
            Map<String, Integer> typeCounts = new HashMap<>();
            Map<String, Double> typeRevenue = new HashMap<>();
            
            for (Transaction t : transactions) {
                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                typeCounts.put(t.getType() != null ? t.getType() : "Inconnu", 
                              typeCounts.getOrDefault(t.getType() != null ? t.getType() : "Inconnu", 0) + 1);
                
                if (type.startsWith("vente")) {
                    double revenue = t.getPrix() * t.getQuantity();
                    totalRevenue += revenue;
                    typeRevenue.put("Vente", typeRevenue.getOrDefault("Vente", 0.0) + revenue);
                    
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    if (mattress != null) {
                        double cost = mattress.getUnitPrice() * t.getQuantity();
                        totalCost += cost;
                        totalProfit += (revenue - cost);
                    }
                    salesCount++;
                    totalQuantity += t.getQuantity();
                } else if (type.startsWith("pack")) {
                    double packRevenue = t.getPrix();
                    totalRevenue += packRevenue;
                    typeRevenue.put("Pack", typeRevenue.getOrDefault("Pack", 0.0) + packRevenue);
                    
                    List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                    double packCost = 0.0;
                    for (PackItem item : packItems) {
                        Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                        if (mattress != null) {
                            packCost += mattress.getUnitPrice() * item.getQuantity();
                            totalQuantity += item.getQuantity();
                        }
                    }
                    totalCost += packCost;
                    totalProfit += (packRevenue - packCost);
                    packCount++;
                    salesCount++;
                } else {
                    totalQuantity += t.getQuantity();
                }
            }
            
            // Add summary
            document.add(new Paragraph("Résumé:", headerFont));
            document.add(new Paragraph("Nombre total de transactions: " + transactionCount, cellFont));
            document.add(new Paragraph("Nombre de ventes: " + salesCount + (packCount > 0 ? " (dont " + packCount + " pack(s))" : ""), cellFont));
            document.add(new Paragraph("Quantité totale: " + totalQuantity, cellFont));
            document.add(new Paragraph("Chiffre d'affaires total: " + String.format("%.2f", totalRevenue) + " DT", cellFont));
            document.add(new Paragraph("Coût total d'achat: " + String.format("%.2f", totalCost) + " DT", cellFont));
            document.add(new Paragraph("Bénéfice net: " + String.format("%.2f", totalProfit) + " DT", cellFont));
            if (totalRevenue > 0) {
                double profitMargin = (totalProfit / totalRevenue) * 100;
                document.add(new Paragraph("Marge bénéficiaire: " + String.format("%.2f", profitMargin) + "%", cellFont));
            }
            document.add(Chunk.NEWLINE);
            
            // Add breakdown by type
            if (!typeCounts.isEmpty()) {
                document.add(new Paragraph("Répartition par type:", headerFont));
                for (java.util.Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                    String typeName = entry.getKey();
                    int count = entry.getValue();
                    double revenue = typeRevenue.getOrDefault(typeName, 0.0);
                    document.add(new Paragraph(typeName + ": " + count + " transaction(s)" + 
                                              (revenue > 0 ? " - " + String.format("%.2f", revenue) + " DT" : ""), cellFont));
                }
                document.add(Chunk.NEWLINE);
            }
            
            // Sort transactions by date ascending
            transactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
            
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Date/Heure", headerFont)));
            table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Matelas/Produit", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Qté", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Prix unitaire (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total (DT)", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Propriétaire", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Notes", headerFont)));
            
            for (Transaction t : transactions) {
                String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
                String mattressDisplay = "";
                double unitPrice = 0.0;
                double totalPrice;
                
                if (type.startsWith("pack")) {
                    List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                    if (packItems != null && !packItems.isEmpty()) {
                        StringBuilder packDetails = new StringBuilder("Pack: ");
                        for (int i = 0; i < packItems.size(); i++) {
                            PackItem item = packItems.get(i);
                            Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                            if (mattress != null) {
                                if (i > 0) packDetails.append(", ");
                                packDetails.append(item.getQuantity()).append("x ");
                                packDetails.append(mattress.getSize()).append(" - ").append(mattress.getReference());
                            }
                        }
                        mattressDisplay = packDetails.toString();
                    } else {
                        mattressDisplay = "Pack";
                    }
                    totalPrice = t.getPrix();
                    unitPrice = 0.0;
                } else {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    unitPrice = mattress != null ? mattress.getUnitPrice() : 0.0;
                    totalPrice = t.getPrix() * t.getQuantity();
                    mattressDisplay = mattress != null ? mattress.getSize() + " - " + mattress.getReference() : "ID: " + t.getMattressId();
                }
                
                table.addCell(new PdfPCell(new Phrase(t.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getId()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getType() != null ? t.getType() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(mattressDisplay, cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", unitPrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalPrice), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getStoreOwnerName() != null ? t.getStoreOwnerName() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "", cellFont)));
            }
            
            document.add(table);
            document.close();
            return filePath;
        } catch (Exception e) {
            System.err.println("Error generating date range report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 