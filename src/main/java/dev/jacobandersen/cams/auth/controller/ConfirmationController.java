package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.annotation.RFC5322Email;
import dev.jacobandersen.cams.auth.annotation.WithRateLimit;
import dev.jacobandersen.cams.auth.dto.in.ConfirmAccountRequestDto;
import dev.jacobandersen.cams.auth.dto.in.EmailRequestBodyDto;
import dev.jacobandersen.cams.auth.dto.out.BasicMessageResponseDto;
import dev.jacobandersen.cams.auth.email.ConfirmAccountEmail;
import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.EmailService;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@RestController
public class ConfirmationController {
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Autowired
    public ConfirmationController(UserService userService, TokenService tokenService, EmailService emailService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @PostMapping("/resend_confirmation")
    @WithRateLimit(id = "resend_confirmation")
    public ResponseEntity<Object> resendConfirmation(@Valid @RequestBody EmailRequestBodyDto dto) {
        final User user = userService.findUserByEmail(dto.getEmail()).orElse(null);

        if (user != null) {
            if (user.isConfirmed()) {
                return ResponseEntity.badRequest().body(new BasicMessageResponseDto("Account is already confirmed"));
            }

            try {
                emailService.sendMail(new ConfirmAccountEmail(user, tokenService.createConfirmationToken(user)));
            } catch (MessagingException ex) {
                return ResponseEntity.internalServerError().body(new BasicMessageResponseDto("Failed to send account confirmation email; please try again later."));
            }
        }

        return ResponseEntity.ok().body(new BasicMessageResponseDto("A new confirmation email has been sent (if an account exists.)"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Object> confirm(@Valid @RequestBody ConfirmAccountRequestDto dto) {
        final String decodedToken;
        try {
            decodedToken = new String(Base64.getDecoder().decode(dto.getToken()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new BasicMessageResponseDto("Invalid confirmation token"));
        }

        final Claims claims;
        try {
            claims = tokenService.validateToken(decodedToken, "confirmation");
        } catch (JwtException | InvalidJwtPurposeException ex) {
            return ResponseEntity.badRequest().body(new BasicMessageResponseDto(ex.getMessage()));
        }

        final UUID userId = UUID.fromString(claims.getSubject());
        final User user = userService.findUserById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!user.isConfirmed()) {
            userService.setUserConfirmed(user);
        }

        return ResponseEntity.ok(new BasicMessageResponseDto("Thank you, your account has been confirmed."));
    }
}
