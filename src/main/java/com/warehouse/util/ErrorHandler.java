package com.warehouse.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Centralized error handling utility for user-friendly error messages
 * and consistent logging across the application.
 */
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    
    /**
     * Handles exceptions from async operations and shows user-friendly notifications
     */
    public static <T> CompletableFuture<T> handleAsyncError(
            CompletableFuture<T> future, 
            String userMessage,
            String logContext) {
        return future.exceptionally(ex -> {
            logger.error("Error in {}: {}", logContext, ex.getMessage(), ex);
            Platform.runLater(() -> {
                showUserError(userMessage, ex);
            });
            return null;
        });
    }
    
    /**
     * Shows a user-friendly error message
     */
    public static void showUserError(String message, Throwable ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            
            String detailedMessage = message;
            if (ex != null) {
                String cause = getRootCause(ex).getMessage();
                if (cause != null && !cause.isEmpty()) {
                    // Make database errors more user-friendly
                    if (cause.contains("Connection") || cause.contains("SQL")) {
                        detailedMessage += "\n\nVérifiez que la base de données est démarrée (XAMPP MySQL).";
                    } else if (cause.contains("Unknown column")) {
                        detailedMessage += "\n\nErreur de schéma de base de données. Contactez l'administrateur.";
                    } else {
                        detailedMessage += "\n\nDétails: " + cause;
                    }
                }
            }
            
            alert.setContentText(detailedMessage);
            alert.showAndWait();
        });
    }
    
    /**
     * Shows error in a label (for non-blocking errors)
     */
    public static void showErrorInLabel(Label errorLabel, String message) {
        if (errorLabel != null) {
            Platform.runLater(() -> {
                errorLabel.setText(message);
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            });
        }
    }
    
    /**
     * Logs error and shows notification via dashboard controller
     */
    public static void handleError(String context, Throwable ex, Runnable onError) {
        logger.error("Error in {}: {}", context, ex.getMessage(), ex);
        if (onError != null) {
            Platform.runLater(onError);
        }
    }
    
    /**
     * Gets the root cause of an exception
     */
    private static Throwable getRootCause(Throwable ex) {
        Throwable cause = ex.getCause();
        if (cause == null || cause == ex) {
            return ex;
        }
        return getRootCause(cause);
    }
    
    /**
     * Creates a user-friendly error message from exception
     */
    public static String getUserFriendlyMessage(Throwable ex) {
        if (ex == null) return "Une erreur inattendue s'est produite.";
        
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = ex.getClass().getSimpleName();
        }
        
        // Translate common database errors
        if (message.contains("Connection refused") || message.contains("Communications link failure")) {
            return "Impossible de se connecter à la base de données. Vérifiez que MySQL est démarré dans XAMPP.";
        }
        if (message.contains("Access denied")) {
            return "Accès refusé à la base de données. Vérifiez les identifiants.";
        }
        if (message.contains("Unknown database")) {
            return "Base de données introuvable. Vérifiez la configuration.";
        }
        if (message.contains("Table") && message.contains("doesn't exist")) {
            return "Table manquante dans la base de données. Exécutez le script de création de schéma.";
        }
        
        return message;
    }
}

