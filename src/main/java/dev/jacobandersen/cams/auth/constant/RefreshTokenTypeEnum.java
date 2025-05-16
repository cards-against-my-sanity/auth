package dev.jacobandersen.cams.auth.constant;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

/**
 * Represents particular use-cases for a refresh token.
 * <p>
 * Users will either be refreshing their access token or they will be refreshing
 * a websocket token for game server connection.
 */
public enum RefreshTokenTypeEnum {
    ACCESS,
    WEBSOCKET;

    @JsonCreator
    public static RefreshTokenTypeEnum fromString(String value) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
