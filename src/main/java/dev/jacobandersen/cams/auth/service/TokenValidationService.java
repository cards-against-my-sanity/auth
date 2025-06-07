package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.model.domain.User;
import dev.jacobandersen.cams.auth.security.token.TokenValidationContext;
import dev.jacobandersen.cams.auth.security.token.TokenValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenValidationService {
    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public TokenValidationService(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public TokenValidationResult validateConfirmationToken(final String token) {
        return validate(tokenService.validateConfirmationToken(token));
    }

    public TokenValidationResult validatePasswordResetToken(final String token) {
        return validate(tokenService.validatePasswordResetToken(token));
    }

    private TokenValidationResult validate(TokenValidationContext context) {
        final UUID userId = context.getUserId();
        if (null == userId) {
            return TokenValidationResult.error("A system error occurred. Please try requesting a new token.");
        }

        final User user = userService.findUserById(userId).orElse(null);
        if (user == null) {
            return TokenValidationResult.error("The user associated with the provided token could not be found. Please try requesting a new token.");
        }

        if (!context.completeValidation(user)) {
            return TokenValidationResult.error("The provided token was invalid. Is it expired? Please try requesting a new token.");
        }

        return TokenValidationResult.success(user);
    }
}
