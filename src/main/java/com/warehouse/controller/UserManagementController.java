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

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        userTable.setItems(userList);
        loadUsers();
    }

    @FXML
    private void loadUsers() {
        userList.setAll(UserDAO.getAllUsers());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        showUserDialog(null);
    }

    @FXML
    private void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showUserDialog(selected);
        } else {
            errorLabel.setText("No user selected.");
        }
    }

    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (UserDAO.deleteUser(selected.getId())) {
                loadUsers();
            } else {
                errorLabel.setText("Failed to delete user.");
            }
        } else {
            errorLabel.setText("No user selected.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void showUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            Parent dialogRoot = loader.load();
            UserDialogController controller = loader.getController();
            if (user != null) controller.setUser(user);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(user == null ? "Add User" : "Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                User edited = controller.getUser();
                boolean success = user == null ? UserDAO.addUser(edited) : UserDAO.updateUser(edited);
                if (success) {
                    loadUsers();
                } else {
                    errorLabel.setText("Failed to save user.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error opening dialog.");
        }
    }
} 