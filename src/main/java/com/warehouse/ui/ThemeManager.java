package com.warehouse.ui;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    
    public enum Theme {
        MODERN_BLUE("Modern Blue", "modern-blue"),
        DARK_PURPLE("Dark Purple", "dark-purple"),
        GREEN_NATURE("Green Nature", "green-nature"),
        ORANGE_SUNSET("Orange Sunset", "orange-sunset"),
        PINK_ROSE("Pink Rose", "pink-rose"),
        CORPORATE_GRAY("Corporate Gray", "corporate-gray");
        
        private final String displayName;
        private final String cssClass;
        
        Theme(String displayName, String cssClass) {
            this.displayName = displayName;
            this.cssClass = cssClass;
        }
        
        public String getDisplayName() { return displayName; }
        public String getCssClass() { return cssClass; }
    }
    
    private static Theme currentTheme = Theme.MODERN_BLUE;
    private static Map<Theme, String> themeColors = new HashMap<>();
    
    static {
        // Définir les couleurs pour chaque thème
        themeColors.put(Theme.MODERN_BLUE, "#3498db,#2980b9,#2c3e50,#ecf0f1");
        themeColors.put(Theme.DARK_PURPLE, "#9b59b6,#8e44ad,#2c3e50,#ecf0f1");
        themeColors.put(Theme.GREEN_NATURE, "#27ae60,#229954,#2c3e50,#ecf0f1");
        themeColors.put(Theme.ORANGE_SUNSET, "#e67e22,#d35400,#2c3e50,#ecf0f1");
        themeColors.put(Theme.PINK_ROSE, "#e91e63,#c2185b,#2c3e50,#ecf0f1");
        themeColors.put(Theme.CORPORATE_GRAY, "#95a5a6,#7f8c8d,#2c3e50,#ecf0f1");
    }
    
    /**
     * Applique un thème à une scène
     */
    public static void applyTheme(Scene scene, Theme theme) {
        currentTheme = theme;
        
        // Supprimer tous les thèmes existants
        scene.getRoot().getStyleClass().removeAll(
            "modern-blue", "dark-purple", "green-nature", 
            "orange-sunset", "pink-rose", "corporate-gray"
        );
        
        // Appliquer le nouveau thème
        scene.getRoot().getStyleClass().add(theme.getCssClass());
        
        // Appliquer les couleurs CSS personnalisées
        applyThemeColors(scene, theme);
    }
    
    /**
     * Applique les couleurs du thème
     */
    private static void applyThemeColors(Scene scene, Theme theme) {
        String colors = themeColors.get(theme);
        if (colors != null) {
            String[] colorArray = colors.split(",");
            if (colorArray.length >= 4) {
                String primaryColor = colorArray[0];
                String secondaryColor = colorArray[1];
                String darkColor = colorArray[2];
                String lightColor = colorArray[3];
                
                // Appliquer les couleurs via CSS inline
                String css = String.format(
                    ".root { -fx-primary-color: %s; -fx-secondary-color: %s; -fx-dark-color: %s; -fx-light-color: %s; }",
                    primaryColor, secondaryColor, darkColor, lightColor
                );
                
                scene.getStylesheets().clear();
                scene.getStylesheets().add("css/modern.css");
                scene.getRoot().setStyle(css);
            }
        }
    }
    
    /**
     * Obtient le thème actuel
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Change le thème avec confirmation
     */
    public static void changeTheme(Scene scene, Theme newTheme) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Changer de thème");
        alert.setHeaderText("Changement de thème");
        alert.setContentText("Voulez-vous appliquer le thème '" + newTheme.getDisplayName() + "' ?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            applyTheme(scene, newTheme);
        }
    }
    
    /**
     * Obtient tous les thèmes disponibles
     */
    public static Theme[] getAvailableThemes() {
        return Theme.values();
    }
    
    /**
     * Génère un thème personnalisé
     */
    public static void createCustomTheme(String name, String primaryColor, String secondaryColor) {
        // Ici on pourrait ajouter la logique pour créer des thèmes personnalisés
        System.out.println("Thème personnalisé créé: " + name);
    }
} 