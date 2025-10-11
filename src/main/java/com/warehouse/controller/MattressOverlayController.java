package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;

public class MattressOverlayController {
    @FXML private TextField typeField;
    @FXML private TextField sizeField;
    @FXML private TextField brandField;
    @FXML private TextField quantityField;
    @FXML private TextField prixField;
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
    
    public void setMattress(Mattress mattress) {
        this.mattress = mattress;
        this.isEditMode = (mattress != null);
        
        if (isEditMode) {
            dialogTitle.setText("Modifier le matelas");
            typeField.setText(mattress.getType());
            sizeField.setText(mattress.getSize());
            brandField.setText(mattress.getBrand());
            quantityField.setText(String.valueOf(mattress.getQuantity()));
            prixField.setText(String.valueOf(mattress.getPrix()));
        } else {
            dialogTitle.setText("Ajouter un matelas");
            typeField.clear();
            sizeField.clear();
            brandField.clear();
            quantityField.clear();
            prixField.clear();
        }
    }
    
    @FXML
    private void handleOk() {
        try {
            String type = typeField.getText().trim();
            String size = sizeField.getText().trim();
            String brand = brandField.getText().trim();
            String quantityStr = quantityField.getText().trim();
            String prixStr = prixField.getText().trim();
            
            // Validation
            if (type.isEmpty() || size.isEmpty() || quantityStr.isEmpty() || prixStr.isEmpty()) {
                showAlert("Erreur", "Tous les champs obligatoires doivent être remplis.", AlertType.ERROR);
                return;
            }
            
            int quantity;
            double prix;
            try {
                quantity = Integer.parseInt(quantityStr);
                prix = Double.parseDouble(prixStr);
                if (quantity < 0 || prix < 0) {
                    showAlert("Erreur", "La quantité et le prix doivent être positifs.", AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "La quantité et le prix doivent être des nombres valides.", AlertType.ERROR);
                return;
            }
            
            boolean success;
            if (isEditMode) {
                System.out.println("DEBUG: Modification matelas ID: " + mattress.getId());
                mattress.setType(type);
                mattress.setSize(size);
                mattress.setBrand(brand);
                mattress.setQuantity(quantity);
                mattress.setPrix(prix);
                success = MattressDAO.updateMattress(mattress);
                System.out.println("DEBUG: Modification " + (success ? "réussie" : "échouée"));
            } else {
                System.out.println("DEBUG: Ajout matelas - Type: " + type + ", Taille: " + size + ", Quantité: " + quantity);
                Mattress newMattress = new Mattress(type, size, brand, quantity, prix);
                success = MattressDAO.addMattress(newMattress);
                System.out.println("DEBUG: Ajout " + (success ? "réussi" : "échoué"));
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