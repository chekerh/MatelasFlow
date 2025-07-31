package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImage;
    @FXML private VBox loginPane;

    @FXML
    public void initialize() {
        // Set the logo image (use the white logo for the login screen)
        try {
            Image logo = new Image(getClass().getResource("/images/white-logo.PNG").toExternalForm());
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo image not found: " + e.getMessage());
        }
        // Set the background image for the root pane
        try {
            VBox root = (VBox) logoImage.getScene().getRoot();
            Image bg = new Image(getClass().getResource("/images/background.JPG").toExternalForm());
            BackgroundImage bgi = new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false));
            root.setBackground(new Background(bgi));
        } catch (Exception e) {
            System.out.println("Background image not set: " + e.getMessage());
        }
        // Set margin for the logo image
        VBox.setMargin(logoImage, new javafx.geometry.Insets(0, 0, 12, 0));
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = UserDAO.findByUsername(username);
        System.out.println("DB hash: " + (user != null ? user.getPasswordHash() : "null"));
        System.out.println("Input password: " + password);
        if (user != null && org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPasswordHash())) {
            // Login successful
            ActivityLogger.logActivity(user, ActivityLogger.ActivityType.LOGIN_SUCCESS, 
                "Connexion depuis " + System.getProperty("user.name") + "@" + System.getProperty("os.name"));
            
            errorLabel.setText("");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
                Parent dashboardRoot = loader.load();
                DashboardController dashboardController = loader.getController();
                dashboardController.setUser(user.getUsername(), user.getRole());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(dashboardRoot, 900, 650));
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
} 