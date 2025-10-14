package com.warehouse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
        
        // Set application title
        primaryStage.setTitle("MatelasPro - Gestion d'Entrepôt STE Habiba");
        
        // Set application icon (taskbar icon)
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/SuperMousse.jpg"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Application icon not found: " + e.getMessage());
        }
        
        // Create scene and set fullscreen
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        
        // Set fullscreen and maximized
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint(""); // Remove "Press ESC to exit fullscreen" message
        
        // Prevent ESC from breaking the layout
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                event.consume(); // Block ESC key
            }
        });
        
        // Listen for fullscreen changes and restore if user exits
        primaryStage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
            if (!isNowFullScreen) {
                // User exited fullscreen (somehow), restore it
                javafx.application.Platform.runLater(() -> {
                    primaryStage.setMaximized(true);
                    primaryStage.setFullScreen(true);
                });
            }
        });
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
