package com.my.pharmacy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles database backups and restore.
 *
 * Backup folder : C:\ProgramData\PharmDesk\backups\
 * Naming        : pharmacy_backup_YYYY-MM-DD_HH-mm-ss.db
 * Retention     : ALL backups kept (no deletion).
 * Trigger       : App shutdown hook (on close) + manual button on Dashboard.
 */
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final String BACKUP_PREFIX = "pharmacy_backup_";
    private static final String BACKUP_SUFFIX = ".db";
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("App closing — running shutdown backup");
            createBackup();
        }, "backup-shutdown-hook"));
        log.info("Shutdown backup hook registered");
    }

    public static File createBackup() {
        File source = new File(AppPaths.DB_FILE);
        if (!source.exists()) {
            log.error("Backup skipped: database not found at {}", source.getAbsolutePath());
            return null;
        }
        try {
            File backupDir = new File(AppPaths.BACKUPS_DIR);
            if (!backupDir.exists()) backupDir.mkdirs();

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            String fileName = BACKUP_PREFIX + timestamp + BACKUP_SUFFIX;
            File dest = new File(backupDir, fileName);

            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Backup saved: {}", dest.getAbsolutePath());
            return dest;
        } catch (IOException e) {
            log.error("Backup failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static boolean restoreFromFile(File backupFile) {
        if (backupFile == null || !backupFile.exists()) {
            log.warn("Restore skipped: backup file is null or does not exist");
            return false;
        }
        File live = new File(AppPaths.DB_FILE);
        try {
            if (live.exists()) {
                new File(AppPaths.BACKUPS_DIR).mkdirs();
                String safetyName = BACKUP_PREFIX + "pre-restore_" +
                        LocalDateTime.now().format(TIMESTAMP_FMT) + BACKUP_SUFFIX;
                Files.copy(live.toPath(), new File(AppPaths.BACKUPS_DIR, safetyName).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                log.info("Pre-restore safety copy saved: {}", safetyName);
            }
            Files.copy(backupFile.toPath(), live.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Database restored from: {}", backupFile.getName());
            return true;
        } catch (IOException e) {
            log.error("Restore failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public static List<File> listBackups() {
        File dir = new File(AppPaths.BACKUPS_DIR);
        if (!dir.exists()) return Collections.emptyList();
        File[] files = dir.listFiles((d, name) -> name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_SUFFIX));
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());
    }
}
