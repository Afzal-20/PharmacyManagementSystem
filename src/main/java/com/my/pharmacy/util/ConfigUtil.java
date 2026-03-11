package com.my.pharmacy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * ConfigUtil — Single source of truth for all PharmDesk configuration keys.
 *
 * Live config location (editable by user):
 *   C:\ProgramData\PharmDesk\config.properties
 *
 * ── MASTER KEY LIST ──────────────────────────────────────────────────────────
 * pharmacy.name     — Displayed on all invoices and receipts
 * pharmacy.address  — Displayed on all invoices and receipts
 * pharmacy.phone    — Displayed on all invoices and receipts
 * db.name           — SQLite database filename (default: wholesale_pharmacy.db)
 * printer.name      — Exact Windows printer name for thermal receipts
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * To add a new config key:
 *   1. Add defaults.setProperty("your.key", "default") in writeHardcodedDefaults()
 *   2. Add a get("your.key", "default") call wherever you need it
 *   3. That's it — no other files need updating
 */
public class ConfigUtil {

    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    private static final String LIVE_CONFIG_PATH =
            System.getenv("PROGRAMDATA") != null
                    ? System.getenv("PROGRAMDATA") + File.separator + "PharmDesk" + File.separator + "config.properties"
                    : System.getProperty("user.home") + File.separator + "PharmDesk" + File.separator + "config.properties";

    private static final Properties properties = new Properties();

    static {
        initialize();
    }

    public static void initialize() {
        File liveFile = new File(LIVE_CONFIG_PATH);
        log.info("Config path: {}", liveFile.getAbsolutePath());

        if (!liveFile.exists() || liveFile.length() == 0) {
            log.warn("Config file missing or empty — writing hardcoded defaults");
            copyDefaultConfig(liveFile);
        }

        properties.clear();
        try (InputStream input = new FileInputStream(liveFile)) {
            properties.load(input);
            log.info("Config loaded successfully from {}", liveFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Could not read config file: {}", e.getMessage(), e);
        }
    }

    private static void copyDefaultConfig(File destination) {
        try {
            destination.getParentFile().mkdirs();
            InputStream defaultStream = ConfigUtil.class.getResourceAsStream("/config.properties");
            if (defaultStream != null && defaultStream.available() > 0) {
                Files.copy(defaultStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("Default config copied to {}", destination.getAbsolutePath());
            } else {
                log.warn("Resource stream empty or missing — writing hardcoded defaults");
                writeHardcodedDefaults(destination);
            }
        } catch (IOException e) {
            log.error("Failed to create config: {}", e.getMessage(), e);
            try { writeHardcodedDefaults(destination); } catch (IOException ignored) {}
        }
    }

    /**
     * Hardcoded defaults — THE master list of all config keys.
     * Add new keys here when adding new config options.
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
        log.info("Hardcoded defaults written to {}", destination.getAbsolutePath());
    }

    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        log.debug("Config get: {} = {}", key, value);
        return value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    public static void set(String key, String value) {
        log.info("Config set: {} = {}", key, value);
        properties.setProperty(key, value);
        try (OutputStream out = new FileOutputStream(LIVE_CONFIG_PATH)) {
            properties.store(out, "PharmDesk Configuration — Edit this file in Notepad to change settings.");
            log.info("Config saved to {}", LIVE_CONFIG_PATH);
        } catch (IOException e) {
            log.error("Could not save config: {}", e.getMessage(), e);
        }
    }
}
