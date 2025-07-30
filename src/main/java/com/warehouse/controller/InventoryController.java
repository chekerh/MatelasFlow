package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InventoryController {
    @FXML private TableView<Mattress> mattressTable;
    @FXML private TableColumn<Mattress, Integer> idColumn;
    @FXML private TableColumn<Mattress, String> typeColumn;
    @FXML private TableColumn<Mattress, String> sizeColumn;
    @FXML private TableColumn<Mattress, String> brandColumn;
    @FXML private TableColumn<Mattress, Integer> quantityColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<Mattress> mattressList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sizeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSize()));
        brandColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBrand()));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        mattressTable.setItems(mattressList);
        loadMattresses();
    }

    @FXML
    public void loadMattresses() {
        mattressList.setAll(MattressDAO.getAllMattresses());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MattressOverlay.fxml"));
            Parent overlayRoot = loader.load();
            MattressOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setInventoryController(this);
            controller.setMattress(null); // Add mode
            
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
    private void handleEdit() {
        Mattress selected = mattressTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Aucun matelas sélectionné.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MattressOverlay.fxml"));
            Parent overlayRoot = loader.load();
            MattressOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setInventoryController(this);
            controller.setMattress(selected); // Edit mode
            
            // Show the overlay
            if (dashboardController != null) {
                dashboardController.showOverlay(overlayRoot);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ouverture du dialogue de modification.");
        }
    }

    @FXML
    private void handleDelete() {
        Mattress selected = mattressTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (MattressDAO.deleteMattress(selected.getId())) {
                loadMattresses();
            } else {
                errorLabel.setText("Échec de la suppression du matelas.");
            }
        } else {
            errorLabel.setText("Aucun matelas sélectionné.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadMattresses();
    }
} 