package com.my.pharmacy.controller;

import com.my.pharmacy.util.BackupService;
import com.my.pharmacy.util.NotificationService;
import com.my.pharmacy.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BackupController {

    @FXML private ListView<String> backupListView;
    @FXML private Label lblStatus;
    @FXML private Button btnRestoreSelected, btnRestoreFromFile;

    private List<File> backupFiles;

    @FXML
    public void initialize() {
        boolean isAdmin = UserSession.getInstance() != null &&
                UserSession.getInstance().getUser().isAdmin();
        btnRestoreSelected.setDisable(!isAdmin);
        btnRestoreFromFile.setDisable(!isAdmin);
        loadBackupList();
    }

    private void loadBackupList() {
        backupFiles = BackupService.listBackups();
        if (backupFiles.isEmpty()) {
            backupListView.setItems(FXCollections.observableArrayList("No backups found yet."));
            lblStatus.setText("No backups exist yet. A backup is created automatically when the app closes.");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy  HH:mm:ss");
        var display = backupFiles.stream()
                .map(f -> String.format("%-52s   %s   (%.1f KB)",
                        f.getName(), sdf.format(new Date(f.lastModified())), f.length() / 1024.0))
                .toList();
        backupListView.setItems(FXCollections.observableArrayList(display));
        lblStatus.setText(backupFiles.size() + " backup file(s) found.");
    }

    @FXML
    private void handleBackupNow() {
        File result = BackupService.createBackup();
        if (result != null) {
            lblStatus.setText("✅ Backup created: " + result.getName());
            NotificationService.success("Backup created: " + result.getName());
            loadBackupList();
        } else {
            lblStatus.setText("❌ Backup failed.");
            NotificationService.error("Backup failed. Make sure the database file exists.");
        }
    }

    @FXML
    private void handleRestoreSelected() {
        int idx = backupListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= backupFiles.size()) {
            NotificationService.warn("Please select a backup from the list first.");
            return;
        }
        confirmAndRestore(backupFiles.get(idx));
    }

    @FXML
    private void handleRestoreFromFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a Database Backup File (.db)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        Stage stage = (Stage) backupListView.getScene().getWindow();
        File chosen = chooser.showOpenDialog(stage);
        if (chosen != null) confirmAndRestore(chosen);
    }

    private void confirmAndRestore(File backupFile) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("⚠️  All current data will be replaced.");
        confirm.setContentText(
                "Restoring from:  " + backupFile.getName() + "\n\n" +
                "Your current database will be saved as a pre-restore safety backup first.\n\n" +
                "The application must be restarted after restore.\n\nAre you sure?"
        );
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            boolean success = BackupService.restoreFromFile(backupFile);
            if (success) {
                NotificationService.success("Database restored. Please restart the application.");
            } else {
                NotificationService.error("Restore failed. Your original database is unchanged.");
            }
            loadBackupList();
        });
    }
}
