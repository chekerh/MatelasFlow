package com.warehouse.util;

import com.warehouse.model.Transaction;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.PackItem;
import com.warehouse.model.PackItemDAO;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CsvExportUtil {
    
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
     * Export transactions to CSV file
     * @param transactions List of transactions to export
     * @param startDate Start date for filename
     * @param endDate End date for filename
     * @param filename Output filename
     * @return File path if successful, null otherwise
     */
    public static String exportTransactionsToCsv(ObservableList<Transaction> transactions, 
                                                 LocalDate startDate, LocalDate endDate, 
                                                 String filename) {
        try {
            // Ensure folder exists
            getTransactionReportsFolderPath();
            
            String filePath = getTransactionReportsFolderPath() + File.separator + filename;
            FileWriter writer = new FileWriter(filePath);
            
            // Write BOM for UTF-8 (helps Excel recognize encoding)
            writer.write('\ufeff');
            
            // Write header
            writer.append("Date/Heure,ID,Type,Matelas/Produit,Quantité,Prix unitaire (DT),Total (DT),Propriétaire,Notes\n");
            
            // Write data
            for (Transaction t : transactions) {
                if (t == null) continue;
                
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
                                if (i > 0) packDetails.append("; ");
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
                
                // Escape CSV values (handle commas and quotes)
                String dateTime = t.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String id = String.valueOf(t.getId());
                String typeStr = escapeCsvValue(t.getType() != null ? t.getType() : "");
                String mattressStr = escapeCsvValue(mattressDisplay);
                String quantity = String.valueOf(t.getQuantity());
                String unitPriceStr = String.format("%.2f", unitPrice);
                String totalPriceStr = String.format("%.2f", totalPrice);
                String ownerStr = escapeCsvValue(t.getStoreOwnerName() != null ? t.getStoreOwnerName() : "");
                String notesStr = escapeCsvValue(t.getNotes() != null ? t.getNotes() : "");
                
                writer.append(dateTime).append(",")
                      .append(id).append(",")
                      .append(typeStr).append(",")
                      .append(mattressStr).append(",")
                      .append(quantity).append(",")
                      .append(unitPriceStr).append(",")
                      .append(totalPriceStr).append(",")
                      .append(ownerStr).append(",")
                      .append(notesStr).append("\n");
            }
            
            writer.flush();
            writer.close();
            return filePath;
        } catch (IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Escape CSV value (handle commas, quotes, and newlines)
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}







