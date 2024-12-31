package dev.jacobandersen.cams.auth.dto.in;

import jakarta.validation.constraints.NotBlank;

public class ConfirmAccountRequestDto {
    @NotBlank(message = "Confirmation token must be specified")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
