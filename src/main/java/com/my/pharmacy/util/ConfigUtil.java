package com.my.pharmacy.util;

import java.io.*;
import java.util.Properties;

public class ConfigUtil {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (Exception e) {
            System.err.println("Warning: Unable to find config.properties. Using default values.");
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "Pharmacy Management System - Configuration");
        } catch (IOException e) {
            System.err.println("Warning: Could not save config.properties — " + e.getMessage());
        }
    }
}