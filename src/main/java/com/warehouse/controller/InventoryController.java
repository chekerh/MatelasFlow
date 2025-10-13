package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
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
    @FXML private TableColumn<Mattress, String> typeColumn;
    @FXML private TableColumn<Mattress, String> sizeColumn;
    @FXML private TableColumn<Mattress, String> brandColumn;
    @FXML private TableColumn<Mattress, Integer> quantityColumn;
    @FXML private TableColumn<Mattress, Double> prixColumn;
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
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sizeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSize()));
        brandColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBrand()));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        prixColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrix()).asObject());
        
        // Format prix column to show 2 decimals with DT currency
        prixColumn.setCellFactory(column -> new TableCell<Mattress, Double>() {
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
            // Log activity
            if (dashboardController != null) {
                User currentUser = UserDAO.findByUsername(dashboardController.getCurrentUser());
                if (currentUser != null) {
                    ActivityLogger.logActivity(currentUser, ActivityLogger.ActivityType.ADD_MATTRESS, 
                        "Ouverture de l'interface d'ajout de matelas");
                }
            }
            
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