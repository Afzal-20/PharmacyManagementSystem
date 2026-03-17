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
 *
 * ── SHORTCUT KEYS ────────────────────────────────────────────────────────────
 * shortcut.pos              — Point of Sale           (default: F1)
 * shortcut.inventory        — Inventory               (default: F2)
 * shortcut.purchase         — Stock Purchase          (default: F3)
 * shortcut.sales_history    — Sales History           (default: F4)
 * shortcut.khata            — Khata Ledgers           (default: F5)
 * shortcut.customers        — Customers               (default: F6)
 * shortcut.dealers          — Dealers                 (default: F7)
 * shortcut.expiry           — Expiry Management       (default: F8)
 * shortcut.dashboard        — Dashboard               (default: F9)
 * shortcut.checkout         — Complete sale / POS     (default: F12)
 * shortcut.add_to_cart      — Add to cart / POS       (default: ENTER)
 * shortcut.return           — Process return          (default: F11)
 * shortcut.backup           — Backup now              (default: F10)
 * shortcut.add_product      — Add product dialog      (default: INSERT)
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * To add a new config key:
 *   1. Add defaults.setProperty("your.key", "default") in writeHardcodedDefaults()
 *   2. Add a get("your.key", "default") call wherever you need it
 *   3. That's it — no other files need updating
 */
public class ConfigUtil {

    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    // FIX: use AppPaths.ROOT as single source of truth for the data directory path
    // instead of duplicating the OS detection logic that already lives in AppPaths
    private static final String LIVE_CONFIG_PATH = AppPaths.ROOT + "config.properties";

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
        // Write a clean, human-readable file instead of using Properties.store()
        // which produces random key order and ugly escaped characters.
        String nl = "\r\n"; // Windows line endings so Notepad displays correctly
        String content =
                "# ============================================================" + nl +
                        "#  PharmDesk Configuration" + nl +
                        "#  Edit this file in Notepad, then restart PharmDesk." + nl +
                        "#  Location: C:\\ProgramData\\PharmDesk\\config.properties" + nl +
                        "# ============================================================" + nl +
                        nl +
                        "# -- Pharmacy Details (printed on every invoice) -------------" + nl +
                        "pharmacy.name=YOUR PHARMACY NAME" + nl +
                        "pharmacy.address=Shop No. X, Main Market, City" + nl +
                        "pharmacy.phone=0300-0000000" + nl +
                        nl +
                        "# -- Database ------------------------------------------------" + nl +
                        "db.name=wholesale_pharmacy.db" + nl +
                        nl +
                        "# -- Thermal Printer -----------------------------------------" + nl +
                        "# Copy the exact printer name from Windows > Devices & Printers" + nl +
                        "# Example: printer.name=Black Copper Turbo BC-85AC-G1" + nl +
                        "printer.name=" + nl +
                        nl +
                        "# -- Expiry Warning at POS -----------------------------------" + nl +
                        "# Show a warning when a medicine expires within this many days." + nl +
                        "# Set to 0 to disable the warning completely." + nl +
                        "expiry.warn_days=30" + nl +
                        nl +
                        "# -- Keyboard Shortcuts --------------------------------------" + nl +
                        "# Valid values: F1-F12, A-Z, 0-9, INSERT, DELETE, HOME, END" + nl +
                        "# Modifier combos: CTRL+S, ALT+P, SHIFT+F1, etc." + nl +
                        "# Restart the app after changing any shortcut." + nl +
                        nl +
                        "# Navigation (work from any screen)" + nl +
                        "shortcut.pos=F1" + nl +
                        "shortcut.inventory=F2" + nl +
                        "shortcut.purchase=F3" + nl +
                        "shortcut.sales_history=F4" + nl +
                        "shortcut.khata=F5" + nl +
                        "shortcut.customers=F6" + nl +
                        "shortcut.dealers=F7" + nl +
                        "shortcut.expiry=F8" + nl +
                        "shortcut.dashboard=F9" + nl +
                        nl +
                        "# Actions (screen-specific -- only active on their screen)" + nl +
                        "shortcut.checkout=F12" + nl +
                        "shortcut.add_to_cart=ENTER" + nl +
                        "shortcut.return=F11" + nl +
                        "shortcut.backup=F10" + nl +
                        "shortcut.add_product=INSERT" + nl;

        destination.getParentFile().mkdirs();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new FileOutputStream(destination), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.print(content);
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