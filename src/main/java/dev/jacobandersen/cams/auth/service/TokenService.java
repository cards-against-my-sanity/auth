package dev.jacobandersen.cams.auth.service;

import com.nimbusds.jose.JOSEException;
import dev.jacobandersen.cams.auth.model.domain.User;
import dev.jacobandersen.cams.auth.security.token.ConfirmationTokenValidationContext;
import dev.jacobandersen.cams.auth.security.token.JwtUtil;
import dev.jacobandersen.cams.auth.security.token.PasswordResetTokenValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Base64;

@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final JwtUtil jwtUtil;

    @Autowired
    public TokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String createConfirmationToken(final User user) {
        final byte[] token;
        try {
            token = jwtUtil.createConfirmationToken(user, Duration.ofMinutes(10)).getBytes(StandardCharsets.UTF_8);
        } catch (JOSEException ex) {
            logger.error("Failed to create account confirmation token", ex);
            return null;
        }

        return Base64.getEncoder().encodeToString(token);
    }

    public ConfirmationTokenValidationContext validateConfirmationToken(final String encodedToken) {
        final String token = decodeBase64(encodedToken);
        if (null == token) {
            return ConfirmationTokenValidationContext.NULL;
        }

        try {
            return jwtUtil.validateConfirmationToken(token);
        } catch (ParseException | JOSEException ex) {
            logger.error("Failed to validate account confirmation token", ex);
            return ConfirmationTokenValidationContext.NULL;
        }
    }

    public String createPasswordResetToken(final User user) {
        final byte[] token;
        try {
            token = jwtUtil.createPasswordResetToken(user, Duration.ofMinutes(5)).getBytes(StandardCharsets.UTF_8);
        } catch (JOSEException ex) {
            logger.error("Failed to create account password reset token", ex);
            return null;
        }

        return Base64.getEncoder().encodeToString(token);
    }

    public PasswordResetTokenValidationContext validatePasswordResetToken(final String encodedToken) {
        final String token = decodeBase64(encodedToken);
        if (null == token) {
            return PasswordResetTokenValidationContext.NULL;
        }

        try {
            return jwtUtil.validatePasswordResetToken(token);
        } catch (ParseException | JOSEException ex) {
            logger.error("Failed to validate account password reset token", ex);
            return PasswordResetTokenValidationContext.NULL;
        }
    }

    private String decodeBase64(final String encoded) {
        final byte[] tokenBytes;
        try {
            tokenBytes = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            logger.error("Tried to decode non-base64 content", ex);
            return null;
        }

        return new String(tokenBytes, StandardCharsets.UTF_8);
    }
}
