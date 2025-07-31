package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;

public class StoreOwnerOverlayController {
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private Label dialogTitle;
    
    private StoreOwner storeOwner;
    private boolean isEditMode = false;
    private DashboardController dashboardController;
    private StoreOwnersController storeOwnersController;
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setStoreOwnersController(StoreOwnersController storeOwnersController) {
        this.storeOwnersController = storeOwnersController;
    }
    
    public void setStoreOwner(StoreOwner storeOwner) {
        this.storeOwner = storeOwner;
        this.isEditMode = (storeOwner != null);
        
        if (isEditMode) {
            dialogTitle.setText("Modifier le propriétaire");
            nameField.setText(storeOwner.getName());
            contactField.setText(storeOwner.getContact());
        } else {
            dialogTitle.setText("Ajouter un propriétaire");
            nameField.clear();
            contactField.clear();
        }
    }
    
    @FXML
    private void handleOk() {
        try {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            
            // Validation
            if (name.isEmpty()) {
                showAlert("Erreur", "Le nom du magasin est obligatoire.", AlertType.ERROR);
                return;
            }
            
            boolean success;
            if (isEditMode) {
                storeOwner.setName(name);
                storeOwner.setContact(contact);
                success = StoreOwnerDAO.updateStoreOwner(storeOwner);
            } else {
                StoreOwner newStoreOwner = new StoreOwner(name, contact);
                success = StoreOwnerDAO.addStoreOwner(newStoreOwner);
            }
            
            if (success) {
                // Refresh the store owners table
                if (storeOwnersController != null) {
                    storeOwnersController.loadStoreOwners();
                }
                
                // Hide the overlay
                if (dashboardController != null) {
                    dashboardController.hideOverlay();
                }
                
                // Show success notification
                if (dashboardController != null) {
                    dashboardController.showNotification(
                        isEditMode ? "Propriétaire modifié avec succès!" : "Propriétaire ajouté avec succès!", 
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