package com.warehouse.controller;

import com.warehouse.model.StoreOwner;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class StoreOwnerDialogController {
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private StoreOwner storeOwner;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        okButton.getStyleClass().add("button-accent-green");
        cancelButton.getStyleClass().add("button-accent-orange");
        okButton.setOnAction(e -> handleOk());
        cancelButton.setOnAction(e -> handleCancel());
        
            }

    public void setStoreOwner(StoreOwner owner) {
        this.storeOwner = owner;
        if (owner != null) {
            nameField.setText(owner.getName());
            contactField.setText(owner.getContact());
        }
    }

    public StoreOwner getStoreOwner() {
        if (storeOwner == null) {
            storeOwner = new StoreOwner(nameField.getText(), contactField.getText());
        } else {
            storeOwner.setName(nameField.getText());
            storeOwner.setContact(contactField.getText());
        }
        return storeOwner;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void handleOk() {
        if (nameField.getText().isEmpty() || contactField.getText().isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }
        okClicked = true;
        ((Stage) okButton.getScene().getWindow()).close();
    }

    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
} 