package com.my.pharmacy.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * DialogUtil — Clean, minimal confirmation dialogs for PharmDesk.
 */
public class DialogUtil {

    private static final String BTN_BASE =
            "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 20px;" +
                    "-fx-border-radius:20px;"+
                    "-fx-padding: 7 7 7 7;" +
                    "-fx-cursor: hand;";

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

        // Clean minimal pane
        dp.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 4);" +
                        "-fx-padding: 8 4 4 4;"
        );

        // Header text — bold, dark
        styleNode(dp.lookup(".header-panel"),
                "-fx-background-color: transparent;"
        );
        styleNode(dp.lookup(".header-panel .label"),
                "-fx-text-fill: #1a1a2e;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 16 4 16;"
        );

        // Divider line under header
        styleNode(dp.lookup(".header-panel"),
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent transparent #e8e8e8 transparent;" +
                        "-fx-border-width: 0 0 1 0;" +
                        "-fx-padding: 0 0 8 0;"
        );

        // Content text — muted grey
        styleNode(dp.lookup(".content.label"),
                "-fx-text-fill: #555555;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 12 16 8 16;"
        );

        // Button bar
        styleNode(dp.lookup(".button-bar"),
                "-fx-background-color: transparent;" +
                        "-fx-padding: 8 16 16 16;"
        );

        Platform.runLater(() -> applyButtonStyles(dp));

        return alert.showAndWait();
    }

    private static void applyButtonStyles(DialogPane dp) {
        dp.lookupAll(".button").forEach(node -> {
            boolean isDefault = node.getStyleClass().contains("default-button");

            String bg      = isDefault ? "#1abc9c" : "transparent";
            String bgHover = isDefault ? "#16a085" : "#f0f0f0";
            String border  = isDefault ? "transparent" : "#cccccc";
            String color   = isDefault ? "white" : "#333333";

            String base = "-fx-background-color: " + bg + ";" +
                    "-fx-text-fill: " + color + ";" +
                    "-fx-border-color: " + border + ";" +
                    "-fx-border-width: 1px;" +
                    BTN_BASE;

            String hovered = "-fx-background-color: " + bgHover + ";" +
                    "-fx-text-fill: " + color + ";" +
                    "-fx-border-color: " + border + ";" +
                    "-fx-border-width: 1px;" +
                    BTN_BASE;

            node.setStyle(base);
            node.setOnMouseEntered(e -> node.setStyle(hovered));
            node.setOnMouseExited(e  -> node.setStyle(base));
        });
    }

    private static void styleNode(Node node, String style) {
        if (node != null) node.setStyle(style);
    }
}