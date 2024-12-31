package dev.jacobandersen.cams.auth.dto.in;

import dev.jacobandersen.cams.auth.annotation.RFC5322Email;

public class EmailRequestBodyDto {
    @RFC5322Email(message = "Email must be valid")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
