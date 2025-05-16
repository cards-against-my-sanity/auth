package dev.jacobandersen.cams.auth.security;

import dev.jacobandersen.cams.auth.constant.TokenPurpose;
import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.exception.TokenExpiredException;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticatorTest {
    @InjectMocks
    private JwtAuthenticator jwtAuthenticator;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    private String generatedToken;

    @BeforeEach
    void setUp() {
        generatedToken = UUID.randomUUID().toString();
    }

    @Test
    void testAuthenticateSuccess() throws TokenExpiredException, InvalidJwtPurposeException {
        final UUID uuid = UUID.randomUUID();
        when(tokenService.validateToken(any(), eq(TokenPurpose.ACCESS))).thenReturn(Jwts.claims().subject(uuid.toString()).build());

        final User user = new User();
        user.setId(uuid);
        user.setRoles(Collections.emptyList());
        when(userService.findUserById(uuid)).thenReturn(Optional.of(user));

        final Optional<Authentication> authentication = jwtAuthenticator.authenticate(generatedToken);
        assertTrue(authentication.isPresent());
        assertEquals(user, authentication.get().getPrincipal());
    }

    @Test
    void testAuthenticateInvalidToken() throws TokenExpiredException, InvalidJwtPurposeException {
        when(tokenService.validateToken(any(), eq(TokenPurpose.ACCESS))).thenThrow(new JwtException("JwtAuthenticatorTest: JwtException"));

        final Optional<Authentication> authentication = jwtAuthenticator.authenticate(generatedToken);
        assertTrue(authentication.isEmpty());
    }

    @Test
    void testAuthenticateInvalidUser() throws TokenExpiredException, InvalidJwtPurposeException {
        final UUID uuid = UUID.randomUUID();
        when(tokenService.validateToken(any(), eq(TokenPurpose.ACCESS))).thenReturn(Jwts.claims().subject(uuid.toString()).build());
        when(userService.findUserById(uuid)).thenReturn(Optional.empty());

        final Optional<Authentication> authentication = jwtAuthenticator.authenticate(generatedToken);
        assertTrue(authentication.isEmpty());
    }
}
