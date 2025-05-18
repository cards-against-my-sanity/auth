package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.dto.ConfirmAccountRequestDto;
import dev.jacobandersen.cams.auth.dto.EmailRequestBodyDto;
import dev.jacobandersen.cams.auth.dto.ResetPasswordRequestDto;
import dev.jacobandersen.cams.auth.dto.SignUpRequestDto;
import dev.jacobandersen.cams.auth.email.ConfirmAccountEmail;
import dev.jacobandersen.cams.auth.email.ForgotPasswordEmail;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.security.token.TokenValidationResult;
import dev.jacobandersen.cams.auth.service.EmailService;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.TokenValidationService;
import dev.jacobandersen.cams.auth.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final TokenService tokenService;
    private final TokenValidationService tokenValidationService;
    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, TokenService tokenService, TokenValidationService tokenValidationService, EmailService emailService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.tokenValidationService = tokenValidationService;
        this.emailService = emailService;
    }

    @GetMapping("signup")
    public String signupForm(Model model, @ModelAttribute("error") String error) {
        model.addAttribute("error", error);
        model.addAttribute("signUpRequest", new SignUpRequestDto());
        return "signup";
    }

    @PostMapping("signup")
    public String signup(@Valid @ModelAttribute("signUpRequest") SignUpRequestDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "signup";
        }

        if (userService.exists(dto.getEmail(), dto.getNickname())) {
            model.addAttribute("error", "An account with that email or nickname already exists.");
            return "signup";
        }

        final User user = userService.createUser(dto);
        final String confirmationToken = tokenService.createConfirmationToken(user);
        try {
            emailService.sendMail(new ConfirmAccountEmail(user, confirmationToken));
        } catch (MessagingException ex) {
            logger.warn("signup: failed to send confirmation email", ex);
            model.addAttribute("error", "Failed to send confirmation email. Please try again later.");
            return "signup";
        }

        redirectAttributes.addFlashAttribute("message", "Sign up successful. Please check your email to confirm your account.");
        return "redirect:/login";
    }

    @PostMapping("confirm")
    public String confirm(@Valid @ModelAttribute("confirmAccountRequest") ConfirmAccountRequestDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "confirm";
        }

        final TokenValidationResult validationResult = tokenValidationService.validateConfirmationToken(dto.getToken());
        if (validationResult.isError()) {
            model.addAttribute("error", validationResult.error());
            return "confirm";
        }

        final User user = validationResult.user();
        if (!user.isConfirmed()) {
            userService.setUserConfirmed(user);
        }

        redirectAttributes.addFlashAttribute("message", "Your account has been confirmed. You may now log in.");
        return "redirect:/login";
    }

    @GetMapping("resend_confirmation")
    public String resendConfirmationForm(Model model, @ModelAttribute("error") String error) {
        model.addAttribute("error", error);
        return "resend_confirmation";
    }

    @PostMapping("resend_confirmation")
    public String resendConfirmation(@Valid @ModelAttribute("resendConfirmationRequest") EmailRequestBodyDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "resend_confirmation";
        }

        final User user = userService.findUserByEmail(dto.getEmail()).orElse(null);
        if (user != null) {
            if (user.isConfirmed()) {
                // TODO: account already confirmed email, don't reveal to user that account exists for this email
            } else {
                try {
                    emailService.sendMail(new ConfirmAccountEmail(user, tokenService.createConfirmationToken(user)));
                } catch (MessagingException ex) {
                    model.addAttribute("error", "A system error has occurred. Please try again later.");
                }
            }
        }

        redirectAttributes.addFlashAttribute("message", "We have sent a new confirmation email. Please check your email to confirm your account.");
        return "redirect:/login";
    }

    @PostMapping("reset_password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reset_password";
        }

        final TokenValidationResult validationResult = tokenValidationService.validatePasswordResetToken(dto.getToken());
        if (validationResult.isError()) {
            model.addAttribute("error", validationResult.error());
            return "reset_password";
        }

        final User user = validationResult.user();
        userService.updateUserPassword(user, dto.getNewPassword());

        redirectAttributes.addFlashAttribute("message", "Your password has been reset. You may now log in.");
        return "redirect:/login";
    }

    @GetMapping("forgot_password")
    public String forgotPasswordForm(Model model, @ModelAttribute("error") String error) {
        model.addAttribute("error", error);
        return "forgot_password";
    }

    @PostMapping("forgot_password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") EmailRequestBodyDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "forgot_password";
        }

        final User user = userService.findUserByEmail(dto.getEmail()).orElse(null);
        if (user != null) {
            try {
                emailService.sendMail(new ForgotPasswordEmail(user, tokenService.createPasswordResetToken(user)));
            } catch (MessagingException ex) {
                model.addAttribute("error", "A system error has occurred. Please try again later.");
            }
        }

        redirectAttributes.addFlashAttribute("message", "We have sent you a link to reset your password. Please check your email.");
        return "redirect:/login";
    }
}
