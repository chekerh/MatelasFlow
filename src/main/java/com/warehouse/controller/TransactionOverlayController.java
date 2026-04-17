package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import com.warehouse.model.PackItem;
import com.warehouse.model.PackItemDAO;
import com.warehouse.util.InputValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TransactionOverlayController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> mattressComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField salePriceField;
    @FXML private TextField totalPriceField;
    @FXML private ComboBox<String> storeOwnerComboBox;
    @FXML private TextField notesField;
    @FXML private DatePicker expectedReturnDatePicker;
    @FXML private TextField destinationField;
    @FXML private VBox lendingFieldsBox;
    @FXML private VBox destinationBox;
    @FXML private VBox returnFieldsBox;
    @FXML private VBox packFieldsBox;
    @FXML private VBox packItemsContainer;
    @FXML private TextField packTotalPriceField;
    @FXML private ComboBox<String> packMattressSelector;
    @FXML private ComboBox<String> returnFromComboBox;
    @FXML private Label dialogTitle;
    @FXML private Label mattressLabel;
    @FXML private Label quantityLabel;
    @FXML private Label salePriceLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label storeOwnerLabel;
    @FXML private Label notesLabel;
    
    // Pack items list
    private java.util.List<PackItemRow> packItemRows = new java.util.ArrayList<>();
    
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
        
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        salePriceField.textProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
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
                String reference = (mattress.getReference() == null || mattress.getReference().trim().isEmpty())
                    ? "Réf inconnue"
                    : mattress.getReference();
                String size = (mattress.getSize() == null || mattress.getSize().trim().isEmpty())
                    ? "Taille inconnue"
                    : mattress.getSize();
                String mattressString = "🛏️ " + mattress.getType() + " - " + size + " - " + reference;
                mattressComboBox.setValue(mattressString);
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
            "📥 Réception",
            "📦 Pack"
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
            String reference = (mattress.getReference() == null || mattress.getReference().trim().isEmpty())
                ? "Réf inconnue"
                : mattress.getReference();
            String size = (mattress.getSize() == null || mattress.getSize().trim().isEmpty())
                ? "Taille inconnue"
                : mattress.getSize();
            mattressNames.add("🛏️ " + mattress.getType() + " - " + size + " - " + reference);
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
        if (selectedType == null) return;
        
        // Reset all fields visibility
        boolean isVente = "💰 Vente".equals(selectedType);
        boolean isPret = "📦 Prêt".equals(selectedType);
        boolean isTransfert = "🚚 Transfert".equals(selectedType);
        boolean isRetour = "🔄 retour".equals(selectedType);
        boolean isReception = "📥 Réception".equals(selectedType);
        boolean isPack = "📦 Pack".equals(selectedType);
        
        // Vente (Sale): Show mattress, quantity, price, total, notes
        // Hide: store owner, return date, destination
        // Pack: Show pack fields, price total, notes
        // Hide: regular mattress, quantity, price per unit
        // Prêt (Loan): Show mattress, quantity, store owner, return date, notes
        // Hide: price fields
        // Transfert: Show mattress, quantity, store owner, destination, notes
        // Hide: price fields, return date
        // Retour: Show mattress, quantity, return from, notes
        // Hide: price fields, store owner (destination)
        // Réception: Show mattress, quantity, notes
        // Hide: price fields, store owner, return date
        
        // Common fields: Type (always visible), Notes (always visible)
        // Show/hide mattress selection based on type
        boolean showRegularMattress = !isPack;
        if (mattressLabel != null) mattressLabel.setVisible(showRegularMattress);
        mattressComboBox.setVisible(showRegularMattress);
        
        // Show quantity for all types except pack (pack has its own quantity management)
        boolean showQuantity = !isPack;
        if (quantityLabel != null) quantityLabel.setVisible(showQuantity);
        quantityField.setVisible(showQuantity);
        
        boolean showPriceFields = (isVente || isPack);
        if (salePriceLabel != null) salePriceLabel.setVisible(showPriceFields && !isPack);
        salePriceField.setVisible(showPriceFields && !isPack);
        if (totalPriceLabel != null) totalPriceLabel.setVisible(showPriceFields && !isPack);
        totalPriceField.setVisible(showPriceFields && !isPack);
        
        boolean showStoreOwner = (isPret || isTransfert);
        if (storeOwnerLabel != null) storeOwnerLabel.setVisible(showStoreOwner);
        storeOwnerComboBox.setVisible(showStoreOwner);
        
        // Show/hide pack fields
        packFieldsBox.setVisible(isPack);
        
        // Show/hide lending fields
        lendingFieldsBox.setVisible(isPret);
        
        // Show/hide destination fields
        destinationBox.setVisible(isTransfert);
        
        // Show/hide return fields
        returnFieldsBox.setVisible(isRetour);
        
        // For pack type, hide regular mattress selection and initialize pack selector
        if (isPack) {
            mattressComboBox.setDisable(true);
            quantityField.setDisable(true);
            salePriceField.setDisable(true);
            packTotalPriceField.setDisable(false);
            
            // Initialize pack mattress selector for automatic product detection
            if (packMattressSelector != null) {
                initializePackMattressSelector();
                packMattressSelector.setDisable(false);
            }
        } else {
            mattressComboBox.setDisable(false);
            quantityField.setDisable(false);
            salePriceField.setDisable(false);
            // Clear pack items when switching away from pack type
            packItemRows.clear();
            packItemsContainer.getChildren().clear();
            if (packMattressSelector != null) {
                packMattressSelector.getItems().clear();
                packMattressSelector.setDisable(true);
            }
        }
        
        // Enable/disable store owner based on transaction type
        if (isPret || isTransfert) {
            storeOwnerComboBox.setDisable(false);
            storeOwnerComboBox.setPromptText("Sélectionner un propriétaire");
        } else {
            storeOwnerComboBox.setDisable(true);
            if (isVente) {
                storeOwnerComboBox.setPromptText("Non applicable pour les ventes");
            } else if (isRetour) {
                storeOwnerComboBox.setPromptText("Non applicable pour les retours");
            } else if (isReception) {
                storeOwnerComboBox.setPromptText("Non applicable pour les réceptions");
            } else if (isPack) {
                storeOwnerComboBox.setPromptText("Non applicable pour les packs");
            }
        }
        
        // Set price field behavior
        if (isRetour || isReception) {
            salePriceField.setText("0");
            salePriceField.setDisable(true);
            salePriceField.setPromptText("Prix automatiquement mis à zéro");
        } else if (!isPack) {
            salePriceField.setDisable(false);
            salePriceField.setPromptText("Prix de vente");
        }
        populatePricesForSelection(mattressComboBox.getValue());
    }
    
    /**
     * Handle pack transaction - creates transaction and pack items
     */
    private void handlePackTransaction(String typeForDB, int userId) {
        try {
            // Validate pack items
            if (packItemRows.isEmpty()) {
                showAlert("Erreur", "Veuillez ajouter au moins un matelas au pack.", AlertType.ERROR);
                return;
            }
            
            // Collect valid pack items
            List<PackItem> packItems = new ArrayList<>();
            for (PackItemRow row : packItemRows) {
                if (!row.isValid()) {
                    showAlert("Erreur", "Veuillez compléter tous les champs du pack (matelas et quantité).", AlertType.ERROR);
                    return;
                }
                packItems.add(new PackItem(0, row.getMattressId(), row.getQuantity()));
            }
            
            if (packItems.isEmpty()) {
                showAlert("Erreur", "Aucun matelas valide dans le pack.", AlertType.ERROR);
                return;
            }
            
            // Get pack total price
            String packPriceStr = packTotalPriceField.getText().trim();
            if (packPriceStr.isEmpty()) {
                showAlert("Erreur", "Veuillez entrer le prix total du pack.", AlertType.ERROR);
                return;
            }
            
            double packPrice;
            try {
                packPrice = Double.parseDouble(packPriceStr);
                if (packPrice <= 0) {
                    showAlert("Erreur", "Le prix du pack doit être positif.", AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le prix du pack doit être un nombre valide.", AlertType.ERROR);
                return;
            }
            
            // Check stock availability for all pack items
            for (PackItem item : packItems) {
                Mattress mattress = MattressDAO.getMattressById(item.getMattressId());
                if (mattress == null) {
                    showAlert("Erreur", "Matelas introuvable dans la base de données.", AlertType.ERROR);
                    return;
                }
                if (mattress.getQuantity() < item.getQuantity()) {
                    showAlert("Erreur", "Stock insuffisant pour " + mattress.getType() + " - " + mattress.getSize() + ". Disponible: " + mattress.getQuantity(), AlertType.ERROR);
                    return;
                }
            }
            
            // Get notes
            String notes = notesField.getText().trim();
            String finalNotes = "Pack - " + packItems.size() + " article(s)";
            if (!notes.isEmpty()) {
                finalNotes += " - " + notes;
            }
            
            // Create transaction using first mattress as placeholder
            int firstMattressId = packItems.get(0).getMattressId();
            int totalQuantity = packItems.stream().mapToInt(PackItem::getQuantity).sum();
            
            // For pack transactions, quantity should be 1 (one pack), not sum of items
            Transaction packTransaction = new Transaction(
                LocalDateTime.now(),
                firstMattressId, // Placeholder mattress ID
                1,               // Quantity is 1 (one pack)
                typeForDB,
                null,            // No store owner for packs
                userId,
                packPrice,       // Total pack price
                finalNotes,
                null             // No return date
            );
            
            // Save transaction and get ID
            int transactionId = TransactionDAO.addTransactionAndGetId(packTransaction);
            if (transactionId <= 0) {
                showAlert("Erreur", "Échec de la création de la transaction.", AlertType.ERROR);
                return;
            }
            
            // Save pack items
            // Note: Database triggers will automatically update inventory for each mattress in the pack
            boolean packItemsSaved = PackItemDAO.addPackItems(transactionId, packItems);
            if (!packItemsSaved) {
                showAlert("Erreur", "Échec de l'enregistrement des articles du pack.", AlertType.ERROR);
                return;
            }
            
            // Success - refresh and close
            if (transactionsController != null) {
                transactionsController.loadTransactions();
            }
            if (inventoryController != null) {
                inventoryController.loadMattresses();
            }
            if (dashboardController != null) {
                dashboardController.hideOverlay();
                dashboardController.showNotification("Pack ajouté avec succès!", false);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (dashboardController != null) {
                dashboardController.showNotification("Une erreur inattendue s'est produite: " + e.getMessage(), true);
            }
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
            } else if (type.startsWith("📦 ") && !"📦 Pack".equals(type)) {
                typeForDB = "Prêt";
            } else if (type.startsWith("🚚 ")) {
                typeForDB = "Transfert";
            } else if (type.startsWith("🔄 ")) {
                typeForDB = "retour";
            } else if (type.startsWith("📥 ")) {
                typeForDB = "Réception";
            } else if ("📦 Pack".equals(type)) {
                typeForDB = "Pack";
            }
            
            // Get default user ID (needed for both pack and regular transactions)
            List<User> users = UserDAO.getAllUsers();
            int userId = users.isEmpty() ? -1 : users.get(0).getId();
            if (userId == -1) {
                showAlert("Erreur", "Aucun utilisateur trouvé dans la base de données.", AlertType.ERROR);
                return;
            }
            
            // Handle Pack transactions specially
            if ("Pack".equals(typeForDB)) {
                handlePackTransaction(typeForDB, userId);
                return;
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
                if ("Vente".equals(typeForDB)) {
                    // Check if selling price is reasonable (not too low compared to unit price)
                    double unitPrice = selectedMattress.getUnitPrice();
                    if (prix < unitPrice * 0.5) {
                        showAlert("Attention", "Le prix de vente est très bas par rapport au prix d'achat (" + String.format("%.2f", unitPrice) + " DT). Voulez-vous continuer ?", AlertType.WARNING);
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
        // No longer populating unit price field since it's removed
        // Sale price field is manually entered by user
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
        
        // Remove emoji if present - emoji can vary in length, so find it and remove
        int emojiIndex = mattressInfo.indexOf("🛏️");
        if (emojiIndex >= 0) {
            // Remove emoji and any following spaces
            mattressInfo = mattressInfo.substring(emojiIndex + 2).trim();
            // Remove any remaining leading spaces
            while (mattressInfo.startsWith(" ")) {
                mattressInfo = mattressInfo.substring(1).trim();
            }
        }
        
        // Format: "Type - Size - Reference"
        // Split by " - " to get the three parts
        String[] parts = mattressInfo.split(" - ");
        
        List<Mattress> allMattresses = MattressDAO.getAllMattresses();
        
        if (parts.length >= 3) {
            // New format with size: "Type - Size - Reference"
            String mattressType = parts[0].trim();
            String sizePart = parts[1].trim();
            String referencePart = parts[2].trim();

            for (Mattress mattress : allMattresses) {
                String reference = mattress.getReference() == null || mattress.getReference().trim().isEmpty() 
                    ? "" 
                    : mattress.getReference().trim();
                String size = mattress.getSize() == null || mattress.getSize().trim().isEmpty() 
                    ? "" 
                    : mattress.getSize().trim();
                String type = mattress.getType() == null ? "" : mattress.getType().trim();
                
                // Match type
                if (!type.equalsIgnoreCase(mattressType)) {
                    continue;
                }
                
                // Match size (handle "Taille inconnue" placeholder)
                boolean sizeMatches = sizePart.isEmpty() || 
                                    size.equalsIgnoreCase(sizePart) || 
                                    ("Taille inconnue".equalsIgnoreCase(sizePart) && size.isEmpty());
                
                // Match reference (handle "Réf inconnue" placeholder)
                boolean referenceMatches = referencePart.isEmpty() || 
                                         reference.equalsIgnoreCase(referencePart) ||
                                         ("Réf inconnue".equalsIgnoreCase(referencePart) && reference.isEmpty());
                
                // If all match, return the mattress
                if (sizeMatches && referenceMatches) {
                    return mattress;
                }
            }
        } else if (parts.length == 2) {
            // Fallback: old format "Type - Reference" (without size)
            String mattressType = parts[0].trim();
            String referencePart = parts[1].trim();
            
            for (Mattress mattress : allMattresses) {
                String reference = mattress.getReference() == null || mattress.getReference().trim().isEmpty() 
                    ? "" 
                    : mattress.getReference().trim();
                String type = mattress.getType() == null ? "" : mattress.getType().trim();
                
                if (type.equalsIgnoreCase(mattressType) && 
                    (reference.equalsIgnoreCase(referencePart) || 
                     ("Réf inconnue".equalsIgnoreCase(referencePart) && reference.isEmpty()))) {
                    return mattress;
                }
            }
        }
        
        // Last resort: try to find by reference only (if it's unique enough)
        if (parts.length >= 3) {
            String referencePart = parts[2].trim();
            if (!referencePart.isEmpty() && !"Réf inconnue".equalsIgnoreCase(referencePart)) {
                for (Mattress mattress : allMattresses) {
                    String reference = mattress.getReference() == null || mattress.getReference().trim().isEmpty() 
                        ? "" 
                        : mattress.getReference().trim();
                    if (reference.equalsIgnoreCase(referencePart)) {
                        return mattress;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Initialize pack mattress selector with all available mattresses
     */
    private void initializePackMattressSelector() {
        if (packMattressSelector == null) return;
        
        List<Mattress> mattresses = MattressDAO.getAllMattresses();
        ObservableList<String> mattressNames = FXCollections.observableArrayList();
        for (Mattress mattress : mattresses) {
            String reference = (mattress.getReference() == null || mattress.getReference().trim().isEmpty())
                ? "Réf inconnue"
                : mattress.getReference();
            String size = (mattress.getSize() == null || mattress.getSize().trim().isEmpty())
                ? "Taille inconnue"
                : mattress.getSize();
            mattressNames.add("🛏️ " + mattress.getType() + " - " + size + " - " + reference);
        }
        packMattressSelector.setItems(mattressNames);
    }
    
    /**
     * Handle when a mattress is selected from pack selector - automatically add to pack
     */
    @FXML
    private void handlePackMattressSelected() {
        if (packMattressSelector == null || packMattressSelector.getValue() == null) {
            return;
        }
        
        String selectedMattress = packMattressSelector.getValue();
        // Automatically add to pack
        addMattressToPack(selectedMattress);
        // Clear selection so user can select again
        packMattressSelector.setValue(null);
    }
    
    /**
     * Quick add button - adds selected mattress from pack selector to pack
     */
    @FXML
    private void handleQuickAddPackItem() {
        if (packMattressSelector == null || packMattressSelector.getValue() == null) {
            showAlert("Attention", "Veuillez sélectionner un matelas d'abord.", AlertType.WARNING);
            return;
        }
        
        String selectedMattress = packMattressSelector.getValue();
        addMattressToPack(selectedMattress);
        packMattressSelector.setValue(null);
    }
    
    /**
     * Add a mattress to the pack (check if already exists, if so increment quantity)
     */
    private void addMattressToPack(String mattressDisplayString) {
        // Resolve mattress from display string
        Mattress mattress = resolveMattressFromDisplay(mattressDisplayString);
        if (mattress == null) {
            showAlert("Erreur", "Impossible de trouver le matelas sélectionné.", AlertType.ERROR);
            return;
        }
        
        // Check if mattress already exists in pack - if so, increment quantity
        for (PackItemRow row : packItemRows) {
            if (row.getMattressId() == mattress.getId()) {
                // Already in pack, increment quantity
                int currentQty = row.getQuantity();
                row.setQuantity(currentQty + 1);
                return;
            }
        }
        
        // Not in pack yet, add new row
        PackItemRow row = new PackItemRow();
        row.setMattress(mattress, mattressDisplayString);
        packItemRows.add(row);
        packItemsContainer.getChildren().add(row.getRoot());
    }
    
    /**
     * Handle adding a new pack item row (manual)
     */
    @FXML
    private void handleAddPackItem() {
        PackItemRow row = new PackItemRow();
        packItemRows.add(row);
        packItemsContainer.getChildren().add(row.getRoot());
    }
    
    /**
     * Remove a pack item row
     */
    private void removePackItem(PackItemRow row) {
        packItemRows.remove(row);
        packItemsContainer.getChildren().remove(row.getRoot());
    }
    
    /**
     * Inner class representing a pack item row in the UI
     */
    private class PackItemRow {
        private HBox root;
        private ComboBox<String> mattressCombo;
        private TextField quantityField;
        private Button removeButton;
        private int mattressId;
        
        public PackItemRow() {
            root = new HBox(8);
            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            root.setStyle("-fx-padding: 4;");
            
            mattressCombo = new ComboBox<>();
            List<Mattress> mattresses = MattressDAO.getAllMattresses();
            ObservableList<String> mattressNames = FXCollections.observableArrayList();
            for (Mattress mattress : mattresses) {
                String reference = (mattress.getReference() == null || mattress.getReference().trim().isEmpty())
                    ? "Réf inconnue"
                    : mattress.getReference();
                String size = (mattress.getSize() == null || mattress.getSize().trim().isEmpty())
                    ? "Taille inconnue"
                    : mattress.getSize();
                mattressNames.add("🛏️ " + mattress.getType() + " - " + size + " - " + reference);
            }
            mattressCombo.setItems(mattressNames);
            mattressCombo.setPrefWidth(350);
            mattressCombo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(mattressCombo, javafx.scene.layout.Priority.ALWAYS);
            
            mattressCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    Mattress mattress = resolveMattressFromDisplay(newVal);
                    mattressId = (mattress != null) ? mattress.getId() : 0;
                }
            });
            
            quantityField = new TextField();
            quantityField.setPromptText("Qté");
            quantityField.setPrefWidth(60);
            setupNumericValidation(quantityField, true);
            
            removeButton = new Button("✖");
            removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            removeButton.setOnAction(e -> removePackItem(this));
            
            root.getChildren().addAll(mattressCombo, quantityField, removeButton);
        }
        
        public HBox getRoot() { return root; }
        public int getMattressId() { return mattressId; }
        public int getQuantity() {
            try {
                String qty = quantityField.getText().trim();
                return qty.isEmpty() ? 1 : Integer.parseInt(qty);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        
        public void setQuantity(int quantity) {
            quantityField.setText(String.valueOf(quantity));
        }
        
        public void setMattress(Mattress mattress, String displayString) {
            mattressId = mattress.getId();
            mattressCombo.setValue(displayString);
            quantityField.setText("1");
        }
        
        public boolean isValid() {
            return mattressId > 0 && getQuantity() > 0;
        }
    }
} 