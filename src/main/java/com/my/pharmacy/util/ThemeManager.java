package com.my.pharmacy.util;

import javafx.scene.Scene;

public class ThemeManager {

    private static boolean isDarkMode = false;

    // Path to CSS files
    private static final String LIGHT_THEME = "/styles/style.css";
    private static final String DARK_THEME = "/styles/dark-theme.css";

    public static void toggleTheme(Scene scene) {
        isDarkMode = !isDarkMode;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        if (isDarkMode) {
            // Load Dark Theme
            scene.getStylesheets().add(ThemeManager.class.getResource(DARK_THEME).toExternalForm());
        } else {
            // Load Light Theme
            scene.getStylesheets().add(ThemeManager.class.getResource(LIGHT_THEME).toExternalForm());
        }
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}