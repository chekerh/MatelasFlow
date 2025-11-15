package com.warehouse.controller;

import com.warehouse.model.Mattress;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MattressDialogController {
    @FXML private TextField typeField;
    @FXML private TextField sizeField;
    @FXML private TextField referenceField;
    @FXML private TextField unitPriceField;
    @FXML private TextField quantityField;
    @FXML private TextField prixField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private Mattress mattress;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        okButton.getStyleClass().add("button-accent-purple");
        cancelButton.getStyleClass().add("button-accent-orange");
        okButton.setOnAction(e -> handleOk());
        cancelButton.setOnAction(e -> handleCancel());
       
    }

    public void setMattress(Mattress mattress) {
        this.mattress = mattress;
        if (mattress != null) {
            typeField.setText(mattress.getType());
            sizeField.setText(mattress.getSize());
            referenceField.setText(mattress.getReference());
            unitPriceField.setText(String.valueOf(mattress.getUnitPrice()));
            quantityField.setText(String.valueOf(mattress.getQuantity()));
            prixField.setText(String.valueOf(mattress.getSalePrice()));
        }
    }

    public Mattress getMattress() {
        return mattress;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void handleOk() {
        String type = typeField.getText();
        String size = sizeField.getText();
        String reference = referenceField.getText();
        String unitPriceStr = unitPriceField.getText();
        String quantityStr = quantityField.getText();
        String prixStr = prixField.getText();
        if (type.isEmpty() || size.isEmpty() || reference.isEmpty() || unitPriceStr.isEmpty() || quantityStr.isEmpty() || prixStr.isEmpty()) {
            errorLabel.setText("Tous les champs sont obligatoires.");
            return;
        }
        int quantity;
        double prix;
        double unitPrice;
        try {
            unitPrice = Double.parseDouble(unitPriceStr);
            quantity = Integer.parseInt(quantityStr);
            prix = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantité et prix doivent être des nombres.");
            return;
        }
        if (mattress == null) {
            mattress = new Mattress(type, size, reference, quantity, unitPrice, prix);
        } else {
            mattress.setType(type);
            mattress.setSize(size);
            mattress.setReference(reference);
            mattress.setUnitPrice(unitPrice);
            mattress.setQuantity(quantity);
            mattress.setSalePrice(prix);
        }
        okClicked = true;
        ((Stage) okButton.getScene().getWindow()).close();
    }

    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
} 