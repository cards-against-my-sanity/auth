package dev.jacobandersen.cams.auth.dto.in;

import dev.jacobandersen.cams.auth.annotation.PasswordMatches;
import dev.jacobandersen.cams.auth.annotation.RFC5322Email;
import dev.jacobandersen.cams.auth.api.HasConfirmablePassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches(message = "Password and password confirmation must match")
public class ResetPasswordRequestDto implements HasConfirmablePassword {
    @NotBlank(message = "Reset password token must be specified")
    private String token;

    @NotBlank(message = "New password must be specified")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "New password confirmation must be specified")
    @Size(min = 8, message = "New password confirmation must be at least 8 characters long")
    private String newPasswordConfirmation;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String getPassword() {
        return getNewPassword();
    }

    public String getNewPasswordConfirmation() {
        return newPasswordConfirmation;
    }

    public void setNewPasswordConfirmation(String newPasswordConfirmation) {
        this.newPasswordConfirmation = newPasswordConfirmation;
    }

    @Override
    public String getPasswordConfirmation() {
        return getNewPasswordConfirmation();
    }
}
