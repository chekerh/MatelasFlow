package com.warehouse.controller;

import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class StoreOwnersController {
    @FXML private TableView<StoreOwner> storeOwnerTable;
    @FXML private TableColumn<StoreOwner, String> nameColumn;
    @FXML private TableColumn<StoreOwner, String> contactColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<StoreOwner> storeOwnerList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        contactColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        storeOwnerTable.setItems(storeOwnerList);
        loadStoreOwners();
    }

    @FXML
    public void loadStoreOwners() {
        storeOwnerList.setAll(StoreOwnerDAO.getAllStoreOwners());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnerOverlay.fxml"));
            Parent overlayRoot = loader.load();
            StoreOwnerOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setStoreOwnersController(this);
            controller.setStoreOwner(null); // Add mode
            
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
        StoreOwner selected = storeOwnerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Aucun propriétaire sélectionné.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnerOverlay.fxml"));
            Parent overlayRoot = loader.load();
            StoreOwnerOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setStoreOwnersController(this);
            controller.setStoreOwner(selected); // Edit mode
            
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
        StoreOwner selected = storeOwnerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (StoreOwnerDAO.deleteStoreOwner(selected.getId())) {
                loadStoreOwners();
            } else {
                errorLabel.setText("Échec de la suppression du propriétaire.");
            }
        } else {
            errorLabel.setText("Aucun propriétaire sélectionné.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadStoreOwners();
    }
} 