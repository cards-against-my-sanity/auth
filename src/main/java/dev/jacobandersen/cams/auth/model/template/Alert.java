package dev.jacobandersen.cams.auth.model.template;

import org.springframework.lang.NonNull;

public record Alert(@NonNull AlertType type, @NonNull String message) {
    public enum AlertType {
        SUCCESS, ERROR, INFO
    }

    public static Alert success(@NonNull String message) {
        return new Alert(AlertType.SUCCESS, message);
    }

    public static Alert error(@NonNull String message) {
        return new Alert(AlertType.ERROR, message);
    }

    public static Alert info(@NonNull String message) {
        return new Alert(AlertType.INFO, message);
    }
}