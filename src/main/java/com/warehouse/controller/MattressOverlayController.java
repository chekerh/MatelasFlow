package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.util.InputValidator;

public class MattressOverlayController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField customTypeField;
    @FXML private TextField sizeField;
    @FXML private TextField referenceField;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private Label dialogTitle;
    
    private Mattress mattress;
    private boolean isEditMode = false;
    private DashboardController dashboardController;
    private InventoryController inventoryController;
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setInventoryController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }
    
    @FXML
    private void initialize() {
        typeComboBox.getItems().setAll(
            "Mousse",
            "Ressort",
            "Latex",
            "Oreiller",
            "Autre..."
        );
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = "Autre...".equals(newVal);
            customTypeField.setManaged(isOther);
            customTypeField.setVisible(isOther);
            if (!isOther) {
                customTypeField.clear();
            }
        });
        
        // Add input validation for numeric fields
        setupNumericValidation(quantityField, true); // Integer only
        setupNumericValidation(unitPriceField, false); // Decimal allowed
    }
    
    /**
     * Sets up numeric validation for TextField using TextFormatter
     */
    private void setupNumericValidation(TextField field, boolean integerOnly) {
        javafx.scene.control.TextFormatter<String> formatter;
        if (integerOnly) {
            formatter = new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.isEmpty() || newText.matches("\\d+")) {
                    return change;
                }
                return null;
            });
        } else {
            formatter = new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.isEmpty() || newText.matches("\\d*\\.?\\d*")) {
                    return change;
                }
                return null;
            });
        }
        field.setTextFormatter(formatter);
    }

    public void setMattress(Mattress mattress) {
        this.mattress = mattress;
        this.isEditMode = (mattress != null);
        
        if (isEditMode && mattress != null) {
            dialogTitle.setText("Modifier le matelas");
            String type = mattress.getType();
            if (typeComboBox.getItems().contains(type)) {
                typeComboBox.setValue(type);
            } else {
                typeComboBox.setValue("Autre...");
                customTypeField.setManaged(true);
                customTypeField.setVisible(true);
                customTypeField.setText(type);
            }
            sizeField.setText(mattress.getSize());
            referenceField.setText(mattress.getReference());
            quantityField.setText(String.valueOf(mattress.getQuantity()));
            unitPriceField.setText(String.valueOf(mattress.getUnitPrice()));
        } else {
            dialogTitle.setText("Ajouter un matelas");
            typeComboBox.setValue("Mousse");
            customTypeField.clear();
            customTypeField.setVisible(false);
            customTypeField.setManaged(false);
            sizeField.clear();
            referenceField.clear();
            quantityField.clear();
            unitPriceField.clear();
        }
    }
    
    @FXML
    private void handleOk() {
        try {
            String selectedType = typeComboBox.getValue();
            String type = "Autre...".equals(selectedType) ? customTypeField.getText().trim() : selectedType;
            String size = sizeField.getText().trim();
            String reference = referenceField.getText().trim();
            String quantityStr = quantityField.getText().trim();
            String unitPriceStr = unitPriceField.getText().trim();
            
            // Validation
            InputValidator.ValidationResult referenceValidation = InputValidator.validateLength(reference, "La référence", InputValidator.MAX_REFERENCE_LENGTH);
            if (!referenceValidation.isValid()) {
                showAlert("Erreur", referenceValidation.getMessage(), AlertType.ERROR);
                return;
            }
            
            InputValidator.ValidationResult typeValidation = InputValidator.validateLength(type, "Le type", InputValidator.MAX_NAME_LENGTH);
            if (!typeValidation.isValid()) {
                showAlert("Erreur", typeValidation.getMessage(), AlertType.ERROR);
                return;
            }
            
            InputValidator.ValidationResult sizeValidation = InputValidator.validateLength(size, "La taille", InputValidator.MAX_NAME_LENGTH);
            if (!sizeValidation.isValid()) {
                showAlert("Erreur", sizeValidation.getMessage(), AlertType.ERROR);
                return;
            }
            
            if (type == null || type.isEmpty() || size.isEmpty() || reference.isEmpty() || quantityStr.isEmpty() || unitPriceStr.isEmpty()) {
                showAlert("Erreur", "Tous les champs obligatoires doivent être remplis.", AlertType.ERROR);
                return;
            }
            
            int quantity;
            double unitPrice;
            try {
                quantity = Integer.parseInt(quantityStr);
                unitPrice = Double.parseDouble(unitPriceStr);
                if (quantity < 0 || unitPrice < 0) {
                    showAlert("Erreur", "La quantité et le prix unitaire doivent être positifs.", AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "La quantité et le prix unitaire doivent être des nombres valides.", AlertType.ERROR);
                return;
            }
            
            boolean success;
            if (isEditMode) {
                mattress.setType(type);
                mattress.setSize(size);
                mattress.setReference(reference);
                mattress.setQuantity(quantity);
                mattress.setUnitPrice(unitPrice);
                // Note: salePrice is now set per transaction, not per mattress
                success = MattressDAO.updateMattress(mattress);
            } else {
                // Create mattress without sale price (it will be set per transaction)
                Mattress newMattress = new Mattress(type, size, reference, quantity, unitPrice, 0.0);
                success = MattressDAO.addMattress(newMattress);
            }
            
            if (success) {
                // Refresh the inventory table
                if (inventoryController != null) {
                    inventoryController.loadMattresses();
                }
                
                // Hide the overlay
                if (dashboardController != null) {
                    dashboardController.hideOverlay();
                }
                
                // Show success notification
                if (dashboardController != null) {
                    dashboardController.showNotification(
                        isEditMode ? "Matelas modifié avec succès!" : "Matelas ajouté avec succès!", 
                        false
                    );
                }
            } else {
                // Show error notification
                if (dashboardController != null) {
                    dashboardController.showNotification("Échec de l'opération. Veuillez réessayer.", true);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (dashboardController != null) {
                dashboardController.showNotification("Une erreur inattendue s'est produite: " + e.getMessage(), true);
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        if (dashboardController != null) {
            dashboardController.hideOverlay();
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