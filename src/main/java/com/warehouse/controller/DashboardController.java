package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.Parent;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button userManagementButton;
    @FXML private StackPane contentPane;
    @FXML private StackPane overlayPane;
    @FXML private ImageView logoImage;
    @FXML private BorderPane root;
    @FXML private VBox navBox;

    private String username;
    private String role;
    private boolean navAnimated = false;

    @FXML
    public void initialize() {
        // Add dashboard class to root
        root.getStyleClass().add("dashboard");
        
        // Set the logo image
        try {
            Image logo = new Image(getClass().getResource("/images/white-logo.PNG").toExternalForm());
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo image not found: " + e.getMessage());
        }
        // Set the background image
        try {
            Image bg = new Image(getClass().getResource("/images/background.JPG").toExternalForm());
            BackgroundImage bgi = new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, false));
            root.setBackground(new Background(bgi));
        } catch (Exception e) {
            System.out.println("Background image not set: " + e.getMessage());
        }
        // Set true fullscreen on load (after scene is shown)
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setFullScreen(true);
        });
    }

    public void setUser(String username, String role) {
        this.username = username;
        this.role = role;
        welcomeLabel.setText("Bienvenue, " + username + " (" + ("admin".equals(role) ? "Administrateur" : "Employé") + ")");
        boolean isAdmin = "admin".equals(role);
        userManagementButton.setVisible(isAdmin);
        userManagementButton.setManaged(isAdmin);
        // Reset navBox to centered on login
        if (navBox != null) {
            navBox.getStyleClass().remove("left");
            if (!navBox.getStyleClass().contains("centered")) navBox.getStyleClass().add("centered");
        }
        navAnimated = false;
        // Set fullscreen and maximized for best compatibility
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setMaximized(true);
            stage.setFullScreen(true);
        });
    }

    // Overlay management methods
    public void showOverlay(Parent overlayContent) {
        overlayPane.getChildren().clear();
        overlayPane.getChildren().add(overlayContent);
        overlayPane.setVisible(true);
        overlayPane.setManaged(true);
    }

    public void hideOverlay() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
        overlayPane.getChildren().clear();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            javafx.scene.Parent loginRoot = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(loginRoot, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateNavBox() {
        if (!navAnimated && navBox != null) {
            navBox.getStyleClass().remove("centered");
            navBox.getStyleClass().add("left");
            navBox.setTranslateX(0); // Ensure no negative offset
            navAnimated = true;
        }
    }

    @FXML
    private void showInventory(ActionEvent event) {
        animateNavBox();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/InventoryView.fxml"));
            javafx.scene.Parent inventoryRoot = loader.load();
            InventoryController inventoryController = loader.getController();
            inventoryController.setDashboardController(this);
            contentPane.getChildren().setAll(inventoryRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTransactions(ActionEvent event) {
        animateNavBox();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/TransactionsView.fxml"));
            javafx.scene.Parent transactionsRoot = loader.load();
            contentPane.getChildren().setAll(transactionsRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStoreOwners(ActionEvent event) {
        animateNavBox();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/StoreOwnersView.fxml"));
            javafx.scene.Parent ownersRoot = loader.load();
            contentPane.getChildren().setAll(ownersRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showReports(ActionEvent event) {
        animateNavBox();
        // TODO: Load reports view into contentPane
    }

    @FXML
    private void showStatistics(ActionEvent event) {
        animateNavBox();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/StatisticsView.fxml"));
            javafx.scene.Parent statsRoot = loader.load();
            contentPane.getChildren().setAll(statsRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserManagement(ActionEvent event) {
        animateNavBox();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/UserManagementView.fxml"));
            javafx.scene.Parent userRoot = loader.load();
            contentPane.getChildren().setAll(userRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 