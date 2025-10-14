package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
import com.warehouse.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionDialogController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<Mattress> mattressComboBox;
    @FXML private TextField quantityField;
    @FXML private ComboBox<StoreOwner> storeOwnerComboBox;
    @FXML private TextField prixField;
    @FXML private VBox lendingFieldsBox;
    @FXML private TextField notesField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private Button addStoreOwnerButton;
    @FXML private Label errorLabel;
    @FXML private DatePicker expectedReturnDatePicker;
    @FXML private HBox destinationBox;
    @FXML private TextField destinationField;

    private Transaction transaction;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Vente", "Transfert", "Prêt", "retour"));
        // Set up mattress ComboBox to show mattress type
        ObservableList<Mattress> mattresses = FXCollections.observableArrayList(MattressDAO.getAllMattresses());
        mattressComboBox.setItems(mattresses);
        mattressComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Mattress item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getType());
            }
        });
        mattressComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Mattress item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getType());
            }
        });
        // Set up store owner ComboBox to show names
        ObservableList<StoreOwner> owners = FXCollections.observableArrayList(StoreOwnerDAO.getAllStoreOwners());
        storeOwnerComboBox.setItems(owners);
        storeOwnerComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(StoreOwner item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        storeOwnerComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StoreOwner item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        okButton.getStyleClass().add("button-accent-green");
        cancelButton.getStyleClass().add("button-accent-orange");
        addStoreOwnerButton.getStyleClass().add("button-accent-purple");
        okButton.setOnAction(e -> handleOk());
        cancelButton.setOnAction(e -> handleCancel());
        addStoreOwnerButton.setOnAction(e -> handleAddStoreOwner());
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFieldsForType());
        updateFieldsForType();
    }

    private void updateFieldsForType() {
        String type = typeComboBox.getValue();
        if ("Prêt".equals(type)) {
            lendingFieldsBox.setVisible(true);
            lendingFieldsBox.setManaged(true);
        } else {
            lendingFieldsBox.setVisible(false);
            lendingFieldsBox.setManaged(false);
        }
        if ("Transfert".equals(type)) {
            destinationBox.setVisible(true);
            destinationBox.setManaged(true);
        } else {
            destinationBox.setVisible(false);
            destinationBox.setManaged(false);
        }
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if (transaction != null) {
            typeComboBox.setValue(transaction.getType());
            // Set mattress selection by id
            for (Mattress m : mattressComboBox.getItems()) {
                if (m.getId() == transaction.getMattressId()) {
                    mattressComboBox.setValue(m);
                    break;
                }
            }
            quantityField.setText(String.valueOf(transaction.getQuantity()));
            // Set store owner selection by id
            if (transaction.getStoreOwnerId() != null) {
                for (StoreOwner s : storeOwnerComboBox.getItems()) {
                    if (s.getId() == transaction.getStoreOwnerId()) {
                        storeOwnerComboBox.setValue(s);
                        break;
                    }
                }
            }
            prixField.setText(String.valueOf(transaction.getPrix()));
            if ("Transfert".equals(transaction.getType())) {
                destinationField.setText(transaction.getNotes());
                notesField.setText("");
            } else {
                notesField.setText(transaction.getNotes());
                destinationField.setText("");
            }
            updateFieldsForType();
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void handleOk() {
        String type = typeComboBox.getValue();
        Mattress mattress = mattressComboBox.getValue();
        String quantityStr = quantityField.getText();
        String prixStr = prixField.getText();
        StoreOwner storeOwner = storeOwnerComboBox.getValue();
        String notes;
        LocalDate expectedReturnDate = expectedReturnDatePicker.getValue();
        if (type == null || mattress == null || quantityStr.isEmpty() || prixStr.isEmpty()) {
            errorLabel.setText("Type, matelas, quantité et prix sont obligatoires.");
            return;
        }
        int quantity;
        double prix;
        try {
            quantity = Integer.parseInt(quantityStr);
            prix = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantité et prix doivent être des nombres.");
            return;
        }
        Integer storeOwnerId = null;
        if ("Prêt".equals(type)) {
            if (storeOwner == null) {
                errorLabel.setText("Propriétaire requis pour le prêt.");
                return;
            }
            if (expectedReturnDate == null) {
                errorLabel.setText("Date de retour prévue requise pour le prêt.");
                return;
            }
            storeOwnerId = storeOwner.getId();
        }
        if ("Transfert".equals(type)) {
            String destination = destinationField.getText();
            if (destination == null || destination.trim().isEmpty()) {
                errorLabel.setText("Lieu de destination requis pour le transfert.");
                return;
            }
            notes = destination;
        } else {
            notes = notesField.getText();
        }
        transaction = new Transaction(
            LocalDateTime.now(),
            mattress.getId(),
            quantity,
            type,
            storeOwnerId,
            0, // userId to be set by caller
            prix,
            notes,
            "Prêt".equals(type) ? expectedReturnDate : null
        );
        okClicked = true;
        ((Stage) okButton.getScene().getWindow()).close();
    }

    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void handleAddStoreOwner() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnerDialog.fxml"));
            Parent dialogRoot = loader.load();
            StoreOwnerDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un propriétaire");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                StoreOwner newOwner = controller.getStoreOwner();
                if (StoreOwnerDAO.addStoreOwner(newOwner)) {
                    ObservableList<StoreOwner> updatedOwners = FXCollections.observableArrayList(StoreOwnerDAO.getAllStoreOwners());
                    storeOwnerComboBox.setItems(updatedOwners);
                    storeOwnerComboBox.setValue(newOwner);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ajout du propriétaire.");
        }
    }
} 