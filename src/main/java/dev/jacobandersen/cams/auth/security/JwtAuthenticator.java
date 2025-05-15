package dev.jacobandersen.cams.auth.security;

import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.exception.TokenExpiredException;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public final class JwtAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticator.class);

    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    public JwtAuthenticator(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    public Optional<Authentication> authenticate(String token) {
        final Claims claims;
        try {
            claims = tokenService.validateToken(token, "access");
        } catch (JwtException | InvalidJwtPurposeException | TokenExpiredException ex) {
            logger.info("Failed to validate token", ex);
            return Optional.empty();
        }

        final User user = userService.findUserById(UUID.fromString(claims.getSubject())).orElse(null);
        if (user == null) {
            logger.warn("Valid token presented, but subject refers to non-existent user id {}", claims.getSubject());
            return Optional.empty();
        }

        return Optional.of(UsernamePasswordAuthenticationToken.authenticated(user, claims, user.getAuthorities()));
    }
}
