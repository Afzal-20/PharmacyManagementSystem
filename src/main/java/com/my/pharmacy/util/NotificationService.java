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
        show(message, "rgba(39, 174, 96, 0.88)", "✅");
    }

    public static void error(String message) {
        log.error("ERROR notification: {}", message);
        show(message, "rgba(192, 57, 43, 0.88)", "❌");
    }

    public static void warn(String message) {
        log.warn("WARN notification: {}", message);
        show(message, "rgba(230, 126, 34, 0.88)", "⚠️");
    }

    public static void info(String message) {
        log.info("INFO notification: {}", message);
        show(message, "rgba(41, 128, 185, 0.88)", "ℹ️");
    }

    private static void show(String message, String color, String icon) {
        if (container == null) {
            log.warn("NotificationService container not set — notification dropped: {}", message);
            return;
        }

        Platform.runLater(() -> {
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 14px;");

            Label textLabel = new Label(message);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            textLabel.setWrapText(false);

            HBox toast = new HBox(8, iconLabel, textLabel);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(10, 18, 10, 18));
            toast.setMaxWidth(430);
            toast.setMinWidth(200);
            toast.setMaxHeight(46);
            toast.setMinHeight(46);
            toast.setPrefHeight(46);
            toast.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);"
            );
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