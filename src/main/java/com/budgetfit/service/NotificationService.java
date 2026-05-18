package com.budgetfit.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Service for showing non-blocking toast notifications in the application.
 */
public class NotificationService {

    public enum NotificationType {
        SUCCESS("#3e6560"),
        ERROR("#ba1a1a"),
        INFO("#7d5546");

        private final String color;
        NotificationType(String color) { this.color = color; }
        public String getColor() { return color; }
    }

    public static void show(Stage stage, String message, NotificationType type) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + type.getColor() + ";" +
                       "-fx-text-fill: white;" +
                       "-fx-padding: 10 20;" +
                       "-fx-background-radius: 8;" +
                       "-fx-font-weight: bold;" +
                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);");

        popup.getContent().add(label);

        // Position at the bottom center of the stage
        popup.show(stage);
        popup.setX(stage.getX() + (stage.getWidth() / 2) - (label.getWidth() / 2));
        popup.setY(stage.getY() + stage.getHeight() - 100);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();
    }
}
