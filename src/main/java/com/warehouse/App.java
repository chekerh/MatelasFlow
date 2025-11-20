package com.warehouse;

import com.warehouse.model.DBUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        java.net.URL loginView = App.class.getResource("/fxml/LoginView.fxml");
        if (loginView == null) {
            throw new IllegalStateException("LoginView.fxml resource not found in /fxml");
        }

        FXMLLoader loader = new FXMLLoader(loginView);
        Parent root = loader.load();
        
        // Set application title
        primaryStage.setTitle("MatelasPro - Gestion d'Entrepôt STE Habiba");
        
        // Set application icon (taskbar icon)
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/SuperMousse.jpg"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Application icon not found: {}", e.getMessage());
        }
        
        // Handle application close - shutdown connection pool
        primaryStage.setOnCloseRequest(e -> {
            logger.info("Application shutting down...");
            DBUtil.shutdown();
            Platform.exit();
            System.exit(0);
        });
        
        // Create scene and set initial state
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        logger.info("MatelasPro application started successfully");
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
