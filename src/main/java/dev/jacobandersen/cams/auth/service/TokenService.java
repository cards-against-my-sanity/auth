package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.exception.SessionMissingOrExpiredException;
import dev.jacobandersen.cams.auth.model.Session;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.repo.SessionRepository;
import dev.jacobandersen.cams.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {
    private final JwtUtil jwtUtil;
    private final SessionRepository sessionRepository;
    private final UserService userService;

    @Autowired
    public TokenService(JwtUtil jwtUtil, SessionRepository sessionRepository, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
    }

    public Claims validateToken(final String token, final String expectPurpose) throws JwtException, InvalidJwtPurposeException {
        return validateTokenWith(token, expectPurpose, null);
    }

    public Claims validateTokenWith(final String token, final String expectPurpose, final String keySalt) throws JwtException, InvalidJwtPurposeException {
        final Claims claims = keySalt == null ? jwtUtil.extractClaims(token) : jwtUtil.extractClaims(token, keySalt);

        if (!claims.get("purpose", String.class).equals(expectPurpose)) {
            throw new InvalidJwtPurposeException(String.format("Provided token does not match expected purpose: %s", expectPurpose));
        }

        final Date expiration = claims.getExpiration();
        if (expiration == null || expiration.after(new Date(System.currentTimeMillis()))) {
            return claims;
        }

        return null;
    }

    public String extractTokenSubjectWithoutValidation(final String token) {
        return jwtUtil.extractSubjectWithoutValidation(token);
    }

    public String createRefreshToken(User user, Session session) {
        return jwtUtil.createRefreshToken(user.getId(), session.getId());
    }

    public String refresh(final String refreshToken, final String type) throws JwtException, InvalidJwtPurposeException, SessionMissingOrExpiredException {
        final Claims claims = jwtUtil.extractClaims(refreshToken);

        if (!claims.get("purpose").equals("refresh")) {
            throw new InvalidJwtPurposeException("Specified token is not a refresh token.");
        }

        User user = userService.findUserById(UUID.fromString(claims.getSubject()))
                .orElseThrow(() -> new IllegalStateException("User does not exist."));

        UUID sessionId = UUID.fromString(claims.get("sid").toString());
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || session.isExpired()) {
            throw new SessionMissingOrExpiredException(String.format("Session %s is missing or expired.", sessionId));
        }

        if (type.equals("access")) {
            return createAccessToken(user);
        } else if (type.equals("websocket")) {
            return createWebsocketToken(user);
        }

        return null;
    }

    public String createAccessToken(User user) {
        return jwtUtil.createAccessToken(user);
    }

    public String createWebsocketToken(User user) {
        return jwtUtil.createWebsocketToken(user);
    }

    public String createConfirmationToken(User user) {
        return Base64.getEncoder().encodeToString(
                jwtUtil.createConfirmationToken(user, Duration.ofMinutes(10))
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createPasswordResetToken(User user) {
        return Base64.getEncoder().encodeToString(
                jwtUtil.createPasswordResetToken(user, Duration.ofMinutes(5))
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
