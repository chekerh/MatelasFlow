package com.warehouse.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Properties;

/**
 * Manages theme preferences persistence.
 * Saves and loads the selected theme to/from a properties file.
 */
public class ThemePreferences {
    private static final Logger logger = LoggerFactory.getLogger(ThemePreferences.class);
    private static final String PREFERENCES_FILE = "theme_preferences.properties";
    private static final String THEME_KEY = "selected.theme";
    private static final String DARK_MODE_KEY = "dark.mode.enabled";
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
            logger.error("Error saving theme preference: {}", e.getMessage(), e);
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
            logger.warn("Error loading theme preference: {}", e.getMessage());
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
    
    /**
     * Saves the dark mode state.
     * @param enabled true if dark mode is enabled
     */
    public static void saveDarkMode(boolean enabled) {
        Properties props = new Properties();
        File prefsFile = getPreferencesFile();
        
        // Load existing preferences
        if (prefsFile.exists()) {
            try (FileInputStream in = new FileInputStream(prefsFile)) {
                props.load(in);
            } catch (IOException e) {
                logger.warn("Error loading preferences: {}", e.getMessage());
            }
        }
        
        props.setProperty(DARK_MODE_KEY, String.valueOf(enabled));
        
        try {
            prefsFile.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(prefsFile)) {
                props.store(out, "Theme Preferences - MatelasPro");
            }
        } catch (IOException e) {
            logger.error("Error saving dark mode preference: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Loads the saved dark mode state.
     * @return true if dark mode is enabled, false otherwise
     */
    public static boolean loadDarkMode() {
        Properties props = new Properties();
        File prefsFile = getPreferencesFile();
        
        if (!prefsFile.exists()) {
            return false;
        }
        
        try (FileInputStream in = new FileInputStream(prefsFile)) {
            props.load(in);
            return Boolean.parseBoolean(props.getProperty(DARK_MODE_KEY, "false"));
        } catch (IOException e) {
            logger.warn("Error loading dark mode preference: {}", e.getMessage());
            return false;
        }
    }
}

