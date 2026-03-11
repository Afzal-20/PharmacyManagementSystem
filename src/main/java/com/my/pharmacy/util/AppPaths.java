package com.my.pharmacy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 * AppPaths — Single source of truth for all PharmDesk filesystem paths.
 *
 * All app data lives under:
 *   C:\ProgramData\PharmDesk\
 *       wholesale_pharmacy.db   ← live database
 *       Invoices\               ← PDF soft copies of every sale invoice
 *       Returns\                ← PDF soft copies of every return receipt
 *       backups\                ← automatic database backups
 *
 * C:\ProgramData\ is writable by all Windows users without UAC elevation,
 * making it the correct location for runtime app data on Windows.
 *
 * On non-Windows systems (dev/testing), falls back to user.home/PharmDesk/
 */
public class AppPaths {
    private static final Logger log = LoggerFactory.getLogger(AppPaths.class);

    /** Root data directory: C:\ProgramData\PharmDesk\ */
    public static final String ROOT;

    public static final String DB_FILE;
    public static final String INVOICES_DIR;
    public static final String RETURNS_DIR;
    public static final String BACKUPS_DIR;

    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // Use PROGRAMDATA env var (handles custom Windows installs correctly)
            String programData = System.getenv("PROGRAMDATA");
            if (programData == null || programData.isEmpty()) {
                programData = "C:\\ProgramData";
            }
            ROOT = programData + File.separator + "PharmDesk" + File.separator;
        } else {
            // Dev / Linux / macOS fallback
            ROOT = System.getProperty("user.home") + File.separator + "PharmDesk" + File.separator;
        }

        DB_FILE      = ROOT + "wholesale_pharmacy.db";
        INVOICES_DIR = ROOT + "Invoices"  + File.separator;
        RETURNS_DIR  = ROOT + "Returns"   + File.separator;
        BACKUPS_DIR  = ROOT + "backups"   + File.separator;
    }

    /**
     * Creates all required directories on first run.
     * Called once from App.java before anything else starts.
     */
    public static void initialize() {
        ensureDir(ROOT);
        ensureDir(INVOICES_DIR);
        ensureDir(RETURNS_DIR);
        ensureDir(BACKUPS_DIR);
        System.out.println("✅ AppPaths: Data directory ready at: " + ROOT);
    }

    /**
     * Returns the full path for a sale invoice PDF.
     * e.g. C:\ProgramData\PharmDesk\Invoices\Invoice_42.pdf
     */
    public static String invoicePath(int saleId) {
        return INVOICES_DIR + "Invoice_" + saleId + ".pdf";
    }

    /**
     * Returns the full path for a reprint invoice PDF.
     * e.g. C:\ProgramData\PharmDesk\Invoices\Reprint_Invoice_42.pdf
     */
    public static String reprintPath(int saleId) {
        return INVOICES_DIR + "Reprint_Invoice_" + saleId + ".pdf";
    }

    /**
     * Returns the full path for a return receipt PDF.
     * e.g. C:\ProgramData\PharmDesk\Returns\Return_Inv_42_1234567890.pdf
     */
    public static String returnReceiptPath(int saleId) {
        return RETURNS_DIR + "Return_Inv_" + saleId + "_" + System.currentTimeMillis() + ".pdf";
    }

    private static void ensureDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("✅ AppPaths: Created directory: " + path);
            } else {
                System.err.println("❌ AppPaths: Failed to create directory: " + path);
            }
        }
    }
}