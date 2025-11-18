package com.warehouse.util;

import java.io.*;
import java.util.Properties;

/**
 * Manages theme preferences persistence.
 * Saves and loads the selected theme to/from a properties file.
 */
public class ThemePreferences {
    private static final String PREFERENCES_FILE = "theme_preferences.properties";
    private static final String THEME_KEY = "selected.theme";
    private static final String DEFAULT_THEME = "Original Layout";
    
    /**
     * Saves the selected theme to the preferences file.
     * @param themeName The name of the theme to save
     */
    public static void saveTheme(String themeName) {
        Properties props = new Properties();
        props.setProperty(THEME_KEY, themeName);
        
        try {
            File prefsFile = getPreferencesFile();
            // Create parent directories if they don't exist
            prefsFile.getParentFile().mkdirs();
            
            try (FileOutputStream out = new FileOutputStream(prefsFile)) {
                props.store(out, "Theme Preferences - MatelasPro");
            }
        } catch (IOException e) {
            System.err.println("Error saving theme preference: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the saved theme from the preferences file.
     * @return The saved theme name, or DEFAULT_THEME if not found
     */
    public static String loadTheme() {
        Properties props = new Properties();
        File prefsFile = getPreferencesFile();
        
        if (!prefsFile.exists()) {
            return DEFAULT_THEME;
        }
        
        try (FileInputStream in = new FileInputStream(prefsFile)) {
            props.load(in);
            String theme = props.getProperty(THEME_KEY, DEFAULT_THEME);
            return theme;
        } catch (IOException e) {
            System.err.println("Error loading theme preference: " + e.getMessage());
            return DEFAULT_THEME;
        }
    }
    
    /**
     * Gets the preferences file path.
     * Uses user's home directory for cross-platform compatibility.
     */
    private static File getPreferencesFile() {
        String userHome = System.getProperty("user.home");
        String appDir = ".matelaspro";
        File appDirFile = new File(userHome, appDir);
        return new File(appDirFile, PREFERENCES_FILE);
    }
    
    /**
     * Resets theme to default (Original Layout).
     */
    public static void resetTheme() {
        saveTheme(DEFAULT_THEME);
    }
}

