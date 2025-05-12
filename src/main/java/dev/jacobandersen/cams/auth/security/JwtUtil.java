package dev.jacobandersen.cams.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacobandersen.cams.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Component
public class JwtUtil {
    private final String rawSecretKey;
    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtUtil(@Value("${application.security.secret-key}") String rawSecretKey, ObjectMapper objectMapper) {
        this.rawSecretKey = rawSecretKey;
        secretKey = Keys.hmacShaKeyFor(rawSecretKey.getBytes(StandardCharsets.UTF_8));
        this.objectMapper = objectMapper;
    }

    private String createToken(UUID userId, Duration validDuration, String purpose, Map<String, ?> additionalClaims) {
        return createToken(userId, validDuration, purpose, additionalClaims, secretKey);
    }

    private String createToken(UUID userId, Duration validDuration, String purpose, Map<String, ?> additionalClaims, SecretKey secretKey) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(validDuration != null ? new Date(System.currentTimeMillis() + validDuration.toMillis()) : null)
                .claims(additionalClaims != null ? additionalClaims : Collections.emptyMap())
                .claim("purpose", purpose)
                .signWith(secretKey)
                .compact();
    }

    private SecretKey createdSaltedSecretKey(String salt) {
        return Keys.hmacShaKeyFor(String.format("%s%s", rawSecretKey, salt).getBytes(StandardCharsets.UTF_8));
    }

    public String createRefreshToken(UUID userId, UUID sessionId) {
        return createToken(userId, null, "refresh", Map.of("sid", sessionId));
    }

    private String createIdToken(User user, Duration duration, String purpose) {
        return createToken(user.getId(), duration, purpose,
                Map.of("nickname", user.getNickname(),
                        "roles", user.getRoles().stream()
                                .map(role -> String.format("ROLE_%s", role.getName())).toList()
                ));
    }

    public String createAccessToken(User user) {
        return createIdToken(user, Duration.ofMinutes(30), "access");
    }

    public String createWebsocketToken(User user) {
        return createIdToken(user, Duration.ofMinutes(1), "websocket");
    }

    public String createConfirmationToken(User user, Duration duration) {
        return createToken(user.getId(), duration, "confirmation", null);
    }

    public String createPasswordResetToken(User user, Duration duration) {
        return createToken(user.getId(), duration, "password-reset", null, createdSaltedSecretKey(user.getPassword()));
    }

    public String extractSubjectWithoutValidation(String token) {
        int headerEnd = token.indexOf(".");
        int signatureStart = token.lastIndexOf(".");

        String payload = token.substring(headerEnd + 1, signatureStart);
        try {
            return objectMapper.readTree(Base64.getDecoder().decode(payload.getBytes(StandardCharsets.UTF_8))).get("sub").textValue();
        } catch (IOException e) {
            return null;
        }
    }

    public Claims extractClaims(String token) {
        return extractClaims(token, null);
    }

    public Claims extractClaims(String token, String keySalt) throws JwtException {
        return Jwts.parser().verifyWith(keySalt == null ? secretKey : createdSaltedSecretKey(keySalt)).build().parseSignedClaims(token).getPayload();
    }
}
