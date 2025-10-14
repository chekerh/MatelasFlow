package com.warehouse.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.warehouse.model.Transaction;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.net.URL;

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
            System.out.println("Created reports folder: " + reportsFolderPath);
        }
        
        return reportsFolderPath;
    }
    
    /**
     * Get full path for a report file
     */
    private static String getReportFilePath(String filename) {
        return getReportsFolderPath() + File.separator + filename;
    }
    
    /**
     * Add company header with logo to the document
     */
    private static void addHeader(Document document) throws Exception {
        try {
            // Try to load logo from resources
            URL logoUrl = PdfReportUtil.class.getResource("/images/SuperMousse.jpg");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_LEFT);
                
                // Create header table with logo and company info
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                headerTable.setWidths(new float[]{1, 3});
                
                // Logo cell
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerTable.addCell(logoCell);
                
                // Company info cell
                Font companyFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.DARK_GRAY);
                Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
                
                Paragraph companyInfo = new Paragraph();
                companyInfo.add(new Chunk("MatelasPro\n", companyFont));
                companyInfo.add(new Chunk("Syst\u00e8me de Gestion d'Entrep\u00f4t STE HABIBA", subtitleFont));
                
                PdfPCell infoCell = new PdfPCell(companyInfo);
                infoCell.setBorder(Rectangle.NO_BORDER);
                infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerTable.addCell(infoCell);
                
                document.add(headerTable);
                document.add(new Paragraph(" ")); // Spacing
                
                // Add separator line
                com.itextpdf.text.pdf.draw.LineSeparator line = new com.itextpdf.text.pdf.draw.LineSeparator();
                line.setLineColor(BaseColor.LIGHT_GRAY);
                document.add(new Chunk(line));
                document.add(new Paragraph(" ")); // Spacing
            } else {
                System.out.println("⚠️ Logo non trouv\u00e9, utilisation du header texte");
                // Fallback to text-only header
                Font companyFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.DARK_GRAY);
                Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
                
                Paragraph header = new Paragraph();
                header.add(new Chunk("MatelasPro\n", companyFont));
                header.add(new Chunk("Syst\u00e8me de Gestion d'Entrep\u00f4t STE HABIBA\n\n", subtitleFont));
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);
                
                com.itextpdf.text.pdf.draw.LineSeparator line = new com.itextpdf.text.pdf.draw.LineSeparator();
                line.setLineColor(BaseColor.LIGHT_GRAY);
                document.add(new Chunk(line));
                document.add(new Paragraph(" ")); // Spacing
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur lors de l'ajout du logo: " + e.getMessage());
            // Continue without logo
        }
    }
    
    public static boolean generateDailyTransactionsReport(LocalDate date, String filename) {
        try {
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            System.out.println("📊 Génération du rapport quotidien pour: " + date);
            System.out.println("📋 Nombre total de transactions en base: " + transactions.size());
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            
            // Add logo header
            addHeader(document);
            
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            
            Paragraph title = new Paragraph("📅 Rapport des transactions du jour", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph datePara = new Paragraph("Date: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);
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
                    System.out.println("✅ Transaction trouvée: " + t.getType() + " - " + t.getDate());
                }
            }
            System.out.println("📊 Transactions trouvées pour " + date + ": " + transactionCount);
            
            // Add summary box
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10f);
            summaryTable.setSpacingAfter(10f);
            
            Font summaryFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            Font summaryValueFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
            
            // Transactions cell
            PdfPCell cell1 = new PdfPCell();
            cell1.setBackgroundColor(new BaseColor(52, 152, 219)); // Blue
            cell1.setPadding(10);
            Paragraph p1 = new Paragraph();
            p1.add(new Chunk("📋 Transactions\n", summaryFont));
            p1.add(new Chunk(String.valueOf(transactionCount), summaryValueFont));
            p1.setAlignment(Element.ALIGN_CENTER);
            cell1.addElement(p1);
            summaryTable.addCell(cell1);
            
            // Quantity cell
            PdfPCell cell2 = new PdfPCell();
            cell2.setBackgroundColor(new BaseColor(46, 204, 113)); // Green
            cell2.setPadding(10);
            Paragraph p2 = new Paragraph();
            p2.add(new Chunk("📦 Quantit\u00e9 totale\n", summaryFont));
            p2.add(new Chunk(String.valueOf(totalQuantity), summaryValueFont));
            p2.setAlignment(Element.ALIGN_CENTER);
            cell2.addElement(p2);
            summaryTable.addCell(cell2);
            
            // Sales cell
            PdfPCell cell3 = new PdfPCell();
            cell3.setBackgroundColor(new BaseColor(230, 126, 34)); // Orange
            cell3.setPadding(10);
            Paragraph p3 = new Paragraph();
            p3.add(new Chunk("💰 Chiffre d'affaires\n", summaryFont));
            p3.add(new Chunk(String.format("%.2f DT", totalSales), summaryValueFont));
            p3.setAlignment(Element.ALIGN_CENTER);
            cell3.addElement(p3);
            summaryTable.addCell(cell3);
            
            document.add(summaryTable);
            
            // Check if there are transactions for this date
            if (transactionCount == 0) {
                document.add(new Paragraph("\n⚠️ Aucune transaction trouvée pour cette date.", cellFont));
                document.add(new Paragraph("\nVérifiez que :", cellFont));
                document.add(new Paragraph("• La date sélectionnée est correcte", cellFont));
                document.add(new Paragraph("• Des transactions ont été enregistrées ce jour-là", cellFont));
                document.add(new Paragraph("• La base de données contient des données", cellFont));
                document.close();
                System.out.println("⚠️ Rapport généré mais aucune transaction trouvée pour la date: " + date);
                System.out.println("Total transactions en DB: " + transactions.size());
                return true;
            }
            
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1.5f, 2f, 1f, 1.5f, 2f, 3f, 1.5f, 1.5f});
            table.setSpacingBefore(10f);
            
            // Header cells with colored background
            BaseColor headerColor = new BaseColor(52, 73, 94); // Dark blue
            String[] headers = {"Heure", "Type", "Matelas", "Qt\u00e9", "Prix (DT)", "Propri\u00e9taire", "Notes", "Date Retour", "ID"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setBackgroundColor(headerColor);
                headerCell.setPadding(8);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }
            
            // Data rows with alternating colors
            int rowNum = 0;
            for (Transaction t : transactions) {
                if (t.getDate().toLocalDate().equals(date)) {
                    Mattress mattress = MattressDAO.getMattressById(t.getMattressId());
                    StoreOwner storeOwner = t.getStoreOwnerId() != null ? StoreOwnerDAO.getStoreOwnerById(t.getStoreOwnerId()) : null;
                    
                    BaseColor rowColor = (rowNum % 2 == 0) ? BaseColor.WHITE : new BaseColor(245, 245, 245);
                    rowNum++;
                    
                    // Heure
                    PdfPCell cell = new PdfPCell(new Phrase(t.getDate().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    table.addCell(cell);
                    
                    // Type with icon
                    String typeIcon = "";
                    BaseColor typeColor = BaseColor.BLACK;
                    switch(t.getType()) {
                        case "Vente": typeIcon = "💰 "; typeColor = new BaseColor(46, 204, 113); break;
                        case "Pr\u00eat": typeIcon = "📦 "; typeColor = new BaseColor(52, 152, 219); break;
                        case "retour": typeIcon = "🔄 "; typeColor = new BaseColor(155, 89, 182); break;
                        case "Transfert": typeIcon = "🚚 "; typeColor = new BaseColor(230, 126, 34); break;
                        case "R\u00e9ception": typeIcon = "📥 "; typeColor = new BaseColor(26, 188, 156); break;
                    }
                    Font typeFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, typeColor);
                    cell = new PdfPCell(new Phrase(typeIcon + t.getType(), typeFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    table.addCell(cell);
                    
                    // Matelas
                    cell = new PdfPCell(new Phrase(mattress != null ? mattress.getType() + " (" + mattress.getSize() + ")" : "Inconnu", cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    table.addCell(cell);
                    
                    // Quantit\u00e9
                    cell = new PdfPCell(new Phrase(String.valueOf(t.getQuantity()), cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                    
                    // Prix
                    cell = new PdfPCell(new Phrase(String.format("%.2f", t.getPrix()), cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);
                    
                    // Propri\u00e9taire (NAME instead of ID!)
                    String ownerName = storeOwner != null ? "🏪 " + storeOwner.getName() : "-";
                    cell = new PdfPCell(new Phrase(ownerName, cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    table.addCell(cell);
                    
                    // Notes
                    cell = new PdfPCell(new Phrase(t.getNotes() != null ? t.getNotes() : "-", cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    table.addCell(cell);
                    
                    // Date Retour
                    String returnDate = t.getExpectedReturnDate() != null ? t.getExpectedReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
                    cell = new PdfPCell(new Phrase(returnDate, cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                    
                    // ID
                    cell = new PdfPCell(new Phrase(String.valueOf(t.getId()), cellFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(5);
                    table.addCell(cell);
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
            System.out.println("📊 Génération du rapport mensuel pour: " + date.getYear() + "-" + date.getMonthValue());
            System.out.println("📋 Nombre total de transactions en base: " + transactions.size());
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            
            // Add logo header
            addHeader(document);
            
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            
            Paragraph title = new Paragraph("📅 Rapport des transactions du mois", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph datePara = new Paragraph("Mois: " + date.format(DateTimeFormatter.ofPattern("MMMM yyyy")), dateFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);
            document.add(Chunk.NEWLINE);
            
            // Count transactions for this month
            int monthTransactionCount = 0;
            for (Transaction t : transactions) {
                if (t.getDate().getYear() == date.getYear() && t.getDate().getMonthValue() == date.getMonthValue()) {
                    monthTransactionCount++;
                }
            }
            System.out.println("📊 Transactions trouvées pour le mois: " + monthTransactionCount);
            
            if (monthTransactionCount == 0) {
                document.add(new Paragraph("\n⚠️ Aucune transaction trouvée pour ce mois.", cellFont));
                document.add(new Paragraph("\nVérifiez que :", cellFont));
                document.add(new Paragraph("• Le mois sélectionné est correct", cellFont));
                document.add(new Paragraph("• Des transactions ont été enregistrées ce mois-là", cellFont));
                document.add(new Paragraph("• La base de données contient des données", cellFont));
                document.close();
                System.out.println("⚠️ Rapport généré mais aucune transaction trouvée pour le mois");
                return true;
            }
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
            System.out.println("📊 Génération du rapport de stock");
            System.out.println("🛍️ Nombre de matelas en stock: " + mattresses.size());
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            
            // Add logo header
            addHeader(document);
            
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            
            Paragraph title = new Paragraph("📦 Rapport de Stock - MatelasPro", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph datePara = new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);
            document.add(Chunk.NEWLINE);
            
            if (mattresses.isEmpty()) {
                document.add(new Paragraph("\n⚠️ Aucun matelas trouvé en stock.", cellFont));
                document.add(new Paragraph("\nVérifiez que :", cellFont));
                document.add(new Paragraph("• Des matelas ont été ajoutés à l'inventaire", cellFont));
                document.add(new Paragraph("• La base de données contient des données", cellFont));
                document.close();
                System.out.println("⚠️ Rapport généré mais aucun matelas en stock");
                return true;
            }
            
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Taille", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Marque", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Quantité", headerFont)));
            for (Mattress m : mattresses) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(m.getId()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getType(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getSize(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(m.getBrand() != null ? m.getBrand() : "", cellFont)));
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
            System.out.println("📊 Génération du rapport de toutes les transactions");
            System.out.println("📋 Nombre total de transactions: " + transactions.size());
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(getReportFilePath(filename)));
            document.open();
            
            // Add logo header
            addHeader(document);
            
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            
            Paragraph title = new Paragraph("📋 Rapport de Transactions - MatelasPro", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph datePara = new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);
            document.add(Chunk.NEWLINE);
            
            if (transactions.isEmpty()) {
                document.add(new Paragraph("\n⚠️ Aucune transaction trouvée.", cellFont));
                document.add(new Paragraph("\nVérifiez que :", cellFont));
                document.add(new Paragraph("• Des transactions ont été enregistrées", cellFont));
                document.add(new Paragraph("• La base de données contient des données", cellFont));
                document.close();
                System.out.println("⚠️ Rapport généré mais aucune transaction en base");
                return true;
            }
            
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