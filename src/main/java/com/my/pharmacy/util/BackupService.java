package com.my.pharmacy.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles database backups and restore.
 *
 * Backup folder : ./backups/
 * Naming        : pharmacy_backup_YYYY-MM-DD_HH-mm-ss.db
 * Retention     : ALL backups kept (no deletion).
 * Trigger       : App shutdown hook (on close) + manual button on Dashboard.
 */
public class BackupService {

    private static final String BACKUP_DIR    = "backups";
    private static final String BACKUP_PREFIX = "pharmacy_backup_";
    private static final String BACKUP_SUFFIX = ".db";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // ── Shutdown hook registration ─────────────────────────────────────────────

    /**
     * Registers a JVM shutdown hook so a backup is created automatically
     * whenever the application closes (normal exit, window X button, or crash).
     * Call this once from App.start().
     */
    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔒 App closing — running shutdown backup...");
            createBackup();
        }, "backup-shutdown-hook"));
    }

    // ── Core backup logic ──────────────────────────────────────────────────────

    /**
     * Creates a timestamped backup of the live database right now.
     * Safe to call from any thread.
     *
     * @return the created backup File, or null on failure.
     */
    public static File createBackup() {
        String dbPath = ConfigUtil.get("db.name", "wholesale_pharmacy.db");
        File source = new File(dbPath);

        if (!source.exists()) {
            System.err.println("❌ Backup skipped: database not found at " + source.getAbsolutePath());
            return null;
        }

        try {
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) backupDir.mkdirs();

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            String fileName  = BACKUP_PREFIX + timestamp + BACKUP_SUFFIX;
            File dest = new File(backupDir, fileName);

            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Backup saved: " + dest.getAbsolutePath());
            return dest;

        } catch (IOException e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ── Restore ────────────────────────────────────────────────────────────────

    /**
     * Replaces the live database with the given backup file.
     * Before overwriting, saves a "pre-restore" safety copy.
     * The application must be restarted after this call.
     *
     * @param backupFile  the .db file to restore from
     * @return true on success
     */
    public static boolean restoreFromFile(File backupFile) {
        if (backupFile == null || !backupFile.exists()) return false;

        String dbPath = ConfigUtil.get("db.name", "wholesale_pharmacy.db");
        File live = new File(dbPath);

        try {
            // Safety copy before overwriting
            if (live.exists()) {
                new File(BACKUP_DIR).mkdirs();
                String safetyName = BACKUP_PREFIX + "pre-restore_" +
                        LocalDateTime.now().format(TIMESTAMP_FMT) + BACKUP_SUFFIX;
                Files.copy(live.toPath(),
                           new File(BACKUP_DIR, safetyName).toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ Pre-restore safety copy saved: " + safetyName);
            }

            Files.copy(backupFile.toPath(), live.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Database restored from: " + backupFile.getName());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Restore failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Listing ────────────────────────────────────────────────────────────────

    /**
     * Returns all .db files in the backups folder, sorted newest-first.
     */
    public static List<File> listBackups() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) return Collections.emptyList();

        File[] files = dir.listFiles((d, name) ->
                name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_SUFFIX));

        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());
    }
}
