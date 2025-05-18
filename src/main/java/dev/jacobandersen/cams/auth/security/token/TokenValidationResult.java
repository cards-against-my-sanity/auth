package dev.jacobandersen.cams.auth.security.token;

import dev.jacobandersen.cams.auth.model.User;

public record TokenValidationResult(User user, String error) {
    public static TokenValidationResult success(User user) {
        return new TokenValidationResult(user, null);
    }

    public static TokenValidationResult error(String error) {
        return new TokenValidationResult(null, error);
    }

    public boolean isError() {
        return user == null;
    }
}
