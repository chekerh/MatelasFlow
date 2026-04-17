package com.warehouse.controller;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.PackItem;
import com.warehouse.model.PackItemDAO;
import com.warehouse.ui.SkeletonPane;
import com.warehouse.ui.IconFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class TransactionsController {
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> mattressNameColumn;
    @FXML private TableColumn<Transaction, Integer> quantityColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> prixColumn;
    @FXML private TableColumn<Transaction, String> storeOwnerNameColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;
    @FXML private TableColumn<Transaction, String> expectedReturnDateColumn;
    @FXML private TableColumn<Transaction, String> totalPriceColumn;
    @FXML private TableColumn<Transaction, Void> addMattressColumn;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button pdfReportButton;
    @FXML private Button deleteButton;
    @FXML private Label errorLabel;
    @FXML private DatePicker reportDatePicker;
    @FXML private DatePicker startDatePicker; // Optional - may not exist in FXML
    @FXML private DatePicker endDatePicker; // Optional - may not exist in FXML
    @FXML private Button generatePdfRangeButton; // Optional - may not exist in FXML
    @FXML private Button exportCsvButton; // Optional - may not exist in FXML
    @FXML private TextField searchField; // Optional - may not exist in FXML
    @FXML private ComboBox<String> sortOrderComboBox; // Optional - may not exist in FXML

    @FXML private ComboBox<String> viewModeComboBox;
    @FXML private HBox quickRangeButtonsContainer; // Optional - may not exist in FXML
    @FXML private ToggleButton groupByDayToggle; // Optional - may not exist in FXML

    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private boolean isGroupedView = false;
    private FilteredList<Transaction> filteredTransactions;
    private DashboardController dashboardController;
    private Transaction draggedTransaction;
    private final SkeletonPane tableSkeleton = SkeletonPane.forTable(560, 260);
    private final Label emptyPlaceholder = new Label("Aucune transaction enregistrée.");

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        // Use variable row height for pack transactions (they need more space)
        transactionTable.setFixedCellSize(-1); // -1 means variable row height
        // Use a constrained resize policy. Some JavaFX versions (e.g., 8/11)
        // don't have CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN, which would
        // crash controller initialization and prevent the Transactions UI from opening.
        try {
            // Prefer FLEX_LAST_COLUMN when available (newer JavaFX).
            transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        } catch (NoSuchFieldError | Exception ignored) {
            // Fallback for older JavaFX versions.
            transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
        mattressNameColumn.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            if (t == null) {
                return new javafx.beans.property.SimpleStringProperty("Inconnu");
            }
            String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
            if (type.startsWith("pack")) {
                // For packs, show all items with size - reference format
                List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                if (packItems != null && !packItems.isEmpty()) {
                    StringBuilder packDisplay = new StringBuilder();
                    for (int i = 0; i < packItems.size(); i++) {
                        PackItem item = packItems.get(i);
                        if (i > 0) packDisplay.append("\n");
                        packDisplay.append(item.getQuantity()).append("x ");
                        if (item.getMattressName() != null) {
                            packDisplay.append(item.getMattressName());
                        } else {
                            packDisplay.append("Matelas");
                        }
                    }
                    return new javafx.beans.property.SimpleStringProperty(packDisplay.toString());
                }
                return new javafx.beans.property.SimpleStringProperty("Pack");
            }
            // For regular transactions, mattress_name already contains "size - reference" format from SQL
            return new javafx.beans.property.SimpleStringProperty(
                t.getMattressName() != null ? t.getMattressName() : "Inconnu"
            );
        });
        
        // Set cell factory for mattress column to handle multi-line text and centering
        mattressNameColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Center the text but not too much
                    setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    // Allow text wrapping for pack items
                    setWrapText(true);
                    // Add padding for better display
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                String lower = item.toLowerCase(Locale.ROOT).trim();
                if (lower.startsWith("vente")) {
                    setText("💰 Vente");
                    setStyle("-fx-text-fill: #16a085; -fx-font-weight: bold;");
                } else if (lower.startsWith("prêt") || lower.startsWith("pret") || lower.contains("pret")) {
                    setText("📦 Prêt");
                    setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                } else if (lower.startsWith("transfert") || lower.contains("transfert")) {
                    setText("🚚 Transfert");
                    setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold;");
                } else if (lower.startsWith("retour") || lower.contains("retour")) {
                    setText("🔄 Retour");
                    setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold;");
                } else if (lower.startsWith("réception") || lower.startsWith("reception") || lower.contains("reception")) {
                    setText("📥 Réception");
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else if (lower.startsWith("pack")) {
                    setText("📦 Pack");
                    setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold;");
                } else {
                    setText(item);
                    setStyle("");
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        prixColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrix()).asObject());
        
        // Format prix column to show 2 decimals with DT currency
        prixColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    if (t != null && t.getType() != null && t.getType().toLowerCase(Locale.ROOT).startsWith("pack")) {
                        // For packs, show the total pack price (not per unit)
                        setText(String.format("%.2f DT", item));
                    } else {
                        // For regular transactions, show unit price
                        setText(String.format("%.2f DT", item));
                    }
                }
            }
        });
        
        storeOwnerNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getStoreOwnerName() == null ? "" : cellData.getValue().getStoreOwnerName()
        ));
        notesColumn.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            if (t == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }

            String type = t.getType() == null ? "" : t.getType();
            String mattress = t.getMattressName() == null ? "Matelas inconnu" : t.getMattressName();
            String owner = t.getStoreOwnerName() == null ? "" : t.getStoreOwnerName();
            int qty = t.getQuantity();
            double unitPrice = t.getPrix();
            double total = unitPrice * qty;

            StringBuilder details = new StringBuilder();
            String lowerType = type.toLowerCase(Locale.ROOT);

            if (lowerType.startsWith("vente")) {
                details.append("Vente de ")
                    .append(qty).append(" x ").append(mattress)
                    .append(" à ").append(String.format("%.2f DT", unitPrice))
                    .append(" (Total: ").append(String.format("%.2f DT", total)).append(")");
            } else if (lowerType.startsWith("prêt") || lowerType.startsWith("pret")) {
                details.append("Prêt de ")
                    .append(qty).append(" x ").append(mattress);
                if (!owner.isEmpty()) {
                    details.append(" à ").append(owner);
                }
                if (t.getExpectedReturnDate() != null) {
                    details.append(" (retour prévu le ").append(t.getExpectedReturnDate()).append(")");
                }
            } else if (lowerType.startsWith("transfert")) {
                details.append("Transfert de ")
                    .append(qty).append(" x ").append(mattress);
                if (!owner.isEmpty()) {
                    details.append(" vers ").append(owner);
                }
            } else if (lowerType.startsWith("retour")) {
                details.append("Retour en stock de ")
                    .append(qty).append(" x ").append(mattress);
                if (!owner.isEmpty()) {
                    details.append(" depuis ").append(owner);
                }
            } else if (lowerType.startsWith("réception") || lowerType.startsWith("reception")) {
                details.append("Réception de ")
                    .append(qty).append(" x ").append(mattress)
                    .append(" (stock augmenté)");
            } else if (lowerType.startsWith("pack")) {
                // For packs, show pack contents from pack_items table
                List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(t.getId());
                if (packItems != null && !packItems.isEmpty()) {
                    details.append("Pack: ");
                    for (int i = 0; i < packItems.size(); i++) {
                        PackItem item = packItems.get(i);
                        if (i > 0) details.append(" + ");
                        details.append(item.getQuantity())
                               .append(" x ")
                               .append(item.getMattressName() != null ? item.getMattressName() : "Matelas");
                    }
                    details.append(" (Total: ").append(String.format("%.2f DT", unitPrice)).append(")");
                } else {
                    details.append("Pack: ").append(mattress).append(" (Total: ").append(String.format("%.2f DT", unitPrice)).append(")");
                }
            } else {
                details.append(type).append(" - ")
                    .append(qty).append(" x ").append(mattress);
            }

            String notes = t.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                details.append(" – ").append(notes.trim());
            }

            return new javafx.beans.property.SimpleStringProperty(details.toString());
        });
        
        // Set cell factory for notes column to wrap text for pack transactions
        notesColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        
        expectedReturnDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getExpectedReturnDate() == null ? "" : cellData.getValue().getExpectedReturnDate().toString()
        ));
        totalPriceColumn.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            if (t == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            String type = t.getType() == null ? "" : t.getType().toLowerCase(Locale.ROOT);
            double totalPrice;
            if (type.startsWith("pack")) {
                // For packs, prix already contains the total pack price (not per unit)
                totalPrice = t.getPrix();
            } else {
                // For regular transactions, calculate: unit price * quantity
                totalPrice = t.getPrix() * t.getQuantity();
            }
            return new javafx.beans.property.SimpleStringProperty(String.format("%.2f DT", totalPrice));
        });
        // Set column alignment for better display
        dateColumn.setStyle("-fx-alignment: CENTER;");
        mattressNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        quantityColumn.setStyle("-fx-alignment: CENTER;");
        typeColumn.setStyle("-fx-alignment: CENTER;");
        prixColumn.setStyle("-fx-alignment: CENTER;");
        totalPriceColumn.setStyle("-fx-alignment: CENTER;");
        storeOwnerNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        notesColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        expectedReturnDateColumn.setStyle("-fx-alignment: CENTER;");
        mattressNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        quantityColumn.setStyle("-fx-alignment: CENTER;");
        typeColumn.setStyle("-fx-alignment: CENTER;");
        prixColumn.setStyle("-fx-alignment: CENTER;");
        totalPriceColumn.setStyle("-fx-alignment: CENTER;");
        storeOwnerNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        notesColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        expectedReturnDateColumn.setStyle("-fx-alignment: CENTER;");
        totalPriceColumn.setStyle("-fx-alignment: CENTER;");

        // Set minimum widths for columns to ensure readability
        addMattressColumn.setMinWidth(50);
        addMattressColumn.setMaxWidth(50);
        dateColumn.setMinWidth(100);
        mattressNameColumn.setMinWidth(150);
        quantityColumn.setMinWidth(70);
        typeColumn.setMinWidth(120);
        prixColumn.setMinWidth(100);
        totalPriceColumn.setMinWidth(100);
        storeOwnerNameColumn.setMinWidth(120);
        notesColumn.setMinWidth(200);
        expectedReturnDateColumn.setMinWidth(110);
        
        // Auto-size columns to fit content after table is rendered
        Platform.runLater(() -> {
            // Small delay to ensure table is fully rendered
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> autoResizeColumns());
            pause.play();
        });
        
        transactionTable.setPlaceholder(tableSkeleton);

        // Wrap list in a FilteredList so we can change view per type
        filteredTransactions = new FilteredList<>(transactionList, t -> true);
        transactionTable.setItems(filteredTransactions);

        try {
            setupViewModeComboBox();
            setupAddMatressColumn();
            setupDateRangeFilters();
            setupQuickRanges();
            setupSearchAndSort();
            setupGroupByDayToggle();
        } catch (Exception e) {
            System.err.println("Error setting up optional transaction features: " + e.getMessage());
            e.printStackTrace();
            // Continue initialization even if optional features fail
        }
        
        // Set row factory to make pack transactions taller
        transactionTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<Transaction>() {
                @Override
                protected void updateItem(Transaction item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setPrefHeight(-1);
                        setStyle("");
                    } else {
                        String type = item.getType() == null ? "" : item.getType().toLowerCase(Locale.ROOT);
                        if (type.startsWith("pack")) {
                            // Make pack rows taller to accommodate multiple items
                            List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(item.getId());
                            int itemCount = packItems != null ? packItems.size() : 1;
                            // Base height 56px, add ~35px per additional item
                            double height = 56 + (itemCount > 1 ? (itemCount - 1) * 35 : 0);
                            setPrefHeight(height);
                            setMinHeight(height);
                            setMaxHeight(height);
                        } else {
                            setPrefHeight(-1); // Use default height
                            setMinHeight(-1);
                            setMaxHeight(-1);
                        }
                    }
                }
            };
            return row;
        });
        
        // Add selection listener to show relevant columns when transaction is clicked
        transactionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateColumnsForTransactionType(newSelection);
            }
        });
        
        if (TransactionDAO.isSortOrderSupported()) {
            enableRowReordering();
        } else {
            System.out.println("[TransactionsController] L'ordre manuel est désactivé tant que la colonne sort_order n'est pas disponible.");
        }
        loadTransactions();
    }
    
    /**
     * Update column visibility based on selected transaction type
     */
    private void updateColumnsForTransactionType(Transaction transaction) {
        if (transaction == null || transaction.getType() == null) {
            return;
        }
        
        String type = transaction.getType().toLowerCase(Locale.ROOT).trim();
        
        if (type.startsWith("vente") || type.startsWith("pack")) {
            // For sales and packs: show price columns, hide owner and return date
            storeOwnerNameColumn.setVisible(false);
            expectedReturnDateColumn.setVisible(false);
            prixColumn.setVisible(true);
            totalPriceColumn.setVisible(true);
        } else if (type.startsWith("prêt") || type.startsWith("pret")) {
            // For loans: show owner and return date, hide price columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(false);
            totalPriceColumn.setVisible(false);
        } else if (type.startsWith("transfert")) {
            // For transfers: show owner, hide return date and price columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(false);
            prixColumn.setVisible(false);
            totalPriceColumn.setVisible(false);
        } else if (type.startsWith("retour") || type.startsWith("réception") || type.startsWith("reception")) {
            // For returns/receptions: show return date if applicable, hide price columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(false);
            totalPriceColumn.setVisible(false);
        } else {
            // Default: show all columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(true);
            totalPriceColumn.setVisible(true);
        }
        
        // Auto-resize columns after visibility changes
        Platform.runLater(() -> {
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> autoResizeColumns());
            pause.play();
        });
    }

    @FXML
    public void loadTransactions() {
        showSkeleton(true);
        CompletableFuture
            .supplyAsync(() -> {
                List<Transaction> transactions = TransactionDAO.getAllTransactions();
                // Pack transactions should NOT be expanded - show as single rows
                return transactions;
            })
            .thenAccept(list -> Platform.runLater(() -> {
                transactionList.setAll(list);
                System.out.println("[TransactionsController] Transactions chargées: " + transactionList.size());
                errorLabel.setText("");
                applyViewModeFilter();
                // Auto-resize after data is loaded
                Platform.runLater(() -> {
                    PauseTransition pause = new PauseTransition(Duration.millis(150));
                    pause.setOnFinished(e -> autoResizeColumns());
                    pause.play();
                });
                showSkeleton(false);
            }))
            .exceptionally(ex -> {
                com.warehouse.util.ErrorHandler.handleError(
                    "TransactionsController.loadTransactions",
                    ex,
                    () -> {
                        String userMessage = com.warehouse.util.ErrorHandler.getUserFriendlyMessage(ex);
                        errorLabel.setText("Erreur: " + userMessage);
                        if (dashboardController != null) {
                            dashboardController.showNotification("Erreur lors du chargement des transactions: " + userMessage, true);
                        }
                    }
                );
                return null;
            });
    }

    private void showSkeleton(boolean loading) {
        if (loading) {
            transactionTable.setPlaceholder(tableSkeleton);
        } else {
            transactionTable.setPlaceholder(transactionList.isEmpty() ? emptyPlaceholder : new Label(""));
        }
    }

    private void setupViewModeComboBox() {
        if (viewModeComboBox == null) {
            return;
        }
        viewModeComboBox.setItems(FXCollections.observableArrayList(
            "Toutes les transactions",
            "Ventes",
            "Packs",
            "Prêts",
            "Transferts",
            "Retours / Réceptions"
        ));
        viewModeComboBox.getSelectionModel().selectFirst();
        viewModeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyViewModeFilter());
    }

    private void applyViewModeFilter() {
        if (filteredTransactions == null) {
            return;
        }
        String mode = viewModeComboBox != null ? viewModeComboBox.getValue() : "Toutes les transactions";
        if (mode == null || mode.trim().isEmpty() || "Toutes les transactions".equals(mode)) {
            filteredTransactions.setPredicate(t -> true);
            // Show all columns when viewing all transactions
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(true);
            totalPriceColumn.setVisible(true);
        } else if ("Ventes".equals(mode)) {
            filteredTransactions.setPredicate(t -> isTransactionType(t, "vente"));
            // For sales: show price columns, hide owner and return date
            storeOwnerNameColumn.setVisible(false);
            expectedReturnDateColumn.setVisible(false);
            prixColumn.setVisible(true);
            totalPriceColumn.setVisible(true);
        } else if ("Packs".equals(mode)) {
            filteredTransactions.setPredicate(t -> isTransactionType(t, "pack"));
            // For packs: show price columns, hide owner and return date
            storeOwnerNameColumn.setVisible(false);
            expectedReturnDateColumn.setVisible(false);
            prixColumn.setVisible(true);
            totalPriceColumn.setVisible(true);
        } else if ("Prêts".equals(mode)) {
            filteredTransactions.setPredicate(t -> isTransactionType(t, "pret") || isTransactionType(t, "prêt"));
            // For loans: show owner and return date, hide price columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(false);
            totalPriceColumn.setVisible(false);
        } else if ("Transferts".equals(mode)) {
            filteredTransactions.setPredicate(t -> isTransactionType(t, "transfert"));
            // For transfers: show owner, hide return date and price columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(false);
            prixColumn.setVisible(false);
            totalPriceColumn.setVisible(false);
        } else if ("Retours / Réceptions".equals(mode)) {
            filteredTransactions.setPredicate(t -> isTransactionType(t, "retour") || isTransactionType(t, "reception") || isTransactionType(t, "réception"));
            // For returns/receptions: show return date if applicable, hide price columns
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(false);
            totalPriceColumn.setVisible(false);
        } else {
            filteredTransactions.setPredicate(t -> true);
            storeOwnerNameColumn.setVisible(true);
            expectedReturnDateColumn.setVisible(true);
            prixColumn.setVisible(true);
            totalPriceColumn.setVisible(true);
        }
        
        // Auto-resize columns after visibility changes
        Platform.runLater(() -> {
            // Small delay to ensure table is fully rendered
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> autoResizeColumns());
            pause.play();
        });
        
        // Reapply grouping if enabled
        if (isGroupedView) {
            applyGrouping();
        }
    }
    
    /**
     * Robust transaction type checking that handles variations
     */
    private boolean isTransactionType(Transaction t, String type) {
        if (t == null || t.getType() == null) return false;
        String transactionType = t.getType().toLowerCase(Locale.ROOT).trim();
        String searchType = type.toLowerCase(Locale.ROOT).trim();
        
        // Handle variations
        if (searchType.equals("vente")) {
            return transactionType.startsWith("vente");
        } else if (searchType.equals("pret") || searchType.equals("prêt")) {
            return transactionType.startsWith("prêt") || transactionType.startsWith("pret") || transactionType.contains("pret");
        } else if (searchType.equals("transfert")) {
            return transactionType.startsWith("transfert") || transactionType.contains("transfert");
        } else if (searchType.equals("retour")) {
            return transactionType.startsWith("retour") || transactionType.contains("retour");
        } else if (searchType.equals("reception") || searchType.equals("réception")) {
            return transactionType.startsWith("réception") || transactionType.startsWith("reception") || transactionType.contains("reception");
        } else if (searchType.equals("pack")) {
            return transactionType.startsWith("pack") || transactionType.equalsIgnoreCase("pack");
        }
        return transactionType.contains(searchType);
    }
    
    /**
     * Auto-resize columns to fit their content
     */
    private void autoResizeColumns() {
        if (transactionTable == null || filteredTransactions == null) {
            return;
        }
        
        // Use the actual items from the table (which uses filteredTransactions)
        ObservableList<Transaction> items = transactionTable.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // Calculate optimal widths based on visible content
        for (TableColumn<?, ?> column : transactionTable.getColumns()) {
            if (column != null && column.isVisible()) {
                double maxWidth = column.getMinWidth();
                // Sample from visible items (check first 50 to avoid performance issues)
                int sampleSize = Math.min(50, items.size());
                for (int i = 0; i < sampleSize; i++) {
                    Transaction t = items.get(i);
                    if (t != null) {
                        String content = getColumnContent(t, column);
                        if (content != null && !content.isEmpty()) {
                            // Estimate width based on character count (rough estimate: 7-8 pixels per character)
                            double estimatedWidth = content.length() * 7.5 + 30; // Add padding
                            maxWidth = Math.max(maxWidth, estimatedWidth);
                        }
                    }
                }
                // Set pref width but respect min/max constraints
                double optimalWidth = Math.max(column.getMinWidth(), Math.min(maxWidth, 500));
                column.setPrefWidth(optimalWidth);
            }
        }
    }
    
    /**
     * Get the string content for a column
     */
    private String getColumnContent(Transaction t, TableColumn<?, ?> column) {
        if (column == dateColumn) {
            return t.getDate().toString();
        } else if (column == mattressNameColumn) {
            return t.getMattressName() != null ? t.getMattressName() : "Inconnu";
        } else if (column == quantityColumn) {
            return String.valueOf(t.getQuantity());
        } else if (column == typeColumn) {
            return t.getType() != null ? t.getType() : "";
        } else if (column == prixColumn) {
            return String.format("%.2f DT", t.getPrix());
        } else if (column == totalPriceColumn) {
            return String.format("%.2f DT", t.getPrix() * t.getQuantity());
        } else if (column == storeOwnerNameColumn) {
            return t.getStoreOwnerName() != null ? t.getStoreOwnerName() : "";
        } else if (column == notesColumn) {
            // Get the formatted notes content
            String type = t.getType() != null ? t.getType() : "";
            String mattress = t.getMattressName() != null ? t.getMattressName() : "Matelas inconnu";
            String owner = t.getStoreOwnerName() != null ? t.getStoreOwnerName() : "";
            int qty = t.getQuantity();
            double unitPrice = t.getPrix();
            double total = unitPrice * qty;
            
            StringBuilder details = new StringBuilder();
            String lowerType = type.toLowerCase(Locale.ROOT);
            
            if (lowerType.startsWith("vente")) {
                details.append("Vente de ").append(qty).append(" x ").append(mattress)
                    .append(" à ").append(String.format("%.2f DT", unitPrice))
                    .append(" (Total: ").append(String.format("%.2f DT", total)).append(")");
            } else if (lowerType.startsWith("prêt") || lowerType.startsWith("pret")) {
                details.append("Prêt de ").append(qty).append(" x ").append(mattress);
                if (!owner.isEmpty()) {
                    details.append(" à ").append(owner);
                }
                if (t.getExpectedReturnDate() != null) {
                    details.append(" (retour prévu le ").append(t.getExpectedReturnDate()).append(")");
                }
            } else if (lowerType.startsWith("transfert")) {
                details.append("Transfert de ").append(qty).append(" x ").append(mattress);
                if (!owner.isEmpty()) {
                    details.append(" vers ").append(owner);
                }
            } else if (lowerType.startsWith("retour")) {
                details.append("Retour en stock de ").append(qty).append(" x ").append(mattress);
                if (!owner.isEmpty()) {
                    details.append(" depuis ").append(owner);
                }
            } else if (lowerType.startsWith("réception") || lowerType.startsWith("reception")) {
                details.append("Réception de ").append(qty).append(" x ").append(mattress)
                    .append(" (stock augmenté)");
            } else {
                details.append(type).append(" - ").append(qty).append(" x ").append(mattress);
            }
            
            String notes = t.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                details.append(" – ").append(notes.trim());
            }
            
            return details.toString();
        } else if (column == expectedReturnDateColumn) {
            return t.getExpectedReturnDate() != null ? t.getExpectedReturnDate().toString() : "";
        }
        return "";
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionOverlay.fxml"));
            Parent overlayRoot = loader.load();
            TransactionOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setTransactionsController(this);
            controller.setTransaction(null); // Add mode
            
            // Show the overlay
            if (dashboardController != null) {
                dashboardController.showOverlay(overlayRoot);
            }
        } catch (Exception e) {
            com.warehouse.util.ErrorHandler.handleError(
                "TransactionsController.handleAdd",
                e,
                () -> {
                    String userMessage = com.warehouse.util.ErrorHandler.getUserFriendlyMessage(e);
                    errorLabel.setText("Erreur lors de l'ouverture du dialogue d'ajout: " + userMessage);
                    if (dashboardController != null) {
                        dashboardController.showNotification("Impossible d'ouvrir l'ajout: " + userMessage, true);
                    }
                }
            );
        }
    }

    @FXML
    private void handleRefresh() {
        loadTransactions();
    }

    @FXML
    private void handlePdfReport() {
        try {
            LocalDate selectedDate = reportDatePicker.getValue();
            if (selectedDate == null) {
                errorLabel.setText("Veuillez sélectionner une date pour le rapport.");
                return;
            }
            String filename = "rapport_transactions_" + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String filePath = com.warehouse.util.PdfReportUtil.generateDailyTransactionsReport(selectedDate, filename);
            if (filePath != null) {
                errorLabel.setText("Rapport PDF généré avec succès: " + filename);
                // Open PDF and folder automatically
                String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + java.io.File.separator + "LES RAPPORT QUOTIDIEN";
                com.warehouse.util.PdfReportUtil.openPdfFile(filePath);
                com.warehouse.util.PdfReportUtil.openFolder(desktopPath);
            } else {
                errorLabel.setText("Échec de la génération du rapport PDF.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la génération du rapport PDF.");
        }
    }
    
    @FXML
    private void handlePdfRangeReport() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate == null || endDate == null) {
                errorLabel.setText("Veuillez sélectionner une date de début et une date de fin.");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                errorLabel.setText("La date de début doit être antérieure à la date de fin.");
                return;
            }
            
            String filename = "transactions_" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                            "_to_" + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            String filePath = com.warehouse.util.PdfReportUtil.generateDateRangeTransactionsReport(startDate, endDate, filename);
            
            if (filePath != null) {
                errorLabel.setText("Rapport PDF généré avec succès: " + filename);
                String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + 
                                    java.io.File.separator + "RAPPORT DE TRANSACTION";
                com.warehouse.util.PdfReportUtil.openPdfFile(filePath);
                com.warehouse.util.PdfReportUtil.openFolder(desktopPath);
                if (dashboardController != null) {
                    dashboardController.showNotification("Rapport PDF généré avec succès!", false);
                }
            } else {
                errorLabel.setText("Échec de la génération du rapport PDF.");
                if (dashboardController != null) {
                    dashboardController.showNotification("Échec de la génération du rapport PDF.", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la génération du rapport PDF: " + e.getMessage());
            if (dashboardController != null) {
                dashboardController.showNotification("Erreur lors de la génération du rapport PDF.", true);
            }
        }
    }
    
    @FXML
    private void handleExportCsv() {
        try {
            LocalDate startDate = startDatePicker != null && startDatePicker.getValue() != null 
                ? startDatePicker.getValue() 
                : LocalDate.now().minusMonths(1);
            LocalDate endDate = endDatePicker != null && endDatePicker.getValue() != null 
                ? endDatePicker.getValue() 
                : LocalDate.now();
            
            String filename = "transactions_" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                            "_to_" + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
            String filePath = com.warehouse.util.CsvExportUtil.exportTransactionsToCsv(
                filteredTransactions != null ? filteredTransactions : transactionList,
                startDate, endDate, filename
            );
            
            if (filePath != null) {
                errorLabel.setText("Export CSV réussi: " + filename);
                String desktopPath = System.getProperty("user.home") + java.io.File.separator + "Desktop" + 
                                    java.io.File.separator + "RAPPORT DE TRANSACTION";
                com.warehouse.util.PdfReportUtil.openFolder(desktopPath);
                if (dashboardController != null) {
                    dashboardController.showNotification("Export CSV réussi!", false);
                }
            } else {
                errorLabel.setText("Échec de l'export CSV.");
                if (dashboardController != null) {
                    dashboardController.showNotification("Échec de l'export CSV.", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'export CSV: " + e.getMessage());
            if (dashboardController != null) {
                dashboardController.showNotification("Erreur lors de l'export CSV.", true);
            }
        }
    }
    
    private void setupDateRangeFilters() {
        // Set default dates (last 30 days)
        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now().minusDays(30));
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(LocalDate.now());
        }
        
        // Apply filter when dates change
        if (startDatePicker != null) {
            startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateRangeFilter());
        }
        if (endDatePicker != null) {
            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateRangeFilter());
        }
    }
    
    private void setupQuickRanges() {
        if (quickRangeButtonsContainer == null) {
            return;
        }
        
        // Create quick range buttons
        Button todayBtn = new Button("Aujourd'hui");
        Button yesterdayBtn = new Button("Hier");
        Button last7DaysBtn = new Button("7 derniers jours");
        Button thisMonthBtn = new Button("Ce mois");
        Button lastMonthBtn = new Button("Mois dernier");
        Button customBtn = new Button("Personnalisé");
        
        // Style the buttons
        String buttonStyle = "-fx-padding: 6 12; -fx-font-size: 12px;";
        todayBtn.setStyle(buttonStyle);
        yesterdayBtn.setStyle(buttonStyle);
        last7DaysBtn.setStyle(buttonStyle);
        thisMonthBtn.setStyle(buttonStyle);
        lastMonthBtn.setStyle(buttonStyle);
        customBtn.setStyle(buttonStyle);
        
        // Set up button actions
        todayBtn.setOnAction(e -> handleQuickRange("today"));
        yesterdayBtn.setOnAction(e -> handleQuickRange("yesterday"));
        last7DaysBtn.setOnAction(e -> handleQuickRange("last7days"));
        thisMonthBtn.setOnAction(e -> handleQuickRange("thismonth"));
        lastMonthBtn.setOnAction(e -> handleQuickRange("lastmonth"));
        customBtn.setOnAction(e -> {
            // Custom: just ensure date pickers are visible and focused
            if (startDatePicker != null) {
                startDatePicker.requestFocus();
            }
        });
        
        // Add buttons to container
        quickRangeButtonsContainer.getChildren().addAll(
            todayBtn, yesterdayBtn, last7DaysBtn, thisMonthBtn, lastMonthBtn, customBtn
        );
        quickRangeButtonsContainer.setSpacing(8);
    }
    
    // Individual handler methods for FXML if needed
    @FXML
    private void handleQuickRangeToday() {
        handleQuickRange("today");
    }
    
    @FXML
    private void handleQuickRangeYesterday() {
        handleQuickRange("yesterday");
    }
    
    @FXML
    private void handleQuickRangeLast7Days() {
        handleQuickRange("last7days");
    }
    
    @FXML
    private void handleQuickRangeThisMonth() {
        handleQuickRange("thismonth");
    }
    
    @FXML
    private void handleQuickRangeLastMonth() {
        handleQuickRange("lastmonth");
    }
    
    private void setupSearchAndSort() {
        // Setup search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter());
        }
        
        // Setup sort order
        if (sortOrderComboBox != null) {
            sortOrderComboBox.setItems(FXCollections.observableArrayList(
                "Date (croissant)",
                "Date (décroissant)",
                "Montant (croissant)",
                "Montant (décroissant)",
                "Type"
            ));
            sortOrderComboBox.getSelectionModel().selectFirst();
            sortOrderComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applySortOrder());
        }
    }
    
    private void setupGroupByDayToggle() {
        if (groupByDayToggle != null) {
            groupByDayToggle.setText("Grouper par jour");
            groupByDayToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                isGroupedView = newVal;
                applyGrouping();
            });
        }
    }
    
    /**
     * Apply grouping by day if enabled, otherwise show regular view
     */
    private void applyGrouping() {
        if (!isGroupedView || filteredTransactions == null) {
            transactionTable.setItems(filteredTransactions);
            updateDateColumnForGrouping(); // Reset to normal view
            return;
        }
        
        // Group transactions by day
        Map<LocalDate, List<Transaction>> grouped = new HashMap<>();
        for (Transaction t : filteredTransactions) {
            LocalDate day = t.getDate().toLocalDate();
            grouped.computeIfAbsent(day, k -> new ArrayList<>()).add(t);
        }
        
        // Create a sorted list of days
        List<LocalDate> sortedDays = new ArrayList<>(grouped.keySet());
        sortedDays.sort(LocalDate::compareTo);
        
        // Create grouped display items
        ObservableList<Transaction> groupedList = FXCollections.observableArrayList();
        for (LocalDate day : sortedDays) {
            List<Transaction> dayTransactions = grouped.get(day);
            // Sort transactions within the day by time
            dayTransactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
            
            // Add all transactions for this day
            groupedList.addAll(dayTransactions);
        }
        
        transactionTable.setItems(groupedList);
        
        // Update cell factory to show day headers and totals
        updateDateColumnForGrouping();
        updateTotalPriceColumnForGrouping(grouped);
    }
    
    /**
     * Update total price column to show daily totals when grouping is enabled
     */
    private void updateTotalPriceColumnForGrouping(Map<LocalDate, List<Transaction>> grouped) {
        if (!isGroupedView || grouped == null) {
            return;
        }
        
        // Store daily totals for display
        Map<LocalDate, Double> dailyTotals = new HashMap<>();
        for (Map.Entry<LocalDate, List<Transaction>> entry : grouped.entrySet()) {
            double dayTotal = 0.0;
            for (Transaction t : entry.getValue()) {
                String type = t.getType() != null ? t.getType().toLowerCase(Locale.ROOT) : "";
                if (type.startsWith("pack")) {
                    dayTotal += t.getPrix(); // Pack price is already total
                } else if (type.startsWith("vente")) {
                    dayTotal += t.getPrix() * t.getQuantity();
                }
            }
            dailyTotals.put(entry.getKey(), dayTotal);
        }
        
        // Update cell factory to show totals on first row of each day
        totalPriceColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            private LocalDate currentDay = null;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    currentDay = null;
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    if (t != null) {
                        LocalDate transactionDay = t.getDate().toLocalDate();
                        
                        // Check if this is the first transaction of the day
                        boolean isFirstOfDay = false;
                        if (currentDay == null || !currentDay.equals(transactionDay)) {
                            currentDay = transactionDay;
                            
                            // Check if previous row is different day
                            if (getIndex() > 0) {
                                Transaction prevT = getTableView().getItems().get(getIndex() - 1);
                                if (prevT != null) {
                                    LocalDate prevDay = prevT.getDate().toLocalDate();
                                    isFirstOfDay = !prevDay.equals(transactionDay);
                                }
                            } else {
                                isFirstOfDay = true;
                            }
                        }
                        
                        if (isFirstOfDay && dailyTotals.containsKey(transactionDay)) {
                            // Show daily total on first row
                            double dayTotal = dailyTotals.get(transactionDay);
                            setText("Total jour: " + String.format("%.2f DT", dayTotal));
                            setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");
                        } else {
                            // Show transaction total
                            String type = t.getType() != null ? t.getType().toLowerCase(Locale.ROOT) : "";
                            double totalPrice;
                            if (type.startsWith("pack")) {
                                totalPrice = t.getPrix();
                            } else {
                                totalPrice = t.getPrix() * t.getQuantity();
                            }
                            setText(String.format("%.2f DT", totalPrice));
                            setStyle("");
                        }
                    } else {
                        setText(item);
                        setStyle("");
                    }
                }
            }
        });
    }
    
    /**
     * Update date column to show day headers when grouping is enabled
     */
    private void updateDateColumnForGrouping() {
        if (isGroupedView) {
            dateColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                private LocalDate currentDay = null;
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        currentDay = null;
                    } else {
                        Transaction t = getTableView().getItems().get(getIndex());
                        if (t != null) {
                            LocalDate transactionDay = t.getDate().toLocalDate();
                            
                            // Check if this is the first transaction of the day
                            boolean isFirstOfDay = false;
                            if (currentDay == null || !currentDay.equals(transactionDay)) {
                                isFirstOfDay = true;
                                currentDay = transactionDay;
                                
                                // Check if previous row is different day
                                if (getIndex() > 0) {
                                    Transaction prevT = getTableView().getItems().get(getIndex() - 1);
                                    if (prevT != null) {
                                        LocalDate prevDay = prevT.getDate().toLocalDate();
                                        isFirstOfDay = !prevDay.equals(transactionDay);
                                    }
                                } else {
                                    isFirstOfDay = true;
                                }
                            }
                            
                            if (isFirstOfDay) {
                                // Show day header
                                setText("📅 " + transactionDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd (EEEE)", Locale.FRENCH)));
                                setStyle("-fx-font-weight: bold; -fx-background-color: #e8f4f8; -fx-padding: 8;");
                            } else {
                                // Show time only
                                setText(t.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                setStyle("");
                            }
                        } else {
                            setText(item);
                            setStyle("");
                        }
                    }
                }
            });
        } else {
            // Reset to normal date display
            dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
            dateColumn.setCellFactory(null);
            
            // Reset total price column to normal display
            totalPriceColumn.setCellValueFactory(cellData -> {
                Transaction t = cellData.getValue();
                if (t == null) {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
                String type = t.getType() != null ? t.getType().toLowerCase(Locale.ROOT) : "";
                double totalPrice;
                if (type.startsWith("pack")) {
                    totalPrice = t.getPrix();
                } else {
                    totalPrice = t.getPrix() * t.getQuantity();
                }
                return new javafx.beans.property.SimpleStringProperty(String.format("%.2f DT", totalPrice));
            });
            totalPriceColumn.setCellFactory(null);
        }
    }
    
    private void applyDateRangeFilter() {
        if (filteredTransactions == null) {
            return;
        }
        
        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : null;
        
        if (startDate == null && endDate == null) {
            // No date filter, just apply view mode filter
            applyViewModeFilter();
            return;
        }
        
        filteredTransactions.setPredicate(t -> {
            if (t == null) return false;
            
            LocalDateTime transactionDateTime = t.getDate();
            
            // Apply date range (inclusive: startDate 00:00:00 to endDate 23:59:59)
            if (startDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
                if (transactionDateTime.isBefore(startDateTime)) {
                    return false;
                }
            }
            if (endDate != null) {
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 23:59:59
                if (transactionDateTime.isAfter(endDateTime)) {
                    return false;
                }
            }
            
            // Apply view mode filter
            String mode = viewModeComboBox != null ? viewModeComboBox.getValue() : "Toutes les transactions";
            if (mode == null || mode.trim().isEmpty() || "Toutes les transactions".equals(mode)) {
                return true;
            } else if ("Ventes".equals(mode)) {
                return isTransactionType(t, "vente");
            } else if ("Packs".equals(mode)) {
                return isTransactionType(t, "pack");
            } else if ("Prêts".equals(mode)) {
                return isTransactionType(t, "pret") || isTransactionType(t, "prêt");
            } else if ("Transferts".equals(mode)) {
                return isTransactionType(t, "transfert");
            } else if ("Retours / Réceptions".equals(mode)) {
                return isTransactionType(t, "retour") || isTransactionType(t, "reception") || isTransactionType(t, "réception");
            }
            
            return true;
        });
        
        // Reapply grouping if enabled
        if (isGroupedView) {
            applyGrouping();
        }
    }
    
    private void applySearchFilter() {
        if (filteredTransactions == null || searchField == null) {
            return;
        }
        
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            applyDateRangeFilter(); // Just apply date range
            return;
        }
        
        String searchLower = searchText.toLowerCase(Locale.ROOT).trim();
        
        filteredTransactions.setPredicate(t -> {
            if (t == null) return false;
            
            // Search in mattress name
            if (t.getMattressName() != null && t.getMattressName().toLowerCase(Locale.ROOT).contains(searchLower)) {
                return true;
            }
            
            // Search in transaction ID
            if (String.valueOf(t.getId()).contains(searchText)) {
                return true;
            }
            
            // Search in notes
            if (t.getNotes() != null && t.getNotes().toLowerCase(Locale.ROOT).contains(searchLower)) {
                return true;
            }
            
            // Search in type
            if (t.getType() != null && t.getType().toLowerCase(Locale.ROOT).contains(searchLower)) {
                return true;
            }
            
            return false;
        });
        
        // Reapply grouping if enabled
        if (isGroupedView) {
            applyGrouping();
        }
    }
    
    private void applySortOrder() {
        if (sortOrderComboBox == null || transactionTable == null) {
            return;
        }
        
        // If grouping is enabled, sorting is handled within groups
        if (isGroupedView) {
            applyGrouping(); // Reapply grouping which sorts by date
            return;
        }
        
        String sortOrder = sortOrderComboBox.getValue();
        if (sortOrder == null) {
            return;
        }
        
        ObservableList<Transaction> items = transactionTable.getItems();
        if (items == null) {
            return;
        }
        
        javafx.collections.transformation.SortedList<Transaction> sortedList = 
            new javafx.collections.transformation.SortedList<>(items);
        
        switch (sortOrder) {
            case "Date (croissant)":
                sortedList.setComparator((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
                break;
            case "Date (décroissant)":
                sortedList.setComparator((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
                break;
            case "Montant (croissant)":
                sortedList.setComparator((t1, t2) -> {
                    double total1 = t1.getPrix() * t1.getQuantity();
                    double total2 = t2.getPrix() * t2.getQuantity();
                    return Double.compare(total1, total2);
                });
                break;
            case "Montant (décroissant)":
                sortedList.setComparator((t1, t2) -> {
                    double total1 = t1.getPrix() * t1.getQuantity();
                    double total2 = t2.getPrix() * t2.getQuantity();
                    return Double.compare(total2, total1);
                });
                break;
            case "Type":
                sortedList.setComparator((t1, t2) -> {
                    String type1 = t1.getType() != null ? t1.getType() : "";
                    String type2 = t2.getType() != null ? t2.getType() : "";
                    return type1.compareToIgnoreCase(type2);
                });
                break;
        }
        
        transactionTable.setItems(sortedList);
    }
    
    @FXML
    private void handleQuickRange(String range) {
        LocalDate today = LocalDate.now();
        LocalDate start, end = today;
        
        switch (range) {
            case "today":
                start = today;
                end = today;
                break;
            case "yesterday":
                start = today.minusDays(1);
                end = today.minusDays(1);
                break;
            case "last7days":
                start = today.minusDays(7);
                end = today;
                break;
            case "thismonth":
                start = today.withDayOfMonth(1);
                end = today;
                break;
            case "lastmonth":
                start = today.minusMonths(1).withDayOfMonth(1);
                end = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
                break;
            default:
                return;
        }
        
        if (startDatePicker != null) {
            startDatePicker.setValue(start);
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(end);
        }
    }

    private void setupAddMatressColumn() {
        if (addMattressColumn == null) {
            return;
        }
        addMattressColumn.setCellFactory(col -> new AddButtonCell());
    }

    private class AddButtonCell extends TableCell<Transaction, Void> {
        private final Button addButton = new Button();

        AddButtonCell() {
            // Use icon instead of text
            var plusIcon = IconFactory.svg("plus", 16);
            addButton.setGraphic(plusIcon);
            addButton.setText(null); // Remove any text
            addButton.getStyleClass().addAll("icon-button");
            addButton.setMaxWidth(40);
            addButton.setMinWidth(40);
            addButton.setMaxHeight(40);
            addButton.setMinHeight(40);
            addButton.setPrefWidth(40);
            addButton.setPrefHeight(40);
            addButton.setTooltip(new Tooltip("Ajouter un matelas (+1)"));
            addButton.setOnAction(event -> {
                Transaction transaction = getTableView().getItems().get(getIndex());
                increaseTransactionQuantity(transaction);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                setGraphic(addButton);
            }
        }
    }

    private void confirmAndDelete(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer la transaction");
        alert.setHeaderText("Voulez-vous vraiment supprimer cette transaction ?");
        alert.setContentText("Cette action est irréversible.");
        alert.initOwner(transactionTable.getScene().getWindow());

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (TransactionDAO.deleteTransaction(transaction.getId())) {
                    loadTransactions();
                } else {
                    errorLabel.setText("Impossible de supprimer la transaction.");
                }
            }
        });
    }

    @FXML
    private void handleDelete() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Sélectionnez une transaction à supprimer.");
            return;
        }
        confirmAndDelete(selected);
    }

    private void increaseTransactionQuantity(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        try {
            transaction.setQuantity(transaction.getQuantity() + 1);
            boolean success = TransactionDAO.updateTransaction(transaction);
            // Note: Inventory is automatically updated by TransactionDAO.updateTransaction()
            if (success) {
                loadTransactions();
                // Refresh inventory if available
                if (dashboardController != null) {
                    dashboardController.refreshInventory();
                }
                errorLabel.setText("Quantité augmentée.");
            } else {
                errorLabel.setText("Impossible d'ajouter le matelas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ajout.");
        }
    }

    private void enableRowReordering() {
        transactionTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    draggedTransaction = row.getItem();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(draggedTransaction.getNotes());
                    db.setContent(content);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                if (draggedTransaction != null && row.getItem() != null && draggedTransaction != row.getItem()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });

            row.setOnDragDropped(event -> handleTransactionDrop(row, event));
            row.setOnDragDone(event -> draggedTransaction = null);
            return row;
        });

        transactionTable.setOnDragOver(event -> {
            if (draggedTransaction != null) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        transactionTable.setOnDragDropped(event -> {
            if (draggedTransaction != null) {
                transactionList.remove(draggedTransaction);
                transactionList.add(draggedTransaction);
                persistTransactionOrder();
                draggedTransaction = null;
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }

    private void handleTransactionDrop(TableRow<Transaction> row, DragEvent event) {
        if (draggedTransaction == null) {
            return;
        }
        int dropIndex = row.isEmpty() ? transactionList.size() : row.getIndex();
        transactionList.remove(draggedTransaction);
        if (dropIndex > transactionList.size()) {
            dropIndex = transactionList.size();
        }
        transactionList.add(dropIndex, draggedTransaction);
        transactionTable.getSelectionModel().select(draggedTransaction);
        persistTransactionOrder();
        draggedTransaction = null;
        event.setDropCompleted(true);
        event.consume();
    }

    private void persistTransactionOrder() {
        if (TransactionDAO.isSortOrderSupported()) {
            TransactionDAO.updateSortOrder(transactionTable.getItems());
        }
    }

    /**
     * Expand pack transactions into multiple rows (one per pack item)
     */
    private List<Transaction> expandPackTransactions(List<Transaction> transactions) {
        List<Transaction> expanded = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if ("Pack".equalsIgnoreCase(transaction.getType())) {
                // Get pack items for this transaction
                List<PackItem> packItems = PackItemDAO.getPackItemsByTransactionId(transaction.getId());
                if (packItems != null && !packItems.isEmpty()) {
                    // Create a transaction row for each pack item
                    for (int i = 0; i < packItems.size(); i++) {
                        PackItem item = packItems.get(i);
                        // For price display: show full pack price in first row, 0 in others (to avoid double counting)
                        double displayPrice = (i == 0) ? transaction.getPrix() : 0.0;
                        
                        Transaction itemTransaction = new Transaction(
                            transaction.getId(), // Keep same ID to group them
                            transaction.getDate(),
                            item.getMattressId(),
                            item.getQuantity(),
                            "Pack", // Keep pack type
                            transaction.getStoreOwnerId(),
                            transaction.getUserId(),
                            displayPrice, // Show full pack price only in first row
                            transaction.getNotes(), // Keep original notes
                            transaction.getExpectedReturnDate(),
                            transaction.getSortOrder(),
                            item.getMattressName(), // Use mattress name from pack item
                            transaction.getStoreOwnerName()
                        );
                        expanded.add(itemTransaction);
                    }
                } else {
                    // No pack items found, just add the transaction as-is
                    expanded.add(transaction);
                }
            } else {
                // Regular transaction, add as-is
                expanded.add(transaction);
            }
        }
        return expanded;
    }
    
    // Removed bindColumnWidths - now using auto-resize
} 