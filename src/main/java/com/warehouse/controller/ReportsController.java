package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.warehouse.util.PdfReportUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportsController {
    @FXML private DatePicker dailyDatePicker;
    @FXML private DatePicker weeklyDatePicker;
    @FXML private DatePicker monthlyDatePicker;
    @FXML private DatePicker transactionStartDatePicker;
    @FXML private DatePicker transactionEndDatePicker;
    @FXML private Label statusLabel;
    
    @FXML
    public void initialize() {
        // Set default dates
        dailyDatePicker.setValue(LocalDate.now());
        if (weeklyDatePicker != null) {
            weeklyDatePicker.setValue(LocalDate.now());
        }
        monthlyDatePicker.setValue(LocalDate.now());
        
        // Set default date range for transaction report (last 30 days)
        if (transactionStartDatePicker != null) {
            transactionStartDatePicker.setValue(LocalDate.now().minusDays(30));
        }
        if (transactionEndDatePicker != null) {
            transactionEndDatePicker.setValue(LocalDate.now());
        }
    }
    
    @FXML
    private void generateWeeklyReport() {
        try {
            LocalDate selectedDate = weeklyDatePicker != null ? weeklyDatePicker.getValue() : LocalDate.now();
            if (selectedDate == null) {
                showAlert("Erreur", "Veuillez sélectionner une date.", AlertType.ERROR);
                return;
            }
            
            String filename = "rapport_hebdomadaire_" + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String filePath = PdfReportUtil.generateWeeklyTransactionsReport(selectedDate, filename);
            
            String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + java.io.File.separator + "LES RAPPORT QUOTIDIEN";
            
            if (filePath != null) {
                statusLabel.setText("✅ Rapport hebdomadaire généré avec succès");
                // Open PDF and folder automatically
                PdfReportUtil.openPdfFile(filePath);
                PdfReportUtil.openFolder(desktopPath);
                showAlert("Succès", "Rapport hebdomadaire généré avec succès!\n\nFichier: " + filename + "\nEmplacement: " + desktopPath, AlertType.INFORMATION);
            } else {
                statusLabel.setText("❌ Échec de la génération du rapport hebdomadaire");
                showAlert("Erreur", "Échec de la génération du rapport hebdomadaire.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la génération du rapport");
            showAlert("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    @FXML
    private void generateDailyReport() {
        try {
            LocalDate selectedDate = dailyDatePicker.getValue();
            if (selectedDate == null) {
                showAlert("Erreur", "Veuillez sélectionner une date.", AlertType.ERROR);
                return;
            }
            
            String filename = "rapport_quotidien_" + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String filePath = PdfReportUtil.generateDailyTransactionsReport(selectedDate, filename);
            
            String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + java.io.File.separator + "LES RAPPORT QUOTIDIEN";
            
            if (filePath != null) {
                statusLabel.setText("✅ Rapport quotidien généré avec succès");
                // Open PDF and folder automatically
                PdfReportUtil.openPdfFile(filePath);
                PdfReportUtil.openFolder(desktopPath);
                showAlert("Succès", "Rapport quotidien généré avec succès!\n\nFichier: " + filename + "\nEmplacement: " + desktopPath, AlertType.INFORMATION);
            } else {
                statusLabel.setText("❌ Échec de la génération du rapport quotidien");
                showAlert("Erreur", "Échec de la génération du rapport quotidien.\nVérifiez les logs pour plus de détails.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la génération du rapport");
            showAlert("Erreur", "Erreur lors de la génération du rapport:\n" + e.getMessage() + "\n\nVérifiez que:\n- Les dossiers peuvent être créés sur le Bureau\n- Aucun fichier n'est ouvert avec ce nom\n- Vous avez les permissions d'écriture", AlertType.ERROR);
        }
    }
    
    @FXML
    private void generateMonthlyReport() {
        try {
            LocalDate selectedDate = monthlyDatePicker.getValue();
            if (selectedDate == null) {
                showAlert("Erreur", "Veuillez sélectionner un mois.", AlertType.ERROR);
                return;
            }
            
            String filename = "rapport_mensuel_" + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM")) + ".pdf";
            String filePath = PdfReportUtil.generateMonthlyTransactionsReport(selectedDate, filename);
            
            String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + java.io.File.separator + "MONSUEL";
            
            if (filePath != null) {
                statusLabel.setText("✅ Rapport mensuel généré avec succès");
                // Open PDF and folder automatically
                PdfReportUtil.openPdfFile(filePath);
                PdfReportUtil.openFolder(desktopPath);
                showAlert("Succès", "Rapport mensuel généré avec succès!\n\nFichier: " + filename + "\nEmplacement: " + desktopPath, AlertType.INFORMATION);
            } else {
                statusLabel.setText("❌ Échec de la génération du rapport mensuel");
                showAlert("Erreur", "Échec de la génération du rapport mensuel.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la génération du rapport");
            showAlert("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    @FXML
    private void generateStockReport() {
        try {
            String filename = "rapport_stock_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String filePath = PdfReportUtil.generateStockReport(filename);
            
            String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + java.io.File.separator + "RAPPORT DE STOCK";
            
            if (filePath != null) {
                statusLabel.setText("✅ Rapport de stock généré avec succès");
                // Open PDF and folder automatically
                PdfReportUtil.openPdfFile(filePath);
                PdfReportUtil.openFolder(desktopPath);
                showAlert("Succès", "Rapport de stock généré avec succès!\n\nFichier: " + filename + "\nEmplacement: " + desktopPath, AlertType.INFORMATION);
            } else {
                statusLabel.setText("❌ Échec de la génération du rapport de stock");
                showAlert("Erreur", "Échec de la génération du rapport de stock.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la génération du rapport");
            showAlert("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    @FXML
    private void generateTransactionReport() {
        try {
            // Get date range from date pickers
            LocalDate startDate = transactionStartDatePicker != null ? transactionStartDatePicker.getValue() : null;
            LocalDate endDate = transactionEndDatePicker != null ? transactionEndDatePicker.getValue() : null;
            
            // Validate dates
            if (startDate == null || endDate == null) {
                showAlert("Erreur", "Veuillez sélectionner une date de début et une date de fin.", AlertType.ERROR);
                statusLabel.setText("❌ Veuillez sélectionner les dates");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                showAlert("Erreur", "La date de début doit être antérieure ou égale à la date de fin.", AlertType.ERROR);
                statusLabel.setText("❌ Date de début invalide");
                return;
            }
            
            // Generate filename with date range
            String filename = "transactions_" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                            "_to_" + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            // Use the date range report generation method
            String filePath = PdfReportUtil.generateDateRangeTransactionsReport(startDate, endDate, filename);
            
            String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + 
                                java.io.File.separator + "RAPPORT DE TRANSACTION";
            
            if (filePath != null) {
                statusLabel.setText("✅ Rapport de transactions généré avec succès");
                // Open PDF and folder automatically
                PdfReportUtil.openPdfFile(filePath);
                PdfReportUtil.openFolder(desktopPath);
                showAlert("Succès", 
                    "Rapport de transactions généré avec succès!\n\n" +
                    "Période: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                    " au " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n" +
                    "Fichier: " + filename + "\n" +
                    "Emplacement: " + desktopPath, 
                    AlertType.INFORMATION);
            } else {
                statusLabel.setText("❌ Échec de la génération du rapport de transactions");
                showAlert("Erreur", "Échec de la génération du rapport de transactions.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la génération du rapport");
            showAlert("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 