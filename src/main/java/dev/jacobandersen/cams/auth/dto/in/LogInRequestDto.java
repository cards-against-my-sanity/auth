package dev.jacobandersen.cams.auth.dto.in;

import dev.jacobandersen.cams.auth.annotation.RFC5322Email;
import dev.jacobandersen.cams.auth.api.HasPassword;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LogInRequestDto implements HasPassword {
    @RFC5322Email(message = "Email must be valid")
    private String email;


    @NotNull(message = "Password must be specified")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "'Remember me' preference must be specified")
    private boolean rememberMe;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
