package dev.jacobandersen.cams.auth.exception;

public class InvalidJwtPurposeException extends Exception {
    public InvalidJwtPurposeException() {
        this(null);
    }

    public InvalidJwtPurposeException(String message) {
        super(message);
    }
}
