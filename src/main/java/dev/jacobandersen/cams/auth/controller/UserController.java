package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.dto.ConfirmAccountRequestDto;
import dev.jacobandersen.cams.auth.dto.EmailRequestBodyDto;
import dev.jacobandersen.cams.auth.dto.ResetPasswordRequestDto;
import dev.jacobandersen.cams.auth.dto.SignUpRequestDto;
import dev.jacobandersen.cams.auth.email.ConfirmAccountEmail;
import dev.jacobandersen.cams.auth.email.ForgotPasswordEmail;
import dev.jacobandersen.cams.auth.model.domain.User;
import dev.jacobandersen.cams.auth.model.template.Alert;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {
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
    public String signupForm(@ModelAttribute("signUpRequest") SignUpRequestDto dto, @ModelAttribute("alert") Alert alert) {
        return SIGNUP_PAGE;
    }

    @PostMapping("signup")
    public String signup(@Valid @ModelAttribute("signUpRequest") SignUpRequestDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return SIGNUP_PAGE;
        }

        if (userService.exists(dto.getEmail(), dto.getNickname())) {
            addAlert(model, Alert.error("An account with that email or nickname already exists"));
            return SIGNUP_PAGE;
        }

        final User user = userService.createUser(dto);
        final String confirmationToken = tokenService.createConfirmationToken(user);
        try {
            emailService.sendMail(new ConfirmAccountEmail(user, confirmationToken));
        } catch (MessagingException ex) {
            logger.warn("signup: failed to send confirmation email", ex);
            userService.deleteUser(user);
            addAlert(model, Alert.error("A system error has occurred. Please try again later."));
            return SIGNUP_PAGE;
        }

        addFlashAlert(redirectAttributes, Alert.success("Sign up successful. Please check your email to confirm your account."));
        return REDIRECT_TO_LOGIN_PAGE;
    }

    @GetMapping("confirm")
    public String confirm(@ModelAttribute("confirmAccountRequest") ConfirmAccountRequestDto dto, RedirectAttributes redirectAttributes) {
        if (null == dto.getToken()) {
            addFlashAlert(redirectAttributes, Alert.error("Confirmation token was not provided. Do you need to request a new confirmation email?"));
            return "redirect:/user/resend_confirmation";
        }

        final TokenValidationResult validationResult = tokenValidationService.validateConfirmationToken(dto.getToken());
        if (validationResult.isError()) {
            addFlashAlert(redirectAttributes, Alert.error(validationResult.error()));
            return "redirect:/user/resend_confirmation";
        }

        final User user = validationResult.user();
        if (user.confirmed()) {
            addFlashAlert(redirectAttributes, Alert.info("Your account is already confirmed."));
        } else {
            addFlashAlert(redirectAttributes, Alert.success("Your account has been confirmed. You may now log in."));
            userService.setUserConfirmed(user);
        }

        return REDIRECT_TO_LOGIN_PAGE;
    }

    @GetMapping("resend_confirmation")
    public String resendConfirmationForm(@ModelAttribute("resendConfirmationRequest") EmailRequestBodyDto dto, @ModelAttribute("alert") Alert alert) {
        return "resend_confirmation";
    }

    @PostMapping("resend_confirmation")
    public String resendConfirmation(@Valid @ModelAttribute("resendConfirmationRequest") EmailRequestBodyDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "resend_confirmation";
        }

        final User user = userService.findUserByEmail(dto.getEmail()).orElse(null);
        if (null != user) {
            if (user.confirmed()) {
                addFlashAlert(redirectAttributes, Alert.info("The requested account is already confirmed."));
            } else {
                try {
                    emailService.sendMail(new ConfirmAccountEmail(user, tokenService.createConfirmationToken(user)));
                    addFlashAlert(redirectAttributes, Alert.info("We have sent a new confirmation email. Please check your email to confirm your account."));
                } catch (MessagingException ex) {
                    addAlert(model, Alert.error("A system error has occurred. Please try again later."));
                }
            }
        }

        return REDIRECT_TO_LOGIN_PAGE;
    }

    @GetMapping("reset_password")
    public String resetPasswordForm(@ModelAttribute("resetPasswordRequest") ResetPasswordRequestDto dto, @ModelAttribute("alert") Alert alert, RedirectAttributes redirectAttributes) {
        if (null == dto.getToken()) {
            addFlashAlert(redirectAttributes, Alert.error("Reset password token was not provided. Do you need to request a password reset?"));
            return "redirect:/user/forgot_password";
        }

        return "reset_password";
    }

    @PostMapping("reset_password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequestDto dto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reset_password";
        }

        final TokenValidationResult validationResult = tokenValidationService.validatePasswordResetToken(dto.getToken());
        if (validationResult.isError()) {
            addFlashAlert(redirectAttributes, Alert.error(validationResult.error()));
            return "redirect:/user/forgot_password";
        }

        final User user = validationResult.user();
        userService.updateUserPassword(user, dto.getPassword());

        addFlashAlert(redirectAttributes, Alert.success("Your password has been reset. You may now log in."));
        return "redirect:/login";
    }

    @GetMapping("forgot_password")
    public String forgotPasswordForm(@ModelAttribute("forgotPasswordRequest") EmailRequestBodyDto dto, @ModelAttribute("alert") Alert alert) {
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
                addAlert(model, Alert.error("A system error has occurred. Please try again later."));
            }
        }

        addFlashAlert(redirectAttributes, Alert.info("We have sent you a link to reset your password. Please check your email."));
        return "redirect:/login";
    }
}
