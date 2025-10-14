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
    private InventoryController inventoryController;
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setTransactionsController(TransactionsController transactionsController) {
        this.transactionsController = transactionsController;
    }
    
    public void setInventoryController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }
    
    @FXML
    public void initialize() {
        // Initialize ComboBoxes when FXML loads
        initializeComboBoxes();
        
        // Add listener to type ComboBox to update fields dynamically
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldsForType();
        });
        
        // Add listener to mattress ComboBox to update return list when mattress changes
        mattressComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("🔄 retour".equals(typeComboBox.getValue())) {
                updateReturnFromList();
            }
        });
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
        
        // CRITICAL FIX: Force text display with cell factories
        typeComboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setGraphic(null);
            }
        });
        
        typeComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(null);
                }
                setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
            }
        });
        
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
        
        System.out.println("🔄 Type changé: " + selectedType);
        
        // Show/hide lending fields
        lendingFieldsBox.setVisible("📦 Prêt".equals(selectedType));
        lendingFieldsBox.setManaged("📦 Prêt".equals(selectedType));
        
        // Show/hide destination fields
        destinationBox.setVisible("🚚 Transfert".equals(selectedType));
        destinationBox.setManaged("🚚 Transfert".equals(selectedType));
        
        // Show/hide return fields
        returnFieldsBox.setVisible("🔄 retour".equals(selectedType));
        returnFieldsBox.setManaged("🔄 retour".equals(selectedType));
        
        // Update return list if return is selected
        if ("🔄 retour".equals(selectedType)) {
            updateReturnFromList();
        }
        
        // Enable/disable store owner based on transaction type
        if ("📦 Prêt".equals(selectedType) || "🚚 Transfert".equals(selectedType)) {
            storeOwnerComboBox.setDisable(false);
            storeOwnerComboBox.setPromptText("Sélectionner un propriétaire");
        } else if ("💰 Vente".equals(selectedType)) {
            storeOwnerComboBox.setDisable(true);
            storeOwnerComboBox.setPromptText("Non applicable pour les ventes");
            storeOwnerComboBox.setValue(null);
        } else if ("🔄 retour".equals(selectedType)) {
            storeOwnerComboBox.setDisable(true);
            storeOwnerComboBox.setPromptText("Utilisez 'Retour de' ci-dessous");
            storeOwnerComboBox.setValue(null);
        } else if ("📥 Réception".equals(selectedType)) {
            storeOwnerComboBox.setDisable(true);
            storeOwnerComboBox.setPromptText("Non applicable pour les réceptions");
            storeOwnerComboBox.setValue(null);
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
            prixField.setPromptText("Prix de vente (DT)");
        }
    }
    
    /**
     * Update the returnFromComboBox with store owners who have active loans for the selected mattress
     */
    private void updateReturnFromList() {
        System.out.println("📋 Mise à jour de la liste des retours...");
        
        // Get selected mattress
        String mattressString = mattressComboBox.getValue();
        if (mattressString == null || mattressString.trim().isEmpty()) {
            System.out.println("⚠️ Aucun matelas sélectionné");
            returnFromComboBox.setItems(FXCollections.observableArrayList());
            returnFromComboBox.setPromptText("Sélectionnez d'abord un matelas");
            return;
        }
        
        // Extract mattress info
        Mattress selectedMattress = null;
        String mattressInfo = mattressString.trim();
        
        // Remove emoji
        if (mattressInfo.contains(" ")) {
            int firstSpace = mattressInfo.indexOf(" ");
            mattressInfo = mattressInfo.substring(firstSpace + 1).trim();
        }
        
        // Extract type (before opening parenthesis)
        int openParen = mattressInfo.indexOf(" (");
        if (openParen > 0) {
            String mattressType = mattressInfo.substring(0, openParen).trim();
            List<Mattress> allMattresses = MattressDAO.getAllMattresses();
            for (Mattress mattress : allMattresses) {
                if (mattress.getType().equalsIgnoreCase(mattressType)) {
                    selectedMattress = mattress;
                    break;
                }
            }
        }
        
        if (selectedMattress == null) {
            System.out.println("❌ Matelas non trouvé");
            returnFromComboBox.setItems(FXCollections.observableArrayList());
            returnFromComboBox.setPromptText("Matelas invalide");
            return;
        }
        
        // Get store owners with active loans for this mattress
        List<Integer> storeOwnerIds = TransactionDAO.getStoreOwnersWithActiveLoans(selectedMattress.getId());
        System.out.println("📊 Nombre de propriétaires avec prêts actifs: " + storeOwnerIds.size());
        
        if (storeOwnerIds.isEmpty()) {
            // No active loans for this mattress
            ObservableList<String> emptyList = FXCollections.observableArrayList();
            emptyList.add("⚠️ Aucun prêt actif pour ce matelas");
            returnFromComboBox.setItems(emptyList);
            returnFromComboBox.setPromptText("Aucun prêt actif");
            returnFromComboBox.setDisable(true);
            System.out.println("⚠️ Aucun prêt actif trouvé pour ce matelas");
        } else {
            // Get store owner names
            ObservableList<String> returnFromList = FXCollections.observableArrayList();
            for (Integer ownerId : storeOwnerIds) {
                StoreOwner owner = StoreOwnerDAO.getStoreOwnerById(ownerId);
                if (owner != null) {
                    returnFromList.add("🏪 " + owner.getName());
                    System.out.println("✅ Ajouté: " + owner.getName());
                }
            }
            returnFromComboBox.setItems(returnFromList);
            returnFromComboBox.setPromptText("Retour de...");
            returnFromComboBox.setDisable(false);
            System.out.println("✅ Liste mise à jour avec " + returnFromList.size() + " propriétaires");
        }
    }
    
    @FXML
    private void handleOk() {
        System.out.println("========================");
        System.out.println("DEBUG: handleOk() CALLED");
        System.out.println("========================");
        try {
            // Validation 1: Check type selected
            String type = typeComboBox.getValue();
            if (type == null || type.trim().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un type de transaction.", AlertType.ERROR);
                return;
            }
            
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
            
            // Validation 2: Check mattress selected
            String mattressString = mattressComboBox.getValue();
            System.out.println("DEBUG: Mattress ComboBox Value = '" + mattressString + "'"); // Debug
            
            Mattress selectedMattress = null;
            if (mattressString == null || mattressString.trim().isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner un matelas.", AlertType.ERROR);
                return;
            }
            
            // Extract mattress from string with icon - ROBUST emoji handling
            String mattressInfo = mattressString.trim();
            
            // Remove emoji by finding the first space after emoji
            if (mattressInfo.contains(" ")) {
                int firstSpace = mattressInfo.indexOf(" ");
                mattressInfo = mattressInfo.substring(firstSpace + 1).trim();
            }
            
            System.out.println("DEBUG: After emoji removal = '" + mattressInfo + "'"); // Debug
            
            // Extract type (before opening parenthesis)
            int openParen = mattressInfo.indexOf(" (");
            if (openParen > 0) {
                String mattressType = mattressInfo.substring(0, openParen).trim();
                System.out.println("DEBUG: Looking for mattress type = '" + mattressType + "'"); // Debug
                
                List<Mattress> allMattresses = MattressDAO.getAllMattresses();
                for (Mattress mattress : allMattresses) {
                    System.out.println("DEBUG: Comparing '" + mattressType + "' with '" + mattress.getType() + "'"); // Debug
                    // Case-insensitive comparison
                    if (mattress.getType().equalsIgnoreCase(mattressType)) {
                        selectedMattress = mattress;
                        System.out.println("DEBUG: MATCH FOUND! ID=" + mattress.getId());
                        break;
                    }
                }
            }
            
            if (selectedMattress == null) {
                showAlert("Erreur", "Matelas non trouvé. Veuillez réessayer la sélection.", AlertType.ERROR);
                return;
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
                        showAlert("Attention", "Le prix de vente est très bas par rapport au prix original (" + String.format("%.2f", originalPrice) + " DT). Voulez-vous continuer ?", AlertType.WARNING);
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
                String returnFromValue = returnFromComboBox.getValue();
                if (returnFromValue == null || returnFromValue.isEmpty()) {
                    showAlert("Erreur", "Veuillez sélectionner le propriétaire de retour.", AlertType.ERROR);
                    return;
                }
                // Check if it's a warning message (no active loans)
                if (returnFromValue.startsWith("⚠️")) {
                    showAlert("Erreur", "Aucun prêt actif pour ce matelas. Impossible de créer un retour.", AlertType.ERROR);
                    return;
                }
                // Extract store owner from return selection
                if (returnFromValue.startsWith("🏪 ")) {
                    String ownerName = returnFromValue.substring(2);
                    List<StoreOwner> allStoreOwners = StoreOwnerDAO.getAllStoreOwners();
                    for (StoreOwner owner : allStoreOwners) {
                        if (owner.getName().equals(ownerName)) {
                            selectedStoreOwner = owner;
                            break;
                        }
                    }
                }
                if (selectedStoreOwner == null) {
                    showAlert("Erreur", "Propriétaire de retour invalide.", AlertType.ERROR);
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
                finalNotes = "Transfert vers: " + destination + (notes.isEmpty() ? "" : " | Notes: " + notes);
            } else if ("retour".equals(typeForDB) && selectedStoreOwner != null) {
                finalNotes = "Retour de: " + selectedStoreOwner.getName() + " | Quantité: " + quantity + (notes.isEmpty() ? "" : " | Notes: " + notes);
                System.out.println("📝 Notes de retour: " + finalNotes);
            } else if ("Prêt".equals(typeForDB) && selectedStoreOwner != null && expectedReturnDate != null) {
                finalNotes = "Prêt à: " + selectedStoreOwner.getName() + " | Retour prévu: " + expectedReturnDate.toString() + (notes.isEmpty() ? "" : " | Notes: " + notes);
            } else if ("Vente".equals(typeForDB)) {
                finalNotes = "Vente directe | Prix: " + String.format("%.2f DT", prix) + (notes.isEmpty() ? "" : " | Notes: " + notes);
            } else if ("Réception".equals(typeForDB)) {
                finalNotes = "Réception de " + quantity + " matelas" + (notes.isEmpty() ? "" : " | Notes: " + notes);
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
                
                // Note: For edit mode, we need to manually adjust stock since database trigger
                // only fires on INSERT, not UPDATE. We must revert the old effect and apply new effect.
                if (success) {
                    // Revert old transaction's stock effect
                    if ("Vente".equals(oldType) || "Prêt".equals(oldType) || "Transfert".equals(oldType)) {
                        MattressDAO.increaseQuantity(oldMattressId, oldQuantity);
                    } else if ("retour".equals(oldType) || "Réception".equals(oldType)) {
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
                
                // Note: Inventory is automatically updated by database trigger
                // No manual stock adjustment needed here
            }
            
            if (success) {
                // Refresh the transactions table
                if (transactionsController != null) {
                    transactionsController.loadTransactions();
                }
                
                // CRITICAL: Refresh inventory to show updated quantities
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