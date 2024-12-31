package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.annotation.WithRateLimit;
import dev.jacobandersen.cams.auth.dto.in.EmailRequestBodyDto;
import dev.jacobandersen.cams.auth.dto.in.ResetPasswordRequestDto;
import dev.jacobandersen.cams.auth.dto.out.BasicMessageResponseDto;
import dev.jacobandersen.cams.auth.email.ForgotPasswordEmail;
import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.EmailService;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@RestController
public class ForgotPasswordController {
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Autowired
    public ForgotPasswordController(UserService userService, TokenService tokenService, EmailService emailService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @PostMapping("/forgot_password")
    @WithRateLimit(id = "forgot_password", limitPeriodSeconds = 300)
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody EmailRequestBodyDto dto) {
        final User user = userService.findUserByEmail(dto.getEmail()).orElse(null);

        if (user != null) {
            try {
                emailService.sendMail(new ForgotPasswordEmail(user, tokenService.createPasswordResetToken(user)));
            } catch (MessagingException ex) {
                return ResponseEntity.internalServerError().body(new BasicMessageResponseDto("Failed to send forgot password email; please try again later."));
            }
        }

        return ResponseEntity.ok().body(new BasicMessageResponseDto("A forgotten password link has been sent (if an account exists.)"));
    }

    @PostMapping("/reset_password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto) {
        final String decodedToken;
        try {
            decodedToken = new String(Base64.getDecoder().decode(URLDecoder.decode(dto.getToken(), StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new BasicMessageResponseDto("Incorrect password reset token format. Please request a new one."));
        }

        final UUID tokenSubject = UUID.fromString(tokenService.extractTokenSubjectWithoutValidation(decodedToken));
        final User user = userService.findUserById(tokenSubject).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            tokenService.validateTokenWith(decodedToken, "password-reset", user.getPassword());
        } catch (ExpiredJwtException ex) {
            return ResponseEntity.badRequest().body(new BasicMessageResponseDto("Password reset token expired. Please request a new one."));
        } catch (JwtException | InvalidJwtPurposeException ex) {
            return ResponseEntity.badRequest().body(new BasicMessageResponseDto("Password reset token failed validation. Please request a new one."));
        }

        userService.updateUserPassword(user, dto.getNewPassword());
        return ResponseEntity.ok().body(new BasicMessageResponseDto("Your password has been reset."));
    }
}
