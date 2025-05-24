package dev.jacobandersen.cams.auth.dto;

import dev.jacobandersen.cams.auth.api.HasConfirmablePassword;
import dev.jacobandersen.cams.auth.api.annotation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches(message = "Password and password confirmation must match")
public class ResetPasswordRequestDto implements HasConfirmablePassword {
    @NotBlank(message = "Reset password token must be specified")
    private String token;

    @NotBlank(message = "New password must be specified")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "New password confirmation must be specified")
    @Size(min = 8, message = "New password confirmation must be at least 8 characters long")
    private String passwordConfirmation;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }
}
