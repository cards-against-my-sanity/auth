package dev.jacobandersen.cams.auth.dto.in;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenTypeRequestDto {
    @NotBlank(message = "Refresh token type must be specified")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
