package com.warehouse.controller;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.UserDAO;
import com.warehouse.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;

public class TransactionsController {
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, Integer> mattressIdColumn;
    @FXML private TableColumn<Transaction, Integer> quantityColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Integer> storeOwnerIdColumn;
    @FXML private TableColumn<Transaction, Integer> userIdColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button pdfReportButton;
    @FXML private Label errorLabel;
    @FXML private DatePicker reportDatePicker;
    @FXML private TableColumn<Transaction, String> mattressNameColumn;
    @FXML private TableColumn<Transaction, String> storeOwnerNameColumn;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
        mattressIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getMattressId()).asObject());
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        storeOwnerIdColumn.setCellValueFactory(cellData -> cellData.getValue().getStoreOwnerId() == null ? null : new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStoreOwnerId()).asObject());
        userIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        notesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNotes()));
        // Add name columns
        mattressNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            com.warehouse.model.MattressDAO.getMattressById(cellData.getValue().getMattressId()).getType()
        ));
        storeOwnerNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getStoreOwnerId() == null ? "" : com.warehouse.model.StoreOwnerDAO.getStoreOwnerById(cellData.getValue().getStoreOwnerId()).getName()
        ));
        transactionTable.setItems(transactionList);
        loadTransactions();
    }

    @FXML
    private void loadTransactions() {
        transactionList.setAll(TransactionDAO.getAllTransactions());
        errorLabel.setText("");
    }

    private int getDefaultUserId() {
        java.util.List<User> users = UserDAO.getAllUsers();
        if (!users.isEmpty()) {
            return users.get(0).getId();
        }
        return -1;
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionDialog.fxml"));
            Parent dialogRoot = loader.load();
            TransactionDialogController controller = loader.getController();
            Stage mainStage = (Stage) addButton.getScene().getWindow();
            boolean wasFullScreen = mainStage.isFullScreen();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainStage);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setMaximized(true); // maximize for visual consistency
            dialogStage.showAndWait();
            // Restore fullscreen if it was set
            if (wasFullScreen) {
                mainStage.setFullScreen(true);
            }
            if (controller.isOkClicked()) {
                Transaction t = controller.getTransaction();
                int userId = getDefaultUserId();
                if (userId == -1) {
                    errorLabel.setText("Aucun utilisateur trouvé dans la base de données.");
                    return;
                }
                t.setUserId(userId);
                if (TransactionDAO.addTransaction(t)) {
                    // Adjust mattress quantity based on transaction type
                    if ("Vente".equals(t.getType()) || "Prêt".equals(t.getType()) || "Transfert".equals(t.getType())) {
                        boolean updated = com.warehouse.model.MattressDAO.decreaseQuantity(t.getMattressId(), t.getQuantity());
                        if (!updated) {
                            errorLabel.setText("Impossible de diminuer la quantité du matelas (stock insuffisant ?)");
                        }
                    } else if ("retour".equals(t.getType())) {
                        // For returns, increase the quantity
                        com.warehouse.model.MattressDAO.increaseQuantity(t.getMattressId(), t.getQuantity());
                    }
                    loadTransactions();
                } else {
                    errorLabel.setText("Failed to add transaction.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error opening add dialog.");
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
                errorLabel.setText("Please select a date for the report.");
                return;
            }
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            List<Transaction> daily = transactions.stream().filter(t -> t.getDate().toLocalDate().equals(selectedDate)).toList();
            com.warehouse.util.PdfReportUtil.generateDailyTransactionsReport(daily, selectedDate);
            errorLabel.setText("PDF report generated.");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to generate PDF report.");
        }
    }
} 