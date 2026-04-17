package com.warehouse.ui;

import javafx.scene.shape.SVGPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight factory that converts simple SVG files (single path) into SVGPath nodes.
 * Allows us to keep icons as vector assets without additional libraries.
 */
public final class IconFactory {
    private static final Pattern PATH_PATTERN = Pattern.compile("d=\"([^\"]+)\"");
    private static final Map<String, String> PATH_CACHE = new HashMap<>();
    // Fallback path(s) so missing icon resources don't crash the UI.
    // These are sized for a 24x24 viewBox.
    private static final String FALLBACK_PLUS_PATH = "M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z";

    private IconFactory() {}

    public static SVGPath svg(String iconName, double size) {
        String pathContent = PATH_CACHE.computeIfAbsent(iconName, IconFactory::loadPathFromResource);
        SVGPath svgPath = new SVGPath();
        // Defensive: never let a missing/invalid icon crash FXML/controller initialization.
        if (pathContent == null || pathContent.trim().isEmpty()) {
            pathContent = fallbackPath(iconName);
        }
        svgPath.setContent(pathContent);
        svgPath.getStyleClass().add("app-icon");
        double scale = size / 24.0; // assuming viewBox 24
        svgPath.setScaleX(scale);
        svgPath.setScaleY(scale);
        return svgPath;
    }

    private static String loadPathFromResource(String iconName) {
        String resource = "/icons/" + iconName + ".svg";
        try (InputStream is = IconFactory.class.getResourceAsStream(resource)) {
            if (is == null) {
                // Don't throw: UI should keep working even if an icon is missing from resources.
                return fallbackPath(iconName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder svg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    svg.append(line);
                }
                Matcher matcher = PATH_PATTERN.matcher(svg.toString());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            // Don't throw: icons are non-critical.
            return fallbackPath(iconName);
        }
        // No <path> found; fall back.
        return fallbackPath(iconName);
    }

    private static String fallbackPath(String iconName) {
        // Add more fallbacks here if you introduce more icons later.
        if ("plus".equalsIgnoreCase(iconName)) {
            return FALLBACK_PLUS_PATH;
        }
        // Generic fallback: a small square (still visible, harmless).
        return "M6 6H18V18H6Z";
    }
}

