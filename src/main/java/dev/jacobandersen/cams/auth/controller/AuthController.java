package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.constant.CamsAuthConstant;
import dev.jacobandersen.cams.auth.constant.TokenPurpose;
import dev.jacobandersen.cams.auth.dto.in.LogInRequestDto;
import dev.jacobandersen.cams.auth.dto.in.RefreshTokenTypeRequestDto;
import dev.jacobandersen.cams.auth.dto.out.BasicMessageResponseDto;
import dev.jacobandersen.cams.auth.dto.out.BasicTokenResponseDto;
import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.exception.SessionMissingOrExpiredException;
import dev.jacobandersen.cams.auth.exception.TokenExpiredException;
import dev.jacobandersen.cams.auth.model.Session;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.AuthService;
import dev.jacobandersen.cams.auth.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, AuthService authService, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LogInRequestDto dto, @CookieValue(value = CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME, required = false) String maybeAccessToken, @CookieValue(value = CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, required = false) String maybeRefreshToken) {
        logger.info("login: invoke");

        if (maybeAccessToken != null || maybeRefreshToken != null) {
            logger.info("login: fail, potentially already logged in (access token or refresh token already exists, clear cookies!)");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        final Authentication authentication;
        try {
            logger.info("login: attempt authenticate user with username and password");
            authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    dto.getEmail(),
                    dto.getPassword()
            ));
        } catch (BadCredentialsException ignored) {
            logger.info("login: authentication fail, invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BasicMessageResponseDto("Invalid username or password."));
        }

        final User user = (User) authentication.getPrincipal();
        if (user.isBanned()) {
            logger.info("login: fail, banned user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BasicMessageResponseDto("You are banned."));
        } else if (!user.isConfirmed()) {
            logger.info("login: fail, user is not confirmed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BasicMessageResponseDto("Please confirm your account before logging in."));
        }

        logger.info("login: creating session");
        final Session session = authService.createSession(user, dto.isRememberMe());

        logger.info("login: creating access and refresh cookies");
        ResponseCookie accessCookie = ResponseCookie.from(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .value(tokenService.createAccessToken(user))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .value(tokenService.createRefreshToken(user, session))
                .build();

        logger.info("login: ok, sending response");
        return ResponseEntity.ok().headers(headers -> {
            headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }).build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        final Claims claims;
        try {
            claims = tokenService.validateToken(refreshToken, TokenPurpose.REFRESH);
        } catch (JwtException | InvalidJwtPurposeException | TokenExpiredException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(claims.getSubject());
        UUID sessionId = UUID.fromString(claims.get("sid", String.class));
        authService.endSession(userId, sessionId);

        return ResponseEntity.ok().headers(headers -> {
            headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME).maxAge(0).build().toString());
            headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME).maxAge(0).build().toString());
        }).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@CookieValue(value = CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME) String refreshToken, @Valid @RequestBody RefreshTokenTypeRequestDto dto) {
        try {
            if (dto.getType().equals(TokenPurpose.ACCESS)) {
                final ResponseCookie accessCookie = ResponseCookie.from(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME)
                        .httpOnly(true)
                        .sameSite(Cookie.SameSite.LAX.attributeValue())
                        .value(tokenService.refresh(refreshToken, TokenPurpose.ACCESS))
                        .build();

                return ResponseEntity.ok().headers(headers -> headers.set(HttpHeaders.SET_COOKIE, accessCookie.toString())).build();
            } else if (dto.getType().equals(TokenPurpose.WEBSOCKET)) {
                return ResponseEntity.ok().body(new BasicTokenResponseDto(tokenService.refresh(refreshToken, TokenPurpose.WEBSOCKET)));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (InvalidJwtPurposeException ex) {
            return ResponseEntity.badRequest().build();
        } catch (JwtException | SessionMissingOrExpiredException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
