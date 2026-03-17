package com.my.pharmacy.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NotificationService — Shows non-blocking toast notifications at the
 * bottom of the main window.
 *
 * Usage:
 *   NotificationService.success("Sale saved successfully");
 *   NotificationService.error("Printer not found");
 *   NotificationService.warn("Low stock on Panadol");
 *   NotificationService.info("Backup completed");
 */
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static StackPane container;

    public static void setContainer(StackPane pane) {
        container = pane;
        log.debug("NotificationService container set");
    }

    public static void success(String message) {
        log.info("SUCCESS notification: {}", message);
        show(message, "toast-success", "✅");
    }

    public static void error(String message) {
        log.error("ERROR notification: {}", message);
        show(message, "toast-error", "❌");
    }

    public static void warn(String message) {
        log.warn("WARN notification: {}", message);
        show(message, "toast-warn", "⚠️");
    }

    public static void info(String message) {
        log.info("INFO notification: {}", message);
        show(message, "toast-info", "ℹ️");
    }

    private static void show(String message, String typeClass, String icon) {
        if (container == null) {
            log.warn("NotificationService container not set — notification dropped: {}", message);
            return;
        }

        Platform.runLater(() -> {
            Label iconLabel = new Label(icon);
            iconLabel.getStyleClass().add("toast-icon");

            Label textLabel = new Label(message);
            textLabel.getStyleClass().add("toast-text");
            textLabel.setWrapText(false);

            HBox toast = new HBox(8, iconLabel, textLabel);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(10, 18, 10, 18));
            toast.setMaxWidth(430);
            toast.setMinWidth(200);
            toast.setMaxHeight(46);
            toast.setMinHeight(46);
            toast.setPrefHeight(46);
            toast.getStyleClass().addAll("toast-base", typeClass);
            toast.setOpacity(0);

            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            StackPane.setMargin(toast, new Insets(0, 0, 24, 0));
            container.getChildren().add(toast);

            // Fade in → hold → fade out → remove
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,            new KeyValue(toast.opacityProperty(), 0)),
                    new KeyFrame(Duration.millis(250),     new KeyValue(toast.opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(3),      new KeyValue(toast.opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(3.4),    new KeyValue(toast.opacityProperty(), 0))
            );
            timeline.setOnFinished(e -> container.getChildren().remove(toast));
            timeline.play();
        });
    }
}