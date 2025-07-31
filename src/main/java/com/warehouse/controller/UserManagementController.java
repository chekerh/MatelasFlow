package com.warehouse.controller;

import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        userTable.setItems(userList);
        loadUsers();
    }

    @FXML
    public void loadUsers() {
        userList.setAll(UserDAO.getAllUsers());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserOverlay.fxml"));
            Parent overlayRoot = loader.load();
            UserOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setUserManagementController(this);
            controller.setUser(null); // Add mode
            
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
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Aucun utilisateur sélectionné.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserOverlay.fxml"));
            Parent overlayRoot = loader.load();
            UserOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setUserManagementController(this);
            controller.setUser(selected); // Edit mode
            
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
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (UserDAO.deleteUser(selected.getId())) {
                loadUsers();
            } else {
                errorLabel.setText("Échec de la suppression de l'utilisateur.");
            }
        } else {
            errorLabel.setText("Aucun utilisateur sélectionné.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }
} 