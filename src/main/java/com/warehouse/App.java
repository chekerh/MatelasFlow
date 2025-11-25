package com.warehouse;

import com.warehouse.controller.LicenseActivationController;
import com.warehouse.model.DBUtil;
import com.warehouse.security.LicenseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // IMPORTANT: Set icon BEFORE loading the scene for macOS dock
        // Set application icon (dock/taskbar icon) - must be done early for macOS
        setApplicationIcon(primaryStage);
        
        // Check license before showing login
        boolean licenseValid = false;
        try {
            licenseValid = LicenseManager.checkLicense();
            logger.info("License check result: {}", licenseValid);
            // Also log to console for debugging .exe issues
            System.out.println("License check result: " + licenseValid);
            System.out.println("License file path: " + com.warehouse.security.LicenseManager.getLicensePath());
        } catch (Exception e) {
            logger.error("Error checking license: {}", e.getMessage(), e);
            System.err.println("ERROR checking license: " + e.getMessage());
            e.printStackTrace();
            licenseValid = false;
        }
        
        if (!licenseValid) {
            logger.info("No valid license found. Showing activation dialog...");
            System.out.println("No valid license - showing activation dialog");
            showLicenseActivation(primaryStage);
            return;
        }
        
        // License is valid, show login
        logger.info("License valid - showing login view");
        System.out.println("License valid - showing login");
        showLoginView(primaryStage);
        
        logger.info("MatelasPro application started successfully");
    }
    
    /**
     * Shows the license activation dialog
     */
    private void showLicenseActivation(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LicenseActivationView.fxml"));
        Parent root = loader.load();
        
        LicenseActivationController controller = loader.getController();
        controller.setStage(primaryStage);
        
        primaryStage.setTitle("MatelasPro - Activation de licence");
        
        // Prevent closing without license
        primaryStage.setOnCloseRequest(e -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Activation requise");
            alert.setHeaderText(null);
            alert.setContentText("Une clé d'activation valide est requise pour utiliser l'application.");
            alert.showAndWait();
            e.consume(); // Prevent closing
        });
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        logger.info("License activation dialog shown");
    }
    
    /**
     * Shows the login view
     */
    private void showLoginView(Stage primaryStage) throws Exception {
        java.net.URL loginView = App.class.getResource("/fxml/LoginView.fxml");
        if (loginView == null) {
            throw new IllegalStateException("LoginView.fxml resource not found in /fxml");
        }

        FXMLLoader loader = new FXMLLoader(loginView);
        Parent root = loader.load();
        
        // Set application title
        primaryStage.setTitle("MatelasPro - Gestion d'Entrepôt STE Habiba");
        
        // Handle application close - shutdown connection pool
        primaryStage.setOnCloseRequest(e -> {
            logger.info("Application shutting down...");
            System.out.println("Closing application...");
            
            // Shutdown database connections in background thread to avoid blocking
            new Thread(() -> {
                try {
                    DBUtil.shutdown();
                } catch (Exception ex) {
                    logger.error("Error during shutdown: {}", ex.getMessage());
                } finally {
                    Platform.exit();
                    System.exit(0);
                }
            }).start();
        });
        
        // Create scene and set initial state
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        logger.info("Login view shown");
    }
    
    /**
     * Sets the application icon for the dock/taskbar.
     * This must be called early, before showing the stage, especially for macOS.
     */
    private void setApplicationIcon(Stage stage) {
        try {
            // Load the logo image - STE Habiba logo
            java.io.InputStream iconStream = getClass().getResourceAsStream("/images/SuperMousse.jpg");
            if (iconStream != null) {
                // Load the image
                Image icon = new Image(iconStream);
                
                // Clear any existing icons and add the new one
                stage.getIcons().clear();
                stage.getIcons().add(icon);
                
                // For macOS, the icon should now appear in the dock
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    logger.info("Application icon (STE Habiba logo) set for macOS dock");
                } else {
                    logger.info("Application icon (STE Habiba logo) set for taskbar");
                }
            } else {
                logger.warn("Application icon stream is null - icon file not found");
            }
        } catch (Exception e) {
            logger.error("Failed to load application icon (STE Habiba logo): {}", e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        // macOS stability tweaks to avoid NSTrackingRect crashes
        System.setProperty("glass.disableGrab", "true");
        System.setProperty("prism.allowhidpi", "true");
        System.setProperty("prism.text", "t2k");
        
        // Add shutdown hook for graceful connection pool closure
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered - closing connection pool");
            DBUtil.shutdown();
        }));
        
        launch(args);
    }
}
