package dev.jacobandersen.cams.auth.dto.in;

import dev.jacobandersen.cams.auth.annotation.PasswordMatches;
import dev.jacobandersen.cams.auth.api.HasConfirmablePassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches(message = "Password and password confirmation must match")
public class SetRootPasswordDto implements HasConfirmablePassword {
    @NotBlank(message = "Password must be specified")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Password confirmation must be specified")
    @Size(min = 8, message = "Password confirmation must be at least 8 characters long")
    private String passwordConfirmation;

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }
}
