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

    private IconFactory() {}

    public static SVGPath svg(String iconName, double size) {
        String pathContent = PATH_CACHE.computeIfAbsent(iconName, IconFactory::loadPathFromResource);
        SVGPath svgPath = new SVGPath();
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
                throw new IllegalArgumentException("Icon not found: " + resource);
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
            throw new RuntimeException("Unable to load icon: " + iconName, e);
        }
        throw new IllegalStateException("No <path> found in icon: " + iconName);
    }
}

