package com.warehouse.util;

import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class NotificationSystem {
    
    public enum NotificationType {
        SUCCESS("✅ Succès", "green"),
        WARNING("⚠️ Attention", "orange"),
        ERROR("❌ Erreur", "red"),
        INFO("ℹ️ Information", "blue"),
        CRITICAL("🚨 Critique", "darkred");
        
        private final String icon;
        private final String color;
        
        NotificationType(String icon, String color) {
            this.icon = icon;
            this.color = color;
        }
        
        public String getIcon() { return icon; }
        public String getColor() { return color; }
    }
    
    private static final Map<String, Integer> notificationCounts = new HashMap<>();
    private static final int MAX_NOTIFICATIONS_PER_HOUR = 20;
    
    public static void showNotification(String title, String message, NotificationType type) {
        // Vérifier le nombre de notifications par heure
        String hourKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        int count = notificationCounts.getOrDefault(hourKey, 0);
        
        if (count >= MAX_NOTIFICATIONS_PER_HOUR) {
            return; // Éviter le spam
        }
        
        notificationCounts.put(hourKey, count + 1);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(type.getIcon() + " " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Personnaliser l'apparence selon le type
        switch (type) {
            case SUCCESS:
                alert.setAlertType(Alert.AlertType.INFORMATION);
                break;
            case WARNING:
                alert.setAlertType(Alert.AlertType.WARNING);
                break;
            case ERROR:
                alert.setAlertType(Alert.AlertType.ERROR);
                break;
            case CRITICAL:
                alert.setAlertType(Alert.AlertType.ERROR);
                break;
            default:
                alert.setAlertType(Alert.AlertType.INFORMATION);
        }
        
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("❓ " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait().orElse(null) == ButtonType.OK;
    }
    
    public static void showStockAlert(String mattressName, int currentStock, int threshold) {
        if (currentStock <= threshold) {
            showNotification(
                "Stock Faible",
                "Le matelas '" + mattressName + "' a un stock faible (" + currentStock + " unités).\n" +
                "Seuil d'alerte: " + threshold + " unités",
                NotificationType.WARNING
            );
        }
    }
    
    public static void showTransactionAlert(String type, String details) {
        NotificationType notificationType = NotificationType.INFO;
        
        switch (type) {
            case "Vente":
                notificationType = NotificationType.SUCCESS;
                break;
            case "retour":
                notificationType = NotificationType.WARNING;
                break;
            case "Réception":
                notificationType = NotificationType.INFO;
                break;
            default:
                notificationType = NotificationType.INFO;
        }
        
        showNotification(
            "Transaction " + type,
            details,
            notificationType
        );
    }
    
    public static void showSecurityAlert(String username, String activity) {
        showNotification(
            "Activité Suspecte",
            "Utilisateur: " + username + "\nActivité: " + activity + "\n" +
            "Heure: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            NotificationType.CRITICAL
        );
    }
    
    public static void showDailySummary(int totalTransactions, double totalRevenue, int lowStockItems) {
        StringBuilder message = new StringBuilder();
        message.append("📊 Résumé Quotidien\n\n");
        message.append("Transactions: ").append(totalTransactions).append("\n");
        message.append("Revenus: ").append(String.format("%.2f DT", totalRevenue)).append("\n");
        message.append("Stock faible: ").append(lowStockItems).append(" articles\n");
        
        NotificationType type = lowStockItems > 0 ? NotificationType.WARNING : NotificationType.SUCCESS;
        
        showNotification("Résumé Quotidien", message.toString(), type);
    }
    
    public static void showBackupReminder() {
        showNotification(
            "Sauvegarde Recommandée",
            "Il est recommandé de faire une sauvegarde de la base de données.\n" +
            "Dernière sauvegarde: " + getLastBackupDate(),
            NotificationType.INFO
        );
    }
    
    private static String getLastBackupDate() {
        // Simuler une date de dernière sauvegarde
        return LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public static void showPerformanceAlert(String metric, double value, double threshold) {
        NotificationType type = value > threshold ? NotificationType.WARNING : NotificationType.INFO;
        
        showNotification(
            "Performance",
            metric + ": " + String.format("%.2f", value) + "\nSeuil: " + String.format("%.2f", threshold),
            type
        );
    }
} 