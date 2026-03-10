package com.my.pharmacy.util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * ConfigUtil — Reads and writes PharmDesk configuration.
 *
 * Live config location (editable by user):
 *   C:\ProgramData\PharmDesk\config.properties
 *
 * On first launch, if the live file does not exist, the default
 * config.properties bundled inside the jar is copied there automatically.
 * After that, only the live file is ever read or written.
 *
 * To change settings after installation, open the file in Notepad:
 *   C:\ProgramData\PharmDesk\config.properties
 */
public class ConfigUtil {

    // Live config path — resolved after AppPaths static block runs
    private static final String LIVE_CONFIG_PATH =
            System.getenv("PROGRAMDATA") != null
                    ? System.getenv("PROGRAMDATA") + File.separator + "PharmDesk" + File.separator + "config.properties"
                    : System.getProperty("user.home")  + File.separator + "PharmDesk" + File.separator + "config.properties";

    private static final Properties properties = new Properties();

    static {
        initialize();
    }

    /**
     * Called once at startup (also called explicitly from App.java to
     * guarantee ordering after AppPaths.initialize() has created the directory).
     */
    public static void initialize() {
        File liveFile = new File(LIVE_CONFIG_PATH);

        // ── Step 1: Copy default if file missing OR empty ──
        if (!liveFile.exists() || liveFile.length() == 0) {
            copyDefaultConfig(liveFile);
        }

        // ── Step 2: Load from the live file ──
        properties.clear();
        try (InputStream input = new FileInputStream(liveFile)) {
            properties.load(input);
            System.out.println("✅ ConfigUtil: Loaded config from: " + liveFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("⚠ ConfigUtil: Could not read live config. Using built-in defaults.");
        }
    }

    /**
     * Copies the default config.properties from inside the jar to the live location.
     * If the resource stream is unavailable (e.g. running from IDE before packaging),
     * falls back to writing hardcoded defaults directly so the file is never blank.
     */
    private static void copyDefaultConfig(File destination) {
        try {
            destination.getParentFile().mkdirs();

            InputStream defaultStream = ConfigUtil.class.getResourceAsStream("/config.properties");

            if (defaultStream != null && defaultStream.available() > 0) {
                // Happy path — resource found in jar, copy it directly
                Files.copy(defaultStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ ConfigUtil: Default config copied to: " + destination.getAbsolutePath());
            } else {
                // Fallback — write hardcoded defaults so the file is never blank
                System.err.println("⚠ ConfigUtil: Resource stream empty or missing. Writing hardcoded defaults.");
                writeHardcodedDefaults(destination);
            }

        } catch (IOException e) {
            System.err.println("❌ ConfigUtil: Failed to create config — " + e.getMessage());
            // Last resort — write defaults directly
            try { writeHardcodedDefaults(destination); } catch (IOException ignored) {}
        }
    }

    /**
     * Writes the default configuration directly to disk.
     * This is the guaranteed fallback — it never produces a blank file.
     */
    private static void writeHardcodedDefaults(File destination) throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty("pharmacy.name",    "YOUR PHARMACY NAME");
        defaults.setProperty("pharmacy.address", "Shop No. X, Street Name, City");
        defaults.setProperty("pharmacy.phone",   "0300-0000000");
        defaults.setProperty("db.name",          "wholesale_pharmacy.db");
        defaults.setProperty("printer.name",     "");

        try (OutputStream out = new FileOutputStream(destination)) {
            defaults.store(out,
                    "PharmDesk Configuration\n" +
                            "# Edit this file in Notepad, then restart PharmDesk.\n" +
                            "# Location: C:\\ProgramData\\PharmDesk\\config.properties");
        }
        System.out.println("✅ ConfigUtil: Hardcoded defaults written to: " + destination.getAbsolutePath());
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    /**
     * Persists a key-value change to the live config file in ProgramData.
     */
    public static void set(String key, String value) {
        properties.setProperty(key, value);
        try (OutputStream out = new FileOutputStream(LIVE_CONFIG_PATH)) {
            properties.store(out, "PharmDesk Configuration — Edit this file in Notepad to change settings.");
        } catch (IOException e) {
            System.err.println("⚠ ConfigUtil: Could not save config — " + e.getMessage());
        }
    }
}