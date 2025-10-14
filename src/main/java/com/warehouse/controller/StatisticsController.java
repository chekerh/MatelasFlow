package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
import com.warehouse.model.Transaction;
import com.warehouse.model.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsController {
    @FXML private Label stockLabel;
    @FXML private Label salesLabel;
    @FXML private Label returnsLabel;
    @FXML private Label lendsLabel;
    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Button filterButton;
    @FXML private Button resetButton;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> mattressColumn;
    @FXML private TableColumn<Transaction, Integer> quantityColumn;
    @FXML private TableColumn<Transaction, String> storeOwnerColumn;
    @FXML private TableColumn<Transaction, Double> prixColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;
    @FXML private Label errorLabel;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up type combo box
        typeComboBox.setItems(FXCollections.observableArrayList("Tous", "Vente", "Transfert", "Prêt", "retour"));
        typeComboBox.setValue("Tous");
        
        // Set up table columns
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toLocalDate().toString()));
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        mattressColumn.setCellValueFactory(cellData -> {
            Mattress mattress = MattressDAO.getMattressById(cellData.getValue().getMattressId());
            return new javafx.beans.property.SimpleStringProperty(mattress != null ? mattress.getType() + " (" + mattress.getSize() + ")" : "");
        });
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        storeOwnerColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStoreOwnerId() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            StoreOwner storeOwner = StoreOwnerDAO.getStoreOwnerById(cellData.getValue().getStoreOwnerId());
            return new javafx.beans.property.SimpleStringProperty(storeOwner != null ? storeOwner.getName() : "");
        });
        prixColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrix()).asObject());
        notesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNotes()));
        
        transactionTable.setItems(transactionList);
        
        // Set up button actions
        filterButton.setOnAction(e -> applyFilter());
        resetButton.setOnAction(e -> resetFilter());
        
        // Load initial data
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            List<Transaction> all = TransactionDAO.getAllTransactions();
            transactionList.setAll(all);
            
            // Calculate statistics
            int stock = MattressDAO.getAllMattresses().stream().mapToInt(Mattress::getQuantity).sum();
            long sales = all.stream().filter(t -> "Vente".equals(t.getType())).count();
            long returns = all.stream().filter(t -> "retour".equals(t.getType())).count();
            long lends = all.stream().filter(t -> "Prêt".equals(t.getType())).count();
            
            // Update labels
            stockLabel.setText("Stock: " + stock);
            salesLabel.setText("Ventes: " + sales);
            returnsLabel.setText("Retours: " + returns);
            lendsLabel.setText("Prêts: " + lends);
            
            errorLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    private void applyFilter() {
        try {
            List<Transaction> all = TransactionDAO.getAllTransactions();
            LocalDate date = filterDatePicker.getValue();
            String type = typeComboBox.getValue();
            
            List<Transaction> filtered = all;
            
            if (date != null) {
                filtered = filtered.stream()
                    .filter(t -> t.getDate().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            }
            
            if (!"Tous".equals(type)) {
                filtered = filtered.stream()
                    .filter(t -> t.getType().equals(type))
                    .collect(Collectors.toList());
            }
            
            transactionList.setAll(filtered);
            errorLabel.setText("Filtre appliqué: " + filtered.size() + " transactions trouvées");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'application du filtre: " + e.getMessage());
        }
    }

    private void resetFilter() {
        filterDatePicker.setValue(null);
        typeComboBox.setValue("Tous");
        loadStatistics();
    }
} 