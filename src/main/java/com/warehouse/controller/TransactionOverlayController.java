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
import com.warehouse.util.InputValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionOverlayController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> mattressComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private TextField salePriceField;
    @FXML private TextField totalPriceField;
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

        // Add input validation for numeric fields
        setupNumericValidation(quantityField, true); // Integer only
        setupNumericValidation(salePriceField, false); // Decimal allowed
        setupNumericValidation(unitPriceField, false); // Decimal allowed (though read-only)
        
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        salePriceField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        mattressComboBox.valueProperty().addListener((obs, oldVal, newVal) -> populatePricesForSelection(newVal));
        unitPriceField.setEditable(false);
        totalPriceField.setEditable(false);
    }
    
    /**
     * Sets up numeric validation for TextField using TextFormatter
     * @param field The TextField to validate
     * @param integerOnly If true, only integers allowed; if false, decimals allowed
     */
    private void setupNumericValidation(TextField field, boolean integerOnly) {
        javafx.scene.control.TextFormatter<String> formatter;
        if (integerOnly) {
            // Integer only: allow digits, no decimals
            formatter = new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.isEmpty() || newText.matches("\\d+")) {
                    return change;
                }
                return null;
            });
        } else {
            // Decimal: allow digits and one decimal point
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
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        this.isEditMode = (transaction != null);
        
        if (isEditMode) {
            dialogTitle.setText("Modifier la transaction");
            
            // Convert type to display format with icon
            String displayType = transaction != null ? transaction.getType() : null;
            if (displayType != null && "Vente".equals(displayType)) {
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
            Mattress mattress = (transaction != null) ? MattressDAO.getMattressById(transaction.getMattressId()) : null;
            if (mattress != null) {
                String reference = (mattress.getReference() == null || mattress.getReference().isBlank())
                    ? "Réf inconnue"
                    : mattress.getReference();
                String mattressString = "🛏️ " + mattress.getType() + " - " + reference;
                mattressComboBox.setValue(mattressString);
                populatePricesForSelection(mattressString);
            } else {
                unitPriceField.clear();
            }
            
            if (transaction != null) {
                quantityField.setText(String.valueOf(transaction.getQuantity()));
                salePriceField.setText(String.valueOf(transaction.getPrix()));
                updateTotalPrice();
                
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
            }
        } else {
            dialogTitle.setText("Ajouter une transaction");
            
            // Initialize combo boxes first
            initializeComboBoxes();
            
            // Then set default values
            typeComboBox.setValue("💰 Vente");
            quantityField.clear();
            unitPriceField.clear();
            salePriceField.clear();
            totalPriceField.clear();
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
            String reference = (mattress.getReference() == null || mattress.getReference().isBlank())
                ? "Réf inconnue"
                : mattress.getReference();
            mattressNames.add("🛏️ " + mattress.getType() + " - " + reference);
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
        if ("🔄 retour".equals(selectedType) || "📥 Réception".equals(selectedType)) {
            salePriceField.setText("0");
            salePriceField.setDisable(true);
            salePriceField.setPromptText("Prix automatiquement mis à zéro");
        } else {
            salePriceField.setDisable(false);
            salePriceField.setPromptText("Prix de vente");
        }
        populatePricesForSelection(mattressComboBox.getValue());
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
            
            String mattressString = mattressComboBox.getValue();
            Mattress selectedMattress = resolveMattressFromDisplay(mattressString);
            if (selectedMattress == null) {
                showAlert("Erreur", "Matelas non trouvé. Veuillez réessayer la sélection.", AlertType.ERROR);
                return;
            }
            
            String quantityStr = quantityField.getText().trim();
            String salePriceStr = salePriceField.getText().trim();
            
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
            
            // Validate field lengths
            InputValidator.ValidationResult notesValidation = InputValidator.validateLength(notes, "Les notes", InputValidator.MAX_NOTES_LENGTH);
            if (!notesValidation.isValid()) {
                showAlert("Erreur", notesValidation.getMessage(), AlertType.ERROR);
                return;
            }
            
            if (!destination.isEmpty()) {
                InputValidator.ValidationResult destValidation = InputValidator.validateLength(destination, "La destination", InputValidator.MAX_NAME_LENGTH);
                if (!destValidation.isValid()) {
                    showAlert("Erreur", destValidation.getMessage(), AlertType.ERROR);
                    return;
                }
            }
            
            if (quantityStr.isEmpty() || salePriceStr.isEmpty()) {
                showAlert("Erreur", "La quantité et le prix sont obligatoires.", AlertType.ERROR);
                return;
            }
            
            int quantity;
            double prix;
            try {
                quantity = Integer.parseInt(quantityStr);
                prix = Double.parseDouble(salePriceStr);
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
                // Note: Price validation removed - price is now variable per transaction and client
                // Each transaction can have its own price based on client agreement and market conditions
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
                finalNotes = "Vente directe - Prix: " + String.format("%.2f", prix) + " DT";
            } else if ("Réception".equals(typeForDB)) {
                finalNotes = "Réception de " + quantity + " matelas";
            }
            
            boolean success;
            if (isEditMode) {
                // Update existing transaction
                // Note: Inventory adjustment is now handled automatically by TransactionDAO.updateTransaction()
                transaction.setType(typeForDB);
                transaction.setMattressId(selectedMattress.getId());
                transaction.setQuantity(quantity);
            transaction.setPrix(prix);
                transaction.setStoreOwnerId(selectedStoreOwner != null ? selectedStoreOwner.getId() : null);
                transaction.setNotes(finalNotes);
                transaction.setExpectedReturnDate(expectedReturnDate);
                success = TransactionDAO.updateTransaction(transaction);
                // Note: Inventory is now automatically updated by TransactionDAO.updateTransaction()
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
                // Note: Inventory is now automatically updated by TransactionDAO.addTransaction()
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

    private void populatePricesForSelection(String displayValue) {
        Mattress mattress = resolveMattressFromDisplay(displayValue);
        if (mattress != null) {
            unitPriceField.setText(String.format("%.2f", mattress.getUnitPrice()));
            // Sale price is now set per transaction, not auto-filled from mattress
            // User must enter the price for each transaction (variable per client/time)
            if (!salePriceField.isDisabled()) {
                salePriceField.clear();
                salePriceField.setPromptText("Entrer le prix de vente pour cette transaction");
            }
        } else {
            unitPriceField.clear();
            if (!salePriceField.isDisabled()) {
                salePriceField.clear();
            }
        }
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        try {
            double salePrice = Double.parseDouble(salePriceField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            totalPriceField.setText(String.format("%.2f", salePrice * quantity));
        } catch (NumberFormatException e) {
            totalPriceField.setText("");
        }
    }

    private Mattress resolveMattressFromDisplay(String mattressString) {
        if (mattressString == null || mattressString.trim().isEmpty()) {
            return null;
        }
        String mattressInfo = mattressString.trim();
        if (mattressInfo.contains(" ")) {
            int firstSpace = mattressInfo.indexOf(" ");
            mattressInfo = mattressInfo.substring(firstSpace + 1).trim();
        }
        String[] parts = mattressInfo.split(" - ");
        String mattressType = parts[0].trim();
        String referencePart = parts.length > 1 ? parts[1].trim() : "";

        List<Mattress> allMattresses = MattressDAO.getAllMattresses();
        for (Mattress mattress : allMattresses) {
            String reference = mattress.getReference() == null ? "" : mattress.getReference().trim();
            if (mattress.getType().equalsIgnoreCase(mattressType) &&
                (referencePart.isEmpty() || reference.equalsIgnoreCase(referencePart) ||
                 ("Réf inconnue".equals(referencePart) && reference.isEmpty()))) {
                return mattress;
            }
        }
        return null;
    }
} 