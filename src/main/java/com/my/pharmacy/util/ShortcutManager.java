package com.my.pharmacy.util;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * ShortcutManager — Central registry for all global keyboard shortcuts.
 *
 * Shortcuts are loaded from config.properties so the pharmacy owner can
 * change them without recompiling. All keys are plain KeyCode names
 * (F1, F2, ENTER, etc.) or modifier combos (CTRL+S, ALT+P, etc.).
 *
 * ── CONFIG KEYS ──────────────────────────────────────────────────────────────
 * Navigation:
 *   shortcut.pos           = F1    → Point of Sale
 *   shortcut.inventory     = F2    → Inventory
 *   shortcut.purchase      = F3    → Stock Purchase
 *   shortcut.sales_history = F4    → Sales History
 *   shortcut.khata         = F5    → Khata Ledgers
 *   shortcut.customers     = F6    → Customers
 *   shortcut.dealers       = F7    → Dealers
 *   shortcut.expiry        = F8    → Expiry Management
 *   shortcut.dashboard     = F9    → Dashboard
 *
 * Actions:
 *   shortcut.checkout      = F10   → Complete Sale (POS screen)
 *   shortcut.return        = F11   → Process Return (Sales History screen)
 *   shortcut.backup        = F12   → Backup Now
 *   shortcut.add_product   = CTRL+N → Add Product (Inventory screen)
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Usage:
 *   // In MainController.initialize():
 *   ShortcutManager.registerNavigation(scene, mainController);
 *
 *   // In POSController.initialize():
 *   ShortcutManager.registerAction(scene, "shortcut.checkout", "F10", this::handleCheckout);
 */
public class ShortcutManager {

    private static final Logger log = LoggerFactory.getLogger(ShortcutManager.class);

    // Tracks registered combos per scene to avoid duplicates
    private static final Map<String, KeyCombination> resolvedCombos = new HashMap<>();

    /**
     * Register a single action shortcut on the given scene.
     *
     * @param scene      The JavaFX scene to attach the shortcut to
     * @param configKey  The key in config.properties (e.g. "shortcut.checkout")
     * @param defaultKey The fallback key name if config is missing (e.g. "F10")
     * @param action     The Runnable to invoke when the key is pressed
     */
    public static void register(Scene scene, String configKey, String defaultKey, Runnable action) {
        KeyCombination combo = resolve(configKey, defaultKey);
        if (combo != null) {
            scene.getAccelerators().put(combo, action);
            log.debug("Shortcut registered: {} → {}", configKey, combo);
        }
    }

    /**
     * Resolve a KeyCombination from config, falling back to defaultKey.
     * Supports plain keys (F1, ENTER) and modifier combos (CTRL+S, ALT+P).
     */
    public static KeyCombination resolve(String configKey, String defaultKey) {
        String keyStr = ConfigUtil.get(configKey, defaultKey).trim().toUpperCase();
        if (resolvedCombos.containsKey(configKey)) return resolvedCombos.get(configKey);

        try {
            KeyCombination combo = parseKeyCombination(keyStr);
            resolvedCombos.put(configKey, combo);
            return combo;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shortcut '{}' for key '{}' — falling back to '{}'", keyStr, configKey, defaultKey);
            try {
                KeyCombination fallback = parseKeyCombination(defaultKey.toUpperCase());
                resolvedCombos.put(configKey, fallback);
                return fallback;
            } catch (IllegalArgumentException ex) {
                log.error("Default shortcut '{}' is also invalid for key '{}'", defaultKey, configKey);
                return null;
            }
        }
    }

    /**
     * Returns the display string for a shortcut (e.g. "F1", "Ctrl+N").
     * Used for tooltip hints in the UI.
     */
    public static String getDisplayText(String configKey, String defaultKey) {
        String raw = ConfigUtil.get(configKey, defaultKey).trim();
        // Capitalize nicely: "ctrl+n" → "Ctrl+N"
        String[] parts = raw.split("\\+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("+");
            String p = parts[i].trim();
            sb.append(p.substring(0, 1).toUpperCase()).append(p.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static KeyCombination parseKeyCombination(String keyStr) {
        // Support "CTRL+S", "ALT+P", "SHIFT+F1", or plain "F1"
        if (keyStr.contains("+")) {
            String[] parts = keyStr.split("\\+");
            String keyPart = parts[parts.length - 1].trim();
            KeyCode code = KeyCode.valueOf(keyPart);

            KeyCombination.Modifier[] modifiers = new KeyCombination.Modifier[parts.length - 1];
            for (int i = 0; i < parts.length - 1; i++) {
                modifiers[i] = switch (parts[i].trim()) {
                    case "CTRL"  -> KeyCombination.CONTROL_DOWN;
                    case "ALT"   -> KeyCombination.ALT_DOWN;
                    case "SHIFT" -> KeyCombination.SHIFT_DOWN;
                    default -> throw new IllegalArgumentException("Unknown modifier: " + parts[i]);
                };
            }
            return new KeyCodeCombination(code, modifiers);
        } else {
            return new KeyCodeCombination(KeyCode.valueOf(keyStr));
        }
    }
}
