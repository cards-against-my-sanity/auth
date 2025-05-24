package dev.jacobandersen.cams.auth.dto;

import dev.jacobandersen.cams.auth.api.HasConfirmablePassword;
import dev.jacobandersen.cams.auth.api.annotation.Email;
import dev.jacobandersen.cams.auth.api.annotation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches(message = "Password and password confirmation must match")
public class SignUpRequestDto implements HasConfirmablePassword {
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Nickname must be specified")
    @Size(min = 3, max = 16, message = "Nickname must be between 3 and 16 characters long")
    private String nickname;

    @NotBlank(message = "Password must be specified")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Password confirmation must be specified")
    @Size(min = 8, message = "Password confirmation must be at least 8 characters long")
    private String passwordConfirmation;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
