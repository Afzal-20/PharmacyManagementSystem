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
 * ShortcutManager — Central registry for all keyboard shortcuts.
 *
 * ALL shortcuts are registered once on the main scene in MainController.
 * Screen-specific actions (checkout, add-to-cart, return, backup) are routed
 * through an activeController registry so they always work on the correct screen
 * and never "leak" across screens.
 *
 * Config keys (config.properties):
 *   shortcut.pos=F1             shortcut.inventory=F2
 *   shortcut.purchase=F3        shortcut.sales_history=F4
 *   shortcut.khata=F5           shortcut.customers=F6
 *   shortcut.dealers=F7         shortcut.expiry=F8
 *   shortcut.dashboard=F9       shortcut.checkout=F12
 *   shortcut.add_to_cart=ENTER  shortcut.return=F11
 *   shortcut.backup=F10         shortcut.add_product=INSERT
 */
public class ShortcutManager {

    private static final Logger log = LoggerFactory.getLogger(ShortcutManager.class);

    // ── Active screen controller callbacks ──────────────────────────────────
    // Set by each controller when it is loaded; cleared when another screen loads
    private static Runnable onCheckout    = null;
    private static Runnable onAddToCart   = null;
    private static Runnable onReturn      = null;
    private static Runnable onBackupNow   = null;
    private static Runnable onAddProduct  = null;

    // Global fallback for backup — set once by MainController on startup.
    // Fires from any screen when the Backup screen is not currently active.
    private static Runnable globalBackupFallback = null;

    public static void setCheckoutAction(Runnable r)       { onCheckout          = r; }
    public static void setAddToCartAction(Runnable r)      { onAddToCart         = r; }
    public static void setReturnAction(Runnable r)         { onReturn            = r; }
    public static void setBackupNowAction(Runnable r)      { onBackupNow         = r; }
    public static void setAddProductAction(Runnable r)     { onAddProduct        = r; }
    public static void setGlobalBackupFallback(Runnable r) { globalBackupFallback = r; }

    /** Clear all screen-specific actions (called by MainController on every screen load) */
    public static void clearScreenActions() {
        onCheckout   = null;
        onAddToCart  = null;
        onReturn     = null;
        onBackupNow  = null;
        onAddProduct = null;
    }

    // ── Registration ─────────────────────────────────────────────────────────

    /**
     * Register all shortcuts on the main scene. Called ONCE from MainController.
     * Navigation shortcuts run immediately. Action shortcuts delegate to the
     * currently active screen controller — if that screen is not loaded, nothing happens.
     */
    public static void registerAll(Scene scene, MainControllerActions nav) {
        // Navigation
        register(scene, "shortcut.pos",           "F1",    nav::showPOS);
        register(scene, "shortcut.inventory",     "F2",    nav::showInventory);
        register(scene, "shortcut.purchase",      "F3",    nav::showPurchase);
        register(scene, "shortcut.sales_history", "F4",    nav::showHistory);
        register(scene, "shortcut.khata",         "F5",    nav::showKhata);
        register(scene, "shortcut.customers",     "F6",    nav::showCustomers);
        register(scene, "shortcut.dealers",       "F7",    nav::showDealers);
        register(scene, "shortcut.expiry",        "F8",    nav::showExpiry);
        register(scene, "shortcut.dashboard",     "F9",    nav::showDashboard);

        // Screen-specific actions — always registered globally, routed to active controller
        register(scene, "shortcut.checkout",    "F12",   () -> { if (onCheckout   != null) onCheckout.run(); });
        register(scene, "shortcut.add_to_cart", "ENTER", () -> { if (onAddToCart  != null) onAddToCart.run(); });
        register(scene, "shortcut.return",      "F11",   () -> { if (onReturn     != null) onReturn.run(); });
        register(scene, "shortcut.add_product", "INSERT",() -> { if (onAddProduct != null) onAddProduct.run(); });

        // Backup shortcut — works from every screen:
        // If the Backup screen is active, use its handler (which also refreshes the list).
        // Otherwise fall back to the global backup action set by MainController.
        register(scene, "shortcut.backup", "F10", () -> {
            if (onBackupNow != null) onBackupNow.run();
            else if (globalBackupFallback != null) globalBackupFallback.run();
        });

        log.info("All shortcuts registered on main scene");
    }

    /** Register a single shortcut on a scene. */
    public static void register(Scene scene, String configKey, String defaultKey, Runnable action) {
        KeyCombination combo = resolve(configKey, defaultKey);
        if (combo != null) {
            scene.getAccelerators().put(combo, action);
        }
    }

    /** Resolve a KeyCombination from config with fallback. Supports CTRL+X, ALT+X, plain F1 etc. */
    public static KeyCombination resolve(String configKey, String defaultKey) {
        String keyStr = ConfigUtil.get(configKey, defaultKey).trim().toUpperCase();
        try {
            return parseKeyCombination(keyStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shortcut '{}' for '{}' — falling back to '{}'", keyStr, configKey, defaultKey);
            try {
                return parseKeyCombination(defaultKey.toUpperCase());
            } catch (IllegalArgumentException ex) {
                log.error("Default shortcut '{}' also invalid for '{}'", defaultKey, configKey);
                return null;
            }
        }
    }

    private static KeyCombination parseKeyCombination(String keyStr) {
        if (keyStr.contains("+")) {
            String[] parts = keyStr.split("\\+");
            KeyCode code = KeyCode.valueOf(parts[parts.length - 1].trim());
            KeyCombination.Modifier[] mods = new KeyCombination.Modifier[parts.length - 1];
            for (int i = 0; i < parts.length - 1; i++) {
                mods[i] = switch (parts[i].trim()) {
                    case "CTRL"  -> KeyCombination.CONTROL_DOWN;
                    case "ALT"   -> KeyCombination.ALT_DOWN;
                    case "SHIFT" -> KeyCombination.SHIFT_DOWN;
                    default -> throw new IllegalArgumentException("Unknown modifier: " + parts[i]);
                };
            }
            return new KeyCodeCombination(code, mods);
        }
        return new KeyCodeCombination(KeyCode.valueOf(keyStr));
    }

    /** Interface for MainController navigation callbacks */
    public interface MainControllerActions {
        void showPOS();
        void showInventory();
        void showPurchase();
        void showHistory();
        void showKhata();
        void showCustomers();
        void showDealers();
        void showExpiry();
        void showDashboard();
    }
}