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
    @FXML private DatePicker monthlyDatePicker;
    @FXML private Label statusLabel;
    
    @FXML
    public void initialize() {
        // Set default dates
        dailyDatePicker.setValue(LocalDate.now());
        monthlyDatePicker.setValue(LocalDate.now());
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
            boolean success = PdfReportUtil.generateDailyTransactionsReport(selectedDate, filename);
            
            if (success) {
                statusLabel.setText("✅ Rapport quotidien généré avec succès: " + filename);
                showAlert("Succès", "Rapport quotidien généré avec succès: " + filename, AlertType.INFORMATION);
            } else {
                statusLabel.setText("❌ Échec de la génération du rapport quotidien");
                showAlert("Erreur", "Échec de la génération du rapport quotidien.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la génération du rapport");
            showAlert("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage(), AlertType.ERROR);
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
            boolean success = PdfReportUtil.generateMonthlyTransactionsReport(selectedDate, filename);
            
            if (success) {
                statusLabel.setText("✅ Rapport mensuel généré avec succès: " + filename);
                showAlert("Succès", "Rapport mensuel généré avec succès: " + filename, AlertType.INFORMATION);
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
            boolean success = PdfReportUtil.generateStockReport(filename);
            
            if (success) {
                statusLabel.setText("✅ Rapport de stock généré avec succès: " + filename);
                showAlert("Succès", "Rapport de stock généré avec succès: " + filename, AlertType.INFORMATION);
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
            String filename = "rapport_transactions_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            boolean success = PdfReportUtil.generateTransactionReport(filename);
            
            if (success) {
                statusLabel.setText("✅ Rapport de transactions généré avec succès: " + filename);
                showAlert("Succès", "Rapport de transactions généré avec succès: " + filename, AlertType.INFORMATION);
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