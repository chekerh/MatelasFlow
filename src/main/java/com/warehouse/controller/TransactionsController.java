package com.warehouse.controller;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.MattressDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private DashboardController dashboardController;
    private Transaction draggedTransaction;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        transactionTable.setFixedCellSize(56);
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
        mattressNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getMattressName() != null ? cellData.getValue().getMattressName() : "Inconnu"
        ));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
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
        notesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNotes()));
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

        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bindColumnWidths();
        transactionTable.setPlaceholder(new Label("Aucune transaction enregistrée."));
        transactionTable.setItems(transactionList);
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
        transactionList.setAll(TransactionDAO.getAllTransactions());
        System.out.println("[TransactionsController] Transactions chargées: " + transactionList.size());
        errorLabel.setText("");
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
        private final Button addButton = new Button("➕");

        AddButtonCell() {
            addButton.getStyleClass().add("ghost-button");
            addButton.setMaxWidth(Double.MAX_VALUE);
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
            if (success) {
                adjustInventoryForQuickAdd(transaction);
                loadTransactions();
                errorLabel.setText("Quantité augmentée.");
            } else {
                errorLabel.setText("Impossible d'ajouter le matelas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ajout.");
        }
    }

    private void adjustInventoryForQuickAdd(Transaction transaction) {
        String type = transaction.getType();
        if ("Vente".equalsIgnoreCase(type) || "Prêt".equalsIgnoreCase(type) || "Transfert".equalsIgnoreCase(type)) {
            MattressDAO.decreaseQuantity(transaction.getMattressId(), 1);
        } else if ("retour".equalsIgnoreCase(type) || "Réception".equalsIgnoreCase(type)) {
            MattressDAO.increaseQuantity(transaction.getMattressId(), 1);
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

    private void bindColumnWidths() {
        bindColumn(dateColumn, 0.12);
        bindColumn(mattressNameColumn, 0.20);
        bindColumn(quantityColumn, 0.08);
        bindColumn(typeColumn, 0.10);
        bindColumn(prixColumn, 0.10);
        bindColumn(totalPriceColumn, 0.10);
        bindColumn(storeOwnerNameColumn, 0.08);
        bindColumn(notesColumn, 0.12);
        bindColumn(expectedReturnDateColumn, 0.08);
        bindColumn(addMattressColumn, 0.10);
    }

    private void bindColumn(TableColumn<?, ?> column, double percentage) {
        if (column == null) return;
        column.prefWidthProperty().bind(transactionTable.widthProperty().multiply(percentage));
    }
} 