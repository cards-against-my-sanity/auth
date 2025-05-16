package dev.jacobandersen.cams.auth.dto.in;

import dev.jacobandersen.cams.auth.constant.RefreshTokenTypeEnum;
import jakarta.validation.constraints.NotNull;

public class RefreshTokenTypeRequestDto {
    @NotNull(message = "Refresh token type must be valid")
    private RefreshTokenTypeEnum type;

    public RefreshTokenTypeEnum getType() {
        return type;
    }

    public RefreshTokenTypeRequestDto setType(RefreshTokenTypeEnum type) {
        this.type = type;
        return this;
    }

}
