package com.warehouse.controller;

import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button pdfReportButton;
    @FXML private Label errorLabel;
    @FXML private DatePicker reportDatePicker;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
        mattressNameColumn.setCellValueFactory(cellData -> {
            com.warehouse.model.Mattress mattress = com.warehouse.model.MattressDAO.getMattressById(cellData.getValue().getMattressId());
            String displayText = mattress != null ? mattress.getType() : "❌ Supprimé";
            return new javafx.beans.property.SimpleStringProperty(displayText);
        });
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
        
        storeOwnerNameColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStoreOwnerId() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            com.warehouse.model.StoreOwner owner = com.warehouse.model.StoreOwnerDAO.getStoreOwnerById(cellData.getValue().getStoreOwnerId());
            String displayText = owner != null ? owner.getName() : "❌ Supprimé";
            return new javafx.beans.property.SimpleStringProperty(displayText);
        });
        notesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNotes()));
        expectedReturnDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getExpectedReturnDate() == null ? "" : cellData.getValue().getExpectedReturnDate().toString()
        ));
        transactionTable.setItems(transactionList);
        loadTransactions();
    }

    @FXML
    public void loadTransactions() {
        transactionList.setAll(TransactionDAO.getAllTransactions());
        errorLabel.setText("");
    }

    private int getDefaultUserId() {
        List<User> users = UserDAO.getAllUsers();
        if (!users.isEmpty()) {
            return users.get(0).getId();
        }
        return -1;
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
} 