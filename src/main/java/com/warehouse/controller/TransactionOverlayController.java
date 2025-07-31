package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionOverlayController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> mattressComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> storeOwnerComboBox;
    @FXML private TextField notesField;
    @FXML private DatePicker expectedReturnDatePicker;
    @FXML private TextField destinationField;
    @FXML private VBox lendingFieldsBox;
    @FXML private VBox destinationBox;
    @FXML private VBox returnFieldsBox;
    @FXML private ComboBox<String> returnFromComboBox;
    @FXML private Label dialogTitle;
    
    private Transaction transaction;
    private boolean isEditMode = false;
    private DashboardController dashboardController;
    private TransactionsController transactionsController;
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setTransactionsController(TransactionsController transactionsController) {
        this.transactionsController = transactionsController;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        this.isEditMode = (transaction != null);
        
        initializeComboBoxes();
        
        if (isEditMode) {
            dialogTitle.setText("Modifier la transaction");
            typeComboBox.setValue(transaction.getType());
            mattressComboBox.setValue(MattressDAO.getMattressById(transaction.getMattressId()));
            quantityField.setText(String.valueOf(transaction.getQuantity()));
            prixField.setText(String.valueOf(transaction.getPrix()));
            if (transaction.getStoreOwnerId() != null) {
                storeOwnerComboBox.setValue(StoreOwnerDAO.getStoreOwnerById(transaction.getStoreOwnerId()));
            }
            notesField.setText(transaction.getNotes());
            if (transaction.getExpectedReturnDate() != null) {
                expectedReturnDatePicker.setValue(transaction.getExpectedReturnDate());
            }
        } else {
            dialogTitle.setText("Ajouter une transaction");
            typeComboBox.setValue("Vente");
            quantityField.clear();
            prixField.clear();
            notesField.clear();
            expectedReturnDatePicker.setValue(null);
            destinationField.clear();
        }
        
        updateFieldsForType();
    }
    
    private void initializeComboBoxes() {
        // Initialize type combo box with icons
        ObservableList<String> transactionTypes = FXCollections.observableArrayList(
            "💰 Vente",
            "📦 Prêt", 
            "🚚 Transfert",
            "🔄 retour",
            "📥 Réception"
        );
        typeComboBox.setItems(transactionTypes);
        
        // Initialize mattress combo box
        List<Mattress> mattresses = MattressDAO.getAllMattresses();
        ObservableList<String> mattressNames = FXCollections.observableArrayList();
        for (Mattress mattress : mattresses) {
            mattressNames.add("🛏️ " + mattress.getType() + " (" + mattress.getSize() + ")");
        }
        mattressComboBox.setItems(mattressNames);
        
        // Initialize store owner combo box
        List<StoreOwner> storeOwners = StoreOwnerDAO.getAllStoreOwners();
        ObservableList<String> storeOwnerNames = FXCollections.observableArrayList();
        for (StoreOwner owner : storeOwners) {
            storeOwnerNames.add("🏪 " + owner.getName());
        }
        storeOwnerComboBox.setItems(storeOwnerNames);
        returnFromComboBox.setItems(storeOwnerNames);
        
        // Set default selection
        typeComboBox.getSelectionModel().selectFirst();
    }
    
    private void updateFieldsForType() {
        String selectedType = typeComboBox.getValue();
        
        // Show/hide lending fields
        lendingFieldsBox.setVisible("📦 Prêt".equals(selectedType));
        
        // Show/hide destination fields
        destinationBox.setVisible("🚚 Transfert".equals(selectedType));
        
        // Show/hide return fields
        returnFieldsBox.setVisible("🔄 retour".equals(selectedType));
        
        // Enable/disable store owner based on transaction type
        if ("📦 Prêt".equals(selectedType) || "🚚 Transfert".equals(selectedType)) {
            storeOwnerComboBox.setDisable(false);
            storeOwnerComboBox.setPromptText("Sélectionner un propriétaire");
        } else if ("💰 Vente".equals(selectedType)) {
            storeOwnerComboBox.setDisable(true);
            storeOwnerComboBox.setPromptText("Non applicable pour les ventes");
        } else if ("🔄 retour".equals(selectedType)) {
            storeOwnerComboBox.setDisable(true);
            storeOwnerComboBox.setPromptText("Non applicable pour les retours");
        } else if ("📥 Réception".equals(selectedType)) {
            storeOwnerComboBox.setDisable(true);
            storeOwnerComboBox.setPromptText("Non applicable pour les réceptions");
        }
        
        // Set price field behavior
        if ("🔄 retour".equals(selectedType)) {
            prixField.setText("0");
            prixField.setDisable(true);
            prixField.setPromptText("Prix automatiquement mis à zéro");
        } else if ("📥 Réception".equals(selectedType)) {
            prixField.setText("0");
            prixField.setDisable(true);
            prixField.setPromptText("Prix automatiquement mis à zéro");
        } else {
            prixField.setDisable(false);
            prixField.setPromptText("Prix de vente");
        }
    }
    
    @FXML
    private void handleOk() {
        try {
            String type = typeComboBox.getValue();
            Mattress selectedMattress = MattressDAO.getMattressById(Integer.parseInt(mattressComboBox.getValue()));
            String quantityStr = quantityField.getText().trim();
            String prixStr = prixField.getText().trim();
            StoreOwner selectedStoreOwner = StoreOwnerDAO.getStoreOwnerById(Integer.parseInt(storeOwnerComboBox.getValue()));
            String notes = notesField.getText().trim();
            LocalDate expectedReturnDate = expectedReturnDatePicker.getValue();
            String destination = destinationField.getText().trim();
            
            // Validation
            if (type == null) {
                showAlert("Erreur", "Veuillez sélectionner un type de transaction.", AlertType.ERROR);
                return;
            }
            
            if (selectedMattress == null) {
                showAlert("Erreur", "Veuillez sélectionner un matelas.", AlertType.ERROR);
                return;
            }
            
            if (quantityStr.isEmpty()) {
                showAlert("Erreur", "La quantité est obligatoire.", AlertType.ERROR);
                return;
            }
            
            if (prixStr.isEmpty()) {
                showAlert("Erreur", "Le prix est obligatoire.", AlertType.ERROR);
                return;
            }
            
            int quantity;
            double prix;
            try {
                quantity = Integer.parseInt(quantityStr);
                prix = Double.parseDouble(prixStr);
                if (quantity <= 0) {
                    showAlert("Erreur", "La quantité doit être positive.", AlertType.ERROR);
                    return;
                }
                if (prix < 0) {
                    showAlert("Erreur", "Le prix ne peut pas être négatif.", AlertType.ERROR);
                    return;
                }
                
                // Validate price for different transaction types
                if ("💰 Vente".equals(type) && prix == 0) {
                    showAlert("Erreur", "Le prix de vente ne peut pas être zéro.", AlertType.ERROR);
                    return;
                }
                
                // For returns, price should be 0 or the original price
                if ("🔄 retour".equals(type) && prix > 0) {
                    showAlert("Erreur", "Le prix pour un retour doit être zéro.", AlertType.ERROR);
                    return;
                }
                
            } catch (NumberFormatException e) {
                showAlert("Erreur", "La quantité et le prix doivent être des nombres valides.", AlertType.ERROR);
                return;
            }
            
            // Check stock availability for sales, loans, and transfers
            if (("💰 Vente".equals(type) || "📦 Prêt".equals(type) || "🚚 Transfert".equals(type))) {
                if (selectedMattress.getQuantity() < quantity) {
                    showAlert("Erreur", "Stock insuffisant. Disponible: " + selectedMattress.getQuantity(), AlertType.ERROR);
                    return;
                }
                
                // Additional validation for sales
                if ("💰 Vente".equals(type)) {
                    if (prix <= 0) {
                        showAlert("Erreur", "Le prix de vente doit être supérieur à zéro.", AlertType.ERROR);
                        return;
                    }
                    
                    // Check if selling price is reasonable (not too low compared to original price)
                    double originalPrice = selectedMattress.getPrix();
                    if (prix < originalPrice * 0.5) {
                        showAlert("Attention", "Le prix de vente est très bas par rapport au prix original (" + originalPrice + "€). Continuer ?", AlertType.WARNING);
                        // Note: In a real application, you might want to add a confirmation dialog here
                    }
                }
            }
            
            // Validate reception
            if ("📥 Réception".equals(type)) {
                if (prix != 0) {
                    showAlert("Erreur", "Le prix pour une réception doit être zéro.", AlertType.ERROR);
                    return;
                }
            }
            
            // Validate lending requirements
            if ("📦 Prêt".equals(type)) {
                if (selectedStoreOwner == null) {
                    showAlert("Erreur", "Un propriétaire est obligatoire pour un prêt.", AlertType.ERROR);
                    return;
                }
                if (expectedReturnDate == null) {
                    showAlert("Erreur", "Une date de retour est obligatoire pour un prêt.", AlertType.ERROR);
                    return;
                }
                if (expectedReturnDate.isBefore(LocalDate.now())) {
                    showAlert("Erreur", "La date de retour ne peut pas être dans le passé.", AlertType.ERROR);
                    return;
                }
                if (expectedReturnDate.isAfter(LocalDate.now().plusYears(1))) {
                    showAlert("Attention", "La date de retour est très éloignée. Continuer ?", AlertType.WARNING);
                }
            }
            
            // Validate transfer requirements
            if ("🚚 Transfert".equals(type)) {
                if (destination.isEmpty()) {
                    if (dashboardController != null) {
                        dashboardController.showNotification("Une destination est obligatoire pour un transfert.", true);
                    }
                    return;
                }
                if (selectedStoreOwner == null) {
                    if (dashboardController != null) {
                        dashboardController.showNotification("Un propriétaire est obligatoire pour un transfert.", true);
                    }
                    return;
                }
            }
            
            // Validate return requirements
            if ("🔄 retour".equals(type)) {
                if (returnFromComboBox.getValue() == null) {
                    if (dashboardController != null) {
                        dashboardController.showNotification("Un propriétaire est obligatoire pour un retour.", true);
                    }
                    return;
                }
            }
            
            // Get default user ID
            List<User> users = UserDAO.getAllUsers();
            int userId = users.isEmpty() ? -1 : users.get(0).getId();
            if (userId == -1) {
                showAlert("Erreur", "Aucun utilisateur trouvé dans la base de données.", AlertType.ERROR);
                return;
            }
            
            // Prepare notes field
            String finalNotes = notes;
            if ("🚚 Transfert".equals(type) && !destination.isEmpty()) {
                finalNotes = "Transfert vers: " + destination;
            } else if ("🔄 retour".equals(type) && returnFromComboBox.getValue() != null) {
                finalNotes = "Retour de: " + returnFromComboBox.getValue();
            } else if ("📦 Prêt".equals(type) && selectedStoreOwner != null && expectedReturnDate != null) {
                finalNotes = "Prêt à: " + selectedStoreOwner.getName() + " - Retour prévu: " + expectedReturnDate.toString();
            } else if ("💰 Vente".equals(type)) {
                finalNotes = "Vente directe - Prix: " + String.format("%.2f", prix) + "€";
            } else if ("📥 Réception".equals(type)) {
                finalNotes = "Réception de " + quantity + " matelas";
            }
            
            boolean success;
            if (isEditMode) {
                // Store old values for stock adjustment
                String oldType = transaction.getType();
                int oldQuantity = transaction.getQuantity();
                int oldMattressId = transaction.getMattressId();
                
                // Update existing transaction
                transaction.setType(type);
                transaction.setMattressId(selectedMattress.getId());
                transaction.setQuantity(quantity);
                transaction.setPrix(prix);
                transaction.setStoreOwnerId(selectedStoreOwner != null ? selectedStoreOwner.getId() : null);
                transaction.setNotes(finalNotes);
                transaction.setExpectedReturnDate(expectedReturnDate);
                success = TransactionDAO.updateTransaction(transaction);
                
                // Adjust stock based on type changes
                if (success) {
                    // Revert old transaction's stock effect
                    if ("💰 Vente".equals(oldType) || "📦 Prêt".equals(oldType) || "🚚 Transfert".equals(oldType)) {
                        MattressDAO.increaseQuantity(oldMattressId, oldQuantity);
                    } else if ("🔄 retour".equals(oldType)) {
                        MattressDAO.decreaseQuantity(oldMattressId, oldQuantity);
                    }
                    
                    // Apply new transaction's stock effect
                    if ("💰 Vente".equals(type) || "📦 Prêt".equals(type) || "🚚 Transfert".equals(type)) {
                        MattressDAO.decreaseQuantity(selectedMattress.getId(), quantity);
                    } else if ("🔄 retour".equals(type) || "📥 Réception".equals(type)) {
                        MattressDAO.increaseQuantity(selectedMattress.getId(), quantity);
                    }
                }
            } else {
                // Create new transaction
                Transaction newTransaction = new Transaction(
                    LocalDateTime.now(),
                    selectedMattress.getId(),
                    quantity,
                    type,
                    selectedStoreOwner != null ? selectedStoreOwner.getId() : null,
                    userId,
                    prix,
                    finalNotes,
                    expectedReturnDate
                );
                success = TransactionDAO.addTransaction(newTransaction);
                
                // Update mattress quantity
                if (success) {
                    if ("💰 Vente".equals(type) || "📦 Prêt".equals(type) || "🚚 Transfert".equals(type)) {
                        MattressDAO.decreaseQuantity(selectedMattress.getId(), quantity);
                    } else if ("🔄 retour".equals(type) || "📥 Réception".equals(type)) {
                        MattressDAO.increaseQuantity(selectedMattress.getId(), quantity);
                    }
                }
            }
            
            if (success) {
                // Refresh the transactions table
                if (transactionsController != null) {
                    transactionsController.loadTransactions();
                }
                
                // Hide the overlay
                if (dashboardController != null) {
                    dashboardController.hideOverlay();
                }
                
                // Show success notification
                if (dashboardController != null) {
                    dashboardController.showNotification(
                        isEditMode ? "Transaction modifiée avec succès!" : "Transaction ajoutée avec succès!", 
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