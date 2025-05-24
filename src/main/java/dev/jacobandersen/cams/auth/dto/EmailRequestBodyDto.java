package dev.jacobandersen.cams.auth.dto;

import dev.jacobandersen.cams.auth.api.annotation.Email;

public class EmailRequestBodyDto {
    @Email(message = "Email must be valid")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
