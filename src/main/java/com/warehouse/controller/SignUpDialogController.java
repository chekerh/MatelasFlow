package com.warehouse.controller;

import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class SignUpDialogController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private Label feedbackLabel;
    @FXML private Button createAccountButton;

    @FXML
    private void initialize() {
        roleChoiceBox.setItems(FXCollections.observableArrayList("employé", "admin"));
        roleChoiceBox.setValue("employé");
        feedbackLabel.setText("");
    }

    @FXML
    private void handleCreateAccount() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleChoiceBox.getValue();

        if (username.isEmpty()) {
            feedbackLabel.setText("Le nom d'utilisateur est requis.");
            return;
        }
        if (password.isEmpty()) {
            feedbackLabel.setText("Le mot de passe est requis.");
            return;
        }
        if (password.length() < 6) {
            feedbackLabel.setText("Mot de passe trop court (6 caractères min.).");
            return;
        }
        if (!password.equals(confirmPassword)) {
            feedbackLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }
        if (UserDAO.findByUsername(username) != null) {
            feedbackLabel.setText("Ce nom d'utilisateur existe déjà.");
            return;
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(0, username, hash, role);
        boolean created = UserDAO.addUser(newUser);
        if (created) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Compte créé");
            alert.setHeaderText(null);
            alert.setContentText("Le compte a été créé avec succès.");
            alert.showAndWait();
            closeDialog();
        } else {
            feedbackLabel.setText("Échec de la création du compte.");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) createAccountButton.getScene().getWindow();
        stage.close();
    }
}

