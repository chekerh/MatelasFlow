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
            System.out.println("Application icon not found: " + e.getMessage());
        }
        
        // Create scene and set initial state
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // macOS stability tweaks to avoid NSTrackingRect crashes
        System.setProperty("glass.disableGrab", "true");
        System.setProperty("prism.allowhidpi", "true");
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}
