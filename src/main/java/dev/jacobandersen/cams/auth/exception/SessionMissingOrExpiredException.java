package dev.jacobandersen.cams.auth.exception;

public class SessionMissingOrExpiredException extends Exception {
    public SessionMissingOrExpiredException(String message) {
        super(message);
    }
}
