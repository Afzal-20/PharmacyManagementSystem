package com.my.pharmacy.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * DialogUtil — Clean, minimal confirmation dialogs for PharmDesk.
 *
 * All visual styling is handled via CSS classes defined in style.css:
 *   .pharmdesk-dialog          — dialog pane
 *   .dialog-btn-primary        — confirm/OK button (with :hover state in CSS)
 *   .dialog-btn-secondary      — cancel button (with :hover state in CSS)
 *
 * The Java hover logic (setOnMouseEntered/Exited) has been removed entirely —
 * CSS :hover pseudo-class handles it cleanly without any Java code.
 */
public class DialogUtil {

    public static boolean confirm(String title, String header, String content) {
        return confirmRaw(title, header, content)
                .filter(bt -> bt == ButtonType.OK)
                .isPresent();
    }

    public static Optional<ButtonType> confirmRaw(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header.isEmpty() ? null : header);
        alert.setContentText(content.isEmpty() ? null : content);
        alert.setGraphic(null);
        alert.initStyle(StageStyle.DECORATED);

        DialogPane dp = alert.getDialogPane();
        dp.getStyleClass().add("pharmdesk-dialog");

        // Apply button style classes after the dialog has been laid out
        Platform.runLater(() -> dp.lookupAll(".button").forEach(node -> {
            boolean isDefault = node.getStyleClass().contains("default-button");
            node.getStyleClass().add(isDefault ? "dialog-btn-primary" : "dialog-btn-secondary");
        }));

        return alert.showAndWait();
    }
}