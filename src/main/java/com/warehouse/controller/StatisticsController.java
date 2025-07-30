package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
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
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Integer> mattressIdColumn;
    @FXML private TableColumn<Transaction, Integer> quantityColumn;
    @FXML private TableColumn<Transaction, Integer> storeOwnerIdColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;
    @FXML private Label errorLabel;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Tous", "Vente", "Transfert", "Prêt", "retour"));
        typeComboBox.setValue("Tous");
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toLocalDate().toString()));
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        mattressIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getMattressId()).asObject());
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        storeOwnerIdColumn.setCellValueFactory(cellData -> cellData.getValue().getStoreOwnerId() == null ? null : new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStoreOwnerId()).asObject());
        notesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNotes()));
        transactionTable.setItems(transactionList);
        filterButton.setOnAction(e -> applyFilter());
        resetButton.setOnAction(e -> resetFilter());
        loadStatistics();
    }

    private void loadStatistics() {
        List<Transaction> all = TransactionDAO.getAllTransactions();
        transactionList.setAll(all);
        int stock = MattressDAO.getAllMattresses().stream().mapToInt(Mattress::getQuantity).sum();
        long sales = all.stream().filter(t -> "Vente".equals(t.getType())).count();
        long returns = all.stream().filter(t -> "retour".equals(t.getType())).count();
        long lends = all.stream().filter(t -> "Prêt".equals(t.getType())).count();
        stockLabel.setText("Stock: " + stock);
        salesLabel.setText("Total ventes: " + sales);
        returnsLabel.setText("Total retours: " + returns);
        lendsLabel.setText("Total prêts: " + lends);
        errorLabel.setText("");
    }

    private void applyFilter() {
        List<Transaction> all = TransactionDAO.getAllTransactions();
        LocalDate date = filterDatePicker.getValue();
        String type = typeComboBox.getValue();
        List<Transaction> filtered = all;
        if (date != null) {
            filtered = filtered.stream().filter(t -> t.getDate().toLocalDate().equals(date)).collect(Collectors.toList());
        }
        if (!"Tous".equals(type)) {
            filtered = filtered.stream().filter(t -> t.getType().equals(type)).collect(Collectors.toList());
        }
        transactionList.setAll(filtered);
    }

    private void resetFilter() {
        filterDatePicker.setValue(null);
        typeComboBox.setValue("Tous");
        loadStatistics();
    }
} 