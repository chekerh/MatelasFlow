package com.warehouse.service;

import com.warehouse.controller.AdvancedFeaturesController;
import com.warehouse.util.ThemePreferences;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing application themes and dark mode
 */
public class ThemeService {
    private static final Logger logger = LoggerFactory.getLogger(ThemeService.class);
    
    /**
     * Applies the saved theme preference to a scene
     */
    public static void applySavedTheme(Scene scene) {
        try {
            String savedTheme = ThemePreferences.loadTheme();
            if (savedTheme != null && !savedTheme.isEmpty()) {
                AdvancedFeaturesController.applyThemeToScene(scene, savedTheme);
                logger.debug("Applied saved theme: {}", savedTheme);
            }
        } catch (Exception e) {
            logger.error("Error applying saved theme: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Applies saved theme to a node's scene (with delayed application if scene not ready)
     */
    public static void applySavedThemeToNode(Node node) {
        try {
            String savedTheme = ThemePreferences.loadTheme();
            if (savedTheme != null && !savedTheme.isEmpty()) {
                Scene scene = node.getScene();
                if (scene == null) {
                    // Schedule to apply theme after scene is ready
                    javafx.application.Platform.runLater(() -> {
                        Scene laterScene = node.getScene();
                        if (laterScene != null) {
                            AdvancedFeaturesController.applyThemeToScene(laterScene, savedTheme);
                        }
                    });
                    return;
                }
                AdvancedFeaturesController.applyThemeToScene(scene, savedTheme);
            }
        } catch (Exception e) {
            logger.error("Error applying saved theme to node: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Applies dark mode styling to a node and its children recursively
     */
    public static void applyDarkModeToChildren(Node node, boolean darkMode) {
        if (darkMode) {
            node.getStyleClass().add("dark-mode");
        } else {
            node.getStyleClass().remove("dark-mode");
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                applyDarkModeToChildren(child, darkMode);
            }
        }
    }
    
    /**
     * Applies theme styling to a node based on current dark mode state
     */
    public static void applyThemeToNode(Parent root, boolean isDarkMode) {
        if (isDarkMode) {
            if (!root.getStyleClass().contains("dark-mode")) {
                root.getStyleClass().add("dark-mode");
            }
            applyDarkModeToChildren(root, true);
        } else {
            root.getStyleClass().remove("dark-mode");
            applyDarkModeToChildren(root, false);
        }
    }
    
    /**
     * Toggles dark mode on a scene's root
     */
    public static void toggleDarkModeOnScene(Scene scene, boolean darkMode) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().getStyleClass().removeAll("dark-mode");
            if (darkMode) {
                scene.getRoot().getStyleClass().add("dark-mode");
            }
        }
    }
}

