package com.warehouse.controller;

import com.warehouse.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class UserDialogController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private User user;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("admin", "employee");
        okButton.getStyleClass().add("button-accent-green");
        cancelButton.getStyleClass().add("button-accent-orange");
        okButton.setOnAction(e -> handleOk());
        cancelButton.setOnAction(e -> handleCancel());

       
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            usernameField.setText(user.getUsername());
            roleComboBox.setValue(user.getRole());
        }
    }

    public User getUser() {
        return user;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void handleOk() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        if (username.isEmpty() || (user == null && password.isEmpty()) || role == null) {
            errorLabel.setText("All fields are required.");
            return;
        }
        String passwordHash = user != null && password.isEmpty() ? user.getPasswordHash() : BCrypt.hashpw(password, BCrypt.gensalt());
        if (user == null) {
            user = new User(0, username, passwordHash, role);
        } else {
            user.setUsername(username);
            user.setPasswordHash(passwordHash);
            user.setRole(role);
        }
        okClicked = true;
        ((Stage) okButton.getScene().getWindow()).close();
    }

    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
} 