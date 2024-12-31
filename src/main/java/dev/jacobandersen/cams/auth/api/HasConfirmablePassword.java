package dev.jacobandersen.cams.auth.api;

public interface HasConfirmablePassword extends HasPassword {
    String getPasswordConfirmation();
}
