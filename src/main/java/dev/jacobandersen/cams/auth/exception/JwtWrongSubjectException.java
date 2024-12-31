package dev.jacobandersen.cams.auth.exception;

public class JwtWrongSubjectException extends Exception {
    public JwtWrongSubjectException(String message) {
        super(message);
    }
}
