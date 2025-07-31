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
import java.util.ArrayList;

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
        
        if (isEditMode) {
            dialogTitle.setText("Modifier la transaction");
            
            // Convert type to display format with icon
            String displayType = transaction.getType();
            if ("Vente".equals(displayType)) {
                displayType = "💰 Vente";
            } else if ("Prêt".equals(displayType)) {
                displayType = "📦 Prêt";
            } else if ("Transfert".equals(displayType)) {
                displayType = "🚚 Transfert";
            } else if ("retour".equals(displayType)) {
                displayType = "🔄 retour";
            } else if ("Réception".equals(displayType)) {
                displayType = "📥 Réception";
            }
            
            // Initialize combo boxes first
            initializeComboBoxes();
            
            // Then set values
            typeComboBox.setValue(displayType);
            
            // Set mattress value by finding the matching string
            Mattress mattress = MattressDAO.getMattressById(transaction.getMattressId());
            if (mattress != null) {
                String mattressString = "🛏️ " + mattress.getType() + " (" + mattress.getSize() + ")";
                mattressComboBox.setValue(mattressString);
            }
            
            quantityField.setText(String.valueOf(transaction.getQuantity()));
            prixField.setText(String.valueOf(transaction.getPrix()));
            
            if (transaction.getStoreOwnerId() != null) {
                StoreOwner storeOwner = StoreOwnerDAO.getStoreOwnerById(transaction.getStoreOwnerId());
                if (storeOwner != null) {
                    String storeOwnerString = "🏪 " + storeOwner.getName();
                    storeOwnerComboBox.setValue(storeOwnerString);
                }
            }
            
            notesField.setText(transaction.getNotes());
            if (transaction.getExpectedReturnDate() != null) {
                expectedReturnDatePicker.setValue(transaction.getExpectedReturnDate());
            }
        } else {
            dialogTitle.setText("Ajouter une transaction");
            
            // Initialize combo boxes first
            initializeComboBoxes();
            
            // Then set default values
            typeComboBox.setValue("💰 Vente");
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
            
            // Extract type without icon for database storage
            String typeForDB = type;
            if (type.startsWith("💰 ")) {
                typeForDB = "Vente";
            } else if (type.startsWith("📦 ")) {
                typeForDB = "Prêt";
            } else if (type.startsWith("🚚 ")) {
                typeForDB = "Transfert";
            } else if (type.startsWith("🔄 ")) {
                typeForDB = "retour";
            } else if (type.startsWith("📥 ")) {
                typeForDB = "Réception";
            }
            
            // Extract mattress from string with icon
            String mattressString = mattressComboBox.getValue();
            Mattress selectedMattress = null;
            if (mattressString != null && mattressString.startsWith("🛏️ ")) {
                String mattressInfo = mattressString.substring(2); // Remove icon
                String mattressType = mattressInfo.substring(0, mattressInfo.indexOf(" ("));
                List<Mattress> allMattresses = MattressDAO.getAllMattresses();
                for (Mattress mattress : allMattresses) {
                    if (mattress.getType().equals(mattressType)) {
                        selectedMattress = mattress;
                        break;
                    }
                }
            }
            
            String quantityStr = quantityField.getText().trim();
            String prixStr = prixField.getText().trim();
            
            // Extract store owner from string with icon
            String storeOwnerString = storeOwnerComboBox.getValue();
            StoreOwner selectedStoreOwner = null;
            if (storeOwnerString != null && storeOwnerString.startsWith("🏪 ")) {
                String ownerName = storeOwnerString.substring(2); // Remove icon
                List<StoreOwner> allStoreOwners = StoreOwnerDAO.getAllStoreOwners();
                for (StoreOwner owner : allStoreOwners) {
                    if (owner.getName().equals(ownerName)) {
                        selectedStoreOwner = owner;
                        break;
                    }
                }
            }
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
                if ("Vente".equals(typeForDB) && prix == 0) {
                    showAlert("Erreur", "Le prix de vente ne peut pas être zéro.", AlertType.ERROR);
                    return;
                }
                
                // For returns, price should be 0 or the original price
                if ("retour".equals(typeForDB) && prix > 0) {
                    showAlert("Erreur", "Le prix pour un retour doit être zéro.", AlertType.ERROR);
                    return;
                }
                
            } catch (NumberFormatException e) {
                showAlert("Erreur", "La quantité et le prix doivent être des nombres valides.", AlertType.ERROR);
                return;
            }
            
            // Check stock availability for sales, loans, and transfers
            if (("Vente".equals(typeForDB) || "Prêt".equals(typeForDB) || "Transfert".equals(typeForDB))) {
                if (selectedMattress.getQuantity() < quantity) {
                    showAlert("Erreur", "Stock insuffisant. Disponible: " + selectedMattress.getQuantity(), AlertType.ERROR);
                    return;
                }
                
                // Additional validation for sales
                if ("Vente".equals(typeForDB)) {
                    // Check if selling price is reasonable (not too low compared to original price)
                    double originalPrice = selectedMattress.getPrix();
                    if (prix < originalPrice * 0.5) {
                        showAlert("Attention", "Le prix de vente est très bas par rapport au prix original (" + originalPrice + "€). Continuer ?", AlertType.WARNING);
                        // Note: In a real application, you might want to add a confirmation dialog here
                    }
                }
            }
            
            // Validate reception
            if ("Réception".equals(typeForDB)) {
                if (prix != 0) {
                    showAlert("Erreur", "Le prix pour une réception doit être zéro.", AlertType.ERROR);
                    return;
                }
            }
            
            // Validate lending requirements
            if ("Prêt".equals(typeForDB)) {
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
            if ("Transfert".equals(typeForDB)) {
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
            if ("retour".equals(typeForDB)) {
                if (returnFromComboBox.getValue() == null) {
                    if (dashboardController != null) {
                        dashboardController.showNotification("Veuillez sélectionner le propriétaire de retour.", true);
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
            if ("Transfert".equals(typeForDB) && !destination.isEmpty()) {
                finalNotes = "Transfert vers: " + destination;
            } else if ("retour".equals(typeForDB) && returnFromComboBox.getValue() != null) {
                String returnFromString = returnFromComboBox.getValue();
                if (returnFromString.startsWith("🏪 ")) {
                    String ownerName = returnFromString.substring(2);
                    finalNotes = "Retour de: " + ownerName;
                } else {
                    finalNotes = "Retour de: " + returnFromString;
                }
            } else if ("Prêt".equals(typeForDB) && selectedStoreOwner != null && expectedReturnDate != null) {
                finalNotes = "Prêt à: " + selectedStoreOwner.getName() + " - Retour prévu: " + expectedReturnDate.toString();
            } else if ("Vente".equals(typeForDB)) {
                finalNotes = "Vente directe - Prix: " + String.format("%.2f", prix) + "€";
            } else if ("Réception".equals(typeForDB)) {
                finalNotes = "Réception de " + quantity + " matelas";
            }
            
            boolean success;
            if (isEditMode) {
                // Store old values for stock adjustment
                String oldType = transaction.getType();
                int oldQuantity = transaction.getQuantity();
                int oldMattressId = transaction.getMattressId();
                
                // Update existing transaction
                transaction.setType(typeForDB);
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
                    if ("Vente".equals(oldType) || "Prêt".equals(oldType) || "Transfert".equals(oldType)) {
                        MattressDAO.increaseQuantity(oldMattressId, oldQuantity);
                    } else if ("retour".equals(oldType)) {
                        MattressDAO.decreaseQuantity(oldMattressId, oldQuantity);
                    }
                    
                    // Apply new transaction's stock effect
                    if ("Vente".equals(typeForDB) || "Prêt".equals(typeForDB) || "Transfert".equals(typeForDB)) {
                        MattressDAO.decreaseQuantity(selectedMattress.getId(), quantity);
                    } else if ("retour".equals(typeForDB) || "Réception".equals(typeForDB)) {
                        MattressDAO.increaseQuantity(selectedMattress.getId(), quantity);
                    }
                }
            } else {
                // Create new transaction
                Transaction newTransaction = new Transaction(
                    LocalDateTime.now(),
                    selectedMattress.getId(),
                    quantity,
                    typeForDB,
                    selectedStoreOwner != null ? selectedStoreOwner.getId() : null,
                    userId,
                    prix,
                    finalNotes,
                    expectedReturnDate
                );
                success = TransactionDAO.addTransaction(newTransaction);
                
                // Update mattress quantity
                if (success) {
                    if ("Vente".equals(typeForDB) || "Prêt".equals(typeForDB) || "Transfert".equals(typeForDB)) {
                        MattressDAO.decreaseQuantity(selectedMattress.getId(), quantity);
                    } else if ("retour".equals(typeForDB) || "Réception".equals(typeForDB)) {
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