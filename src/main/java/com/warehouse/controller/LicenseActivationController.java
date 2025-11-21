package com.warehouse.controller;

import com.warehouse.security.LicenseManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for license activation dialog
 */
public class LicenseActivationController {
    private static final Logger logger = LoggerFactory.getLogger(LicenseActivationController.class);
    
    @FXML
    private TextField licenseKeyField;
    
    @FXML
    private Button activateButton;
    
    @FXML
    private Label statusLabel;
    
    private Stage stage;
    
    @FXML
    public void initialize() {
        // Auto-format license key input (add hyphens)
        licenseKeyField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Remove all hyphens and spaces for processing
            String clean = newVal.replaceAll("[-\\s]", "").toUpperCase();
            
            // Limit to 20 characters (5 groups of 4)
            if (clean.length() > 20) {
                clean = clean.substring(0, 20);
            }
            
            // Format with hyphens
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append("-");
                }
                formatted.append(clean.charAt(i));
            }
            
            // Only update if the formatted version is different
            if (!formatted.toString().equals(newVal)) {
                licenseKeyField.setText(formatted.toString());
            }
            
            // Enable/disable activate button based on length
            activateButton.setDisable(clean.length() != 20);
        });
        
        // Allow Enter key to activate
        licenseKeyField.setOnAction(e -> handleActivate());
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    private void handleActivate() {
        String licenseKey = licenseKeyField.getText().trim();
        
        if (licenseKey.isEmpty()) {
            showError("Veuillez entrer une clé d'activation");
            return;
        }
        
        // Validate license key
        if (!LicenseManager.validateLicenseKey(licenseKey)) {
            showError("Clé d'activation invalide. Veuillez vérifier et réessayer.");
            statusLabel.setText("❌ Clé invalide");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        
        // Save license
        if (LicenseManager.saveLicense(licenseKey)) {
            showSuccess("Licence activée avec succès!");
            statusLabel.setText("✅ Licence activée");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            
            // Close activation window and show login
            Platform.runLater(() -> {
                try {
                    stage.close();
                    loadLoginView();
                } catch (Exception e) {
                    logger.error("Error loading login view: {}", e.getMessage(), e);
                }
            });
        } else {
            showError("Erreur lors de l'enregistrement de la licence. Veuillez réessayer.");
            statusLabel.setText("❌ Erreur d'enregistrement");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
    
    private void loadLoginView() throws Exception {
        Stage loginStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        loginStage.setScene(scene);
        loginStage.setTitle("MatelasPro - Gestion d'Entrepôt STE Habiba");
        loginStage.show();
        
        // Set icon
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/SuperMousse.jpg"));
            loginStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.debug("Could not load icon: {}", e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur d'activation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Activation réussie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

