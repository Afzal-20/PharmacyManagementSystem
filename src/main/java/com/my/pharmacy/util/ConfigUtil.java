package com.my.pharmacy.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {

    /**
     * Reads the application mode (RETAIL or WHOLESALE) from config.properties.
     */
    public static String getAppMode() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            return props.getProperty("app.mode", "RETAIL");
        } catch (IOException e) {
            System.err.println("⚠️ Could not find config.properties, defaulting to RETAIL.");
            return "RETAIL";
        }
    }
}