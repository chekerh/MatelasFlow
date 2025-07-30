package com.warehouse.controller;

import com.warehouse.model.Mattress;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MattressDialogController {
    @FXML private TextField typeField;
    @FXML private TextField sizeField;
    @FXML private TextField brandField;
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
            brandField.setText(mattress.getBrand());
            quantityField.setText(String.valueOf(mattress.getQuantity()));
            prixField.setText(String.valueOf(mattress.getPrix()));
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
        String brand = brandField.getText();
        String quantityStr = quantityField.getText();
        String prixStr = prixField.getText();
        if (type.isEmpty() || size.isEmpty() || brand.isEmpty() || quantityStr.isEmpty() || prixStr.isEmpty()) {
            errorLabel.setText("Tous les champs sont obligatoires.");
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
        if (mattress == null) {
            mattress = new Mattress(type, size, brand, quantity, prix);
        } else {
            mattress.setType(type);
            mattress.setSize(size);
            mattress.setBrand(brand);
            mattress.setQuantity(quantity);
            mattress.setPrix(prix);
        }
        okClicked = true;
        ((Stage) okButton.getScene().getWindow()).close();
    }

    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
} 