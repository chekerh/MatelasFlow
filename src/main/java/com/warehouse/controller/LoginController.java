package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
import com.warehouse.util.ThemePreferences;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {
    private static final String MASTER_SIGNUP_PASSWORD = "MATELASPRO-ADMIN";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImage;
    @FXML private StackPane rootContainer;
    @FXML private VBox loginPane;
    @FXML private CheckBox rememberUsernameCheckBox;

    @FXML
    public void initialize() {
        // Set the logo image (use the white logo for the login screen)
        try {
            Image logo = new Image(getClass().getResource("/images/SuperMousse.jpg").toExternalForm());
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo image not found: " + e.getMessage());
        }
        // Set the background image for the root pane
        try {
            Image bg = new Image(getClass().getResource("/images/background.jpg").toExternalForm());
            BackgroundImage bgi = new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false));
            if (rootContainer != null) {
                rootContainer.setBackground(new Background(bgi));
            }
        } catch (Exception e) {
            System.out.println("Background image not set: " + e.getMessage());
        }
        // Set margin for the logo image
        if (logoImage != null) {
            VBox.setMargin(logoImage, new javafx.geometry.Insets(0, 0, 12, 0));
        }
        
        // Load saved username if exists
        String savedUsername = loadSavedUsername();
        if (savedUsername != null && !savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberUsernameCheckBox.setSelected(true);
            // Auto-focus password field if username is remembered
            javafx.application.Platform.runLater(() -> passwordField.requestFocus());
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = UserDAO.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            // Login successful - save username if checkbox is checked
            if (rememberUsernameCheckBox.isSelected()) {
                saveUsername(username);
            } else {
                clearSavedUsername();
            }
            
            ActivityLogger.logActivity(user, ActivityLogger.ActivityType.LOGIN_SUCCESS, 
                "Connexion depuis " + System.getProperty("user.name") + "@" + System.getProperty("os.name"));
            
            errorLabel.setText("");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
                Parent dashboardRoot = loader.load();
                DashboardController dashboardController = loader.getController();
                dashboardController.setUser(user.getUsername(), user.getRole());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(dashboardRoot);
                stage.setScene(scene);
                stage.setMinWidth(1200);
                stage.setMinHeight(720);
                stage.setWidth(1280);
                stage.setHeight(800);
                stage.centerOnScreen();
                stage.setMaximized(true);
                
                // Apply saved theme after scene is set
                javafx.application.Platform.runLater(() -> {
                    String savedTheme = ThemePreferences.loadTheme();
                    if (savedTheme != null && !savedTheme.isEmpty()) {
                        AdvancedFeaturesController.applyThemeToScene(scene, savedTheme);
                    }
                });
            } catch (Exception e) {
                errorLabel.setText("Failed to load dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Login failed
            ActivityLogger.logActivity(new User(0, username, "", "employé"), 
                ActivityLogger.ActivityType.LOGIN_FAILED, "Tentative échouée");
            errorLabel.setText("Invalid username or password");
        }
    }
    
    private void saveUsername(String username) {
        try {
            java.io.File prefsFile = getLoginPreferencesFile();
            java.util.Properties props = new java.util.Properties();
            
            // Load existing preferences
            if (prefsFile.exists()) {
                try (java.io.FileInputStream in = new java.io.FileInputStream(prefsFile)) {
                    props.load(in);
                }
            }
            
            props.setProperty("saved.username", username);
            prefsFile.getParentFile().mkdirs();
            
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(prefsFile)) {
                props.store(out, "Login Preferences - MatelasPro");
            }
        } catch (Exception e) {
            // Silently fail - not critical
        }
    }
    
    private String loadSavedUsername() {
        try {
            java.io.File prefsFile = getLoginPreferencesFile();
            if (!prefsFile.exists()) {
                return null;
            }
            
            java.util.Properties props = new java.util.Properties();
            try (java.io.FileInputStream in = new java.io.FileInputStream(prefsFile)) {
                props.load(in);
                return props.getProperty("saved.username", null);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private void clearSavedUsername() {
        try {
            java.io.File prefsFile = getLoginPreferencesFile();
            if (prefsFile.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(prefsFile)) {
                    props.load(in);
                }
                props.remove("saved.username");
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(prefsFile)) {
                    props.store(out, "Login Preferences - MatelasPro");
                }
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    private java.io.File getLoginPreferencesFile() {
        String userHome = System.getProperty("user.home");
        String appDir = ".matelaspro";
        java.io.File appDirFile = new java.io.File(userHome, appDir);
        return new java.io.File(appDirFile, "login_preferences.properties");
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        if (!validateMasterPassword(((Node) event.getSource()).getScene().getWindow())) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUpDialog.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un compte");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir l'inscription.");
        }
    }

    private boolean validateMasterPassword(javafx.stage.Window owner) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe maître");
        dialog.setHeaderText("Autorisation requise");
        dialog.setContentText("Entrez le mot de passe maître pour créer un compte :");
        dialog.initOwner(owner);
        dialog.getEditor().setPromptText("Mot de passe maître");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return false;
        }
        if (!MASTER_SIGNUP_PASSWORD.equals(result.get())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Accès refusé");
            alert.setHeaderText(null);
            alert.setContentText("Mot de passe maître invalide.");
            alert.initOwner(owner);
            alert.showAndWait();
            return false;
        }
        return true;
    }
} 