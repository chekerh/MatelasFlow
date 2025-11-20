package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.InputValidator;
import org.mindrot.jbcrypt.BCrypt;

public class UserOverlayController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label dialogTitle;
    
    private User user;
    private boolean isEditMode = false;
    private DashboardController dashboardController;
    private UserManagementController userManagementController;
    
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    public void setUserManagementController(UserManagementController userManagementController) {
        this.userManagementController = userManagementController;
    }
    
    public void setUser(User user) {
        this.user = user;
        this.isEditMode = (user != null);
        
        // Set up role combo box
        ObservableList<String> roles = FXCollections.observableArrayList("admin", "employé");
        roleComboBox.setItems(roles);
        
        if (isEditMode && user != null) {
            dialogTitle.setText("Modifier l'utilisateur");
            usernameField.setText(user.getUsername());
            usernameField.setDisable(true); // Don't allow username change in edit mode
            passwordField.setPromptText("Laissez vide pour ne pas changer");
            confirmPasswordField.setPromptText("Laissez vide pour ne pas changer");
            roleComboBox.setValue(user.getRole());
        } else {
            dialogTitle.setText("Ajouter un utilisateur");
            usernameField.clear();
            usernameField.setDisable(false);
            passwordField.clear();
            confirmPasswordField.clear();
            roleComboBox.setValue("employé"); // Default role
        }
    }
    
    @FXML
    private void handleOk() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String role = roleComboBox.getValue();
            
            // Validation
            InputValidator.ValidationResult usernameValidation = InputValidator.validateUsername(username);
            if (!usernameValidation.isValid()) {
                showAlert("Erreur", usernameValidation.getMessage(), AlertType.ERROR);
                return;
            }
            
            if (!isEditMode) {
                InputValidator.ValidationResult passwordValidation = InputValidator.validatePassword(password);
                if (!passwordValidation.isValid()) {
                    showAlert("Erreur", passwordValidation.getMessage(), AlertType.ERROR);
                    return;
                }
                if (passwordValidation.isWarning()) {
                    // Show warning but allow to proceed
                    showAlert("Avertissement", passwordValidation.getMessage(), AlertType.WARNING);
                }
            } else if (!password.isEmpty()) {
                // Validate password if provided during edit
                InputValidator.ValidationResult passwordValidation = InputValidator.validatePassword(password);
                if (!passwordValidation.isValid()) {
                    showAlert("Erreur", passwordValidation.getMessage(), AlertType.ERROR);
                    return;
                }
            }
            
            if (!password.isEmpty() && !password.equals(confirmPassword)) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas.", AlertType.ERROR);
                return;
            }
            
            if (role == null) {
                showAlert("Erreur", "Veuillez sélectionner un rôle.", AlertType.ERROR);
                return;
            }
            
            boolean success;
            if (isEditMode) {
                // Update existing user
                if (!password.isEmpty()) {
                    // Hash new password
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    user.setPasswordHash(hashedPassword);
                }
                user.setRole(role);
                success = UserDAO.updateUser(user);
            } else {
                // Create new user
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                User newUser = new User(username, hashedPassword, role);
                success = UserDAO.addUser(newUser);
            }
            
            if (success) {
                // Refresh the user management table
                if (userManagementController != null) {
                    userManagementController.loadUsers();
                }
                
                // Hide the overlay
                if (dashboardController != null) {
                    dashboardController.hideOverlay();
                }
                
                // Show success notification
                if (dashboardController != null) {
                    dashboardController.showNotification(
                        isEditMode ? "Utilisateur modifié avec succès!" : "Utilisateur ajouté avec succès!", 
                        false
                    );
                }
            } else {
                // Show error notification
                if (dashboardController != null) {
                    dashboardController.showNotification("Échec de l'opération. Le nom d'utilisateur existe peut-être déjà.", true);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (dashboardController != null) {
                dashboardController.showNotification("Une erreur inattendue s'est produite: " + e.getMessage(), true);
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        if (dashboardController != null) {
            dashboardController.hideOverlay();
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 