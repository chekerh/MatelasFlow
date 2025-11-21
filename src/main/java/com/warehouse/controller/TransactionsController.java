package com.warehouse.controller;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.ui.SkeletonPane;
import com.warehouse.ui.IconFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

    @FXML private ComboBox<String> viewModeComboBox;

    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
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
        transactionTable.setFixedCellSize(56);
        // Use flexible resize policy for dynamic column sizing
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
        mattressNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getMattressName() != null ? cellData.getValue().getMattressName() : "Inconnu"
        ));
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
                    setText(String.format("%.2f DT", item));
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
            } else {
                details.append(type).append(" - ")
                    .append(qty).append(" x ").append(mattress);
            }

            String notes = t.getNotes();
            if (notes != null && !notes.isBlank()) {
                details.append(" – ").append(notes.trim());
            }

            return new javafx.beans.property.SimpleStringProperty(details.toString());
        });
        expectedReturnDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getExpectedReturnDate() == null ? "" : cellData.getValue().getExpectedReturnDate().toString()
        ));
        totalPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            String.format("%.2f DT", cellData.getValue().getPrix() * cellData.getValue().getQuantity())
        ));
        dateColumn.setStyle("-fx-alignment: CENTER;");
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

        setupViewModeComboBox();
        setupAddMatressColumn();
        if (TransactionDAO.isSortOrderSupported()) {
            enableRowReordering();
        } else {
            System.out.println("[TransactionsController] L'ordre manuel est désactivé tant que la colonne sort_order n'est pas disponible.");
        }
        loadTransactions();
    }

    @FXML
    public void loadTransactions() {
        showSkeleton(true);
        CompletableFuture
            .supplyAsync(TransactionDAO::getAllTransactions)
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
        if (mode == null || mode.isBlank() || "Toutes les transactions".equals(mode)) {
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
            if (notes != null && !notes.isBlank()) {
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
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ouverture du dialogue d'ajout.");
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
            boolean success = com.warehouse.util.PdfReportUtil.generateDailyTransactionsReport(selectedDate, filename);
            if (success) {
                errorLabel.setText("Rapport PDF généré avec succès: " + filename);
            } else {
                errorLabel.setText("Échec de la génération du rapport PDF.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la génération du rapport PDF.");
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

    // Removed bindColumnWidths - now using auto-resize
} 