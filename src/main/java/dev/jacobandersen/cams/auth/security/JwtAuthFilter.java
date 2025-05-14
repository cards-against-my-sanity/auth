package dev.jacobandersen.cams.auth.security;

import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.exception.TokenExpiredException;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    public JwtAuthFilter(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String token = extractToken(request);
        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final Claims claims;
        try {
            claims = tokenService.validateToken(token, "access");
        } catch (JwtException | InvalidJwtPurposeException | TokenExpiredException ex) {
            filterChain.doFilter(request, response);
            return;
        }

        final User user = userService.findUserById(UUID.fromString(claims.getSubject())).orElse(null);
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, claims, user.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        final Cookie accessTokenCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("access_token")).findFirst().orElse(null);

        if (accessTokenCookie != null) {
            return accessTokenCookie.getValue();
        }

        return null;
    }
}

