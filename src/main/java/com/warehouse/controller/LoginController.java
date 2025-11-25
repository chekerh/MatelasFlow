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
import javafx.application.Platform;

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
        System.out.println("DEBUG: LoginController.initialize() called");
        System.out.println("DEBUG: loginButton reference: " + (loginButton != null ? "NOT NULL" : "NULL"));
        
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
        
        // Ensure login button is enabled and set default button
        if (loginButton != null) {
            loginButton.setDisable(false);
            loginButton.setDefaultButton(true); // Allows Enter key to trigger
            
            // CRITICAL: Explicitly set the action handler programmatically as backup
            // This ensures it works even if FXML binding fails in .exe
            loginButton.setOnAction(e -> {
                System.out.println("DEBUG: Login button clicked (programmatic handler)");
                handleLogin(e);
            });
            
            System.out.println("DEBUG: Login button initialized and enabled");
            System.out.println("DEBUG: Login button onAction handler: " + (loginButton.getOnAction() != null ? "SET" : "NULL"));
        } else {
            System.err.println("ERROR: loginButton is null in initialize()!");
        }
        
        // Add Enter key handler for password field to trigger login
        if (passwordField != null) {
            passwordField.setOnAction(e -> {
                System.out.println("DEBUG: Enter pressed in password field");
                if (loginButton != null && !loginButton.isDisable()) {
                    handleLogin(new ActionEvent());
                } else {
                    System.err.println("ERROR: Button is null or disabled!");
                }
            });
        }
        
        // Add Enter key handler for username field to move to password
        if (usernameField != null) {
            usernameField.setOnAction(e -> {
                if (passwordField != null) {
                    passwordField.requestFocus();
                }
            });
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
        // Debug: Log that button was clicked
        System.out.println("========================================");
        System.out.println("DEBUG: handleLogin called");
        System.out.println("========================================");
        
        // Ensure button exists and is not null
        if (loginButton == null) {
            System.err.println("ERROR: loginButton is null!");
            showErrorAlert("Erreur: Bouton de connexion introuvable");
            return;
        }
        
        // Disable button during login to prevent double-clicking
        loginButton.setDisable(true);
        loginButton.setText("Connexion...");
        
        // Clear previous error
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        
        // Get input values
        String username = (usernameField != null) ? usernameField.getText().trim() : "";
        String password = (passwordField != null) ? passwordField.getText() : "";
        
        System.out.println("DEBUG: Username: " + (username.isEmpty() ? "[EMPTY]" : username));
        System.out.println("DEBUG: Password: " + (password.isEmpty() ? "[EMPTY]" : "[HIDDEN]"));
        
        // Validate input
        if (username.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Veuillez entrer un nom d'utilisateur");
            }
            resetLoginButton();
            return;
        }
        
        if (password.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("Veuillez entrer un mot de passe");
            }
            resetLoginButton();
            return;
        }
        
        // Run login in background thread to prevent UI blocking
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Testing database connection...");
                boolean dbConnected = com.warehouse.model.DBUtil.testConnection();
                
                Platform.runLater(() -> {
                    if (!dbConnected) {
                        if (errorLabel != null) {
                            errorLabel.setText("Erreur de connexion à la base de données. Vérifiez que XAMPP MySQL est démarré.");
                        }
                        resetLoginButton();
                        System.err.println("ERROR: Database connection failed");
                        return;
                    }
                    
                    System.out.println("DEBUG: Database connection OK");
                    
                    try {
                        // Look up user
                        System.out.println("DEBUG: Looking up user: " + username);
                        User user = UserDAO.findByUsername(username);
                        
                        if (user == null) {
                            System.err.println("DEBUG: User not found: " + username);
                            Platform.runLater(() -> {
                                if (errorLabel != null) {
                                    errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect");
                                }
                                resetLoginButton();
                            });
                            return;
                        }
                        
                        System.out.println("DEBUG: User found, checking password...");
                        
                        // Check password
                        boolean passwordValid = BCrypt.checkpw(password, user.getPasswordHash());
                        
                        if (passwordValid) {
                            System.out.println("DEBUG: Login successful!");
                            Platform.runLater(() -> performLogin(user, username));
                        } else {
                            System.err.println("DEBUG: Password incorrect");
                            Platform.runLater(() -> {
                                if (errorLabel != null) {
                                    errorLabel.setText("Nom d'utilisateur ou mot de passe incorrect");
                                }
                                resetLoginButton();
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("ERROR during login: " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            if (errorLabel != null) {
                                errorLabel.setText("Erreur lors de la connexion: " + e.getMessage());
                            }
                            resetLoginButton();
                        });
                    }
                });
            } catch (Exception e) {
                System.err.println("ERROR during database check: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (errorLabel != null) {
                        errorLabel.setText("Erreur de connexion: " + e.getMessage());
                    }
                    resetLoginButton();
                });
            }
        }, "LoginThread").start();
    }
    
    private void resetLoginButton() {
        if (loginButton != null) {
            loginButton.setDisable(false);
            loginButton.setText("Se connecter");
        }
    }
    
    private void performLogin(User user, String username) {
        try {
            // Save username if checkbox is checked
            if (rememberUsernameCheckBox != null && rememberUsernameCheckBox.isSelected()) {
                saveUsername(username);
            } else {
                clearSavedUsername();
            }
            
            ActivityLogger.logActivity(user, ActivityLogger.ActivityType.LOGIN_SUCCESS, 
                "Connexion depuis " + System.getProperty("user.name") + "@" + System.getProperty("os.name"));
            
            if (errorLabel != null) {
                errorLabel.setText("");
            }
            
            // Load dashboard
            System.out.println("DEBUG: Loading dashboard...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setUser(user.getUsername(), user.getRole());
            
            Stage stage = (Stage) ((usernameField != null) ? usernameField.getScene().getWindow() : 
                                   (passwordField != null) ? passwordField.getScene().getWindow() : null);
            
            if (stage == null) {
                System.err.println("ERROR: Could not get stage for dashboard");
                if (errorLabel != null) {
                    errorLabel.setText("Erreur: Impossible de charger le tableau de bord");
                }
                resetLoginButton();
                return;
            }
            
            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(720);
            stage.setWidth(1280);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.setMaximized(true);
            
            // Apply saved theme after scene is set
            Platform.runLater(() -> {
                try {
                    String savedTheme = ThemePreferences.loadTheme();
                    if (savedTheme != null && !savedTheme.isEmpty()) {
                        AdvancedFeaturesController.applyThemeToScene(scene, savedTheme);
                    }
                } catch (Exception e) {
                    System.err.println("Error applying theme: " + e.getMessage());
                }
            });
            
            System.out.println("DEBUG: Dashboard loaded successfully!");
        } catch (Exception e) {
            System.err.println("ERROR loading dashboard: " + e.getMessage());
            e.printStackTrace();
            if (errorLabel != null) {
                errorLabel.setText("Erreur lors du chargement: " + e.getMessage());
            }
            resetLoginButton();
            showErrorAlert("Erreur lors du chargement du tableau de bord: " + e.getMessage());
        }
    }
    
    private void showErrorAlert(String message) {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("Error showing alert: " + e.getMessage());
            }
        });
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
        if (!result.isPresent()) {
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