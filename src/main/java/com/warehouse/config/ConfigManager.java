package com.warehouse.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralised configuration loader.
 * Reads values from src/main/resources/config.properties if present.
 */
public final class ConfigManager {
    private static final String CONFIG_PATH = "/config.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = ConfigManager.class.getResourceAsStream(CONFIG_PATH)) {
            if (is != null) {
                PROPERTIES.load(is);
            }
        } catch (IOException e) {
            System.err.println("Impossible de charger config.properties: " + e.getMessage());
        }
    }

    private ConfigManager() { }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

