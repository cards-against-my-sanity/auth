package dev.jacobandersen.cams.auth.exception;

public class InvalidJwtPurposeException extends Exception {
    public InvalidJwtPurposeException(String message) {
        super(message);
    }
}
