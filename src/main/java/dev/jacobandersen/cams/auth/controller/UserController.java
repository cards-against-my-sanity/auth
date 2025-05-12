package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.dto.in.SetRootPasswordDto;
import dev.jacobandersen.cams.auth.dto.in.SignUpRequestDto;
import dev.jacobandersen.cams.auth.email.ConfirmAccountEmail;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.EmailService;
import dev.jacobandersen.cams.auth.service.RoleService;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, TokenService tokenService, EmailService emailService, RoleService roleService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.roleService = roleService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        return ResponseEntity.ok((User) ((UsernamePasswordAuthenticationToken) principal).getDetails());
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody SignUpRequestDto dto) {
        if (userService.exists(dto.getEmail(), dto.getNickname())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        final User user = userService.createUser(dto);
        final String confirmationToken = tokenService.createConfirmationToken(user);

        try {
            emailService.sendMail(new ConfirmAccountEmail(user, confirmationToken));
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Object> setRootPassword(@Valid @RequestBody SetRootPasswordDto dto) {
        if (userService.existsByNickname("root")) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        final SignUpRequestDto signUpRequestDto = new SignUpRequestDto();
        signUpRequestDto.setNickname("root");
        signUpRequestDto.setPassword(dto.getPassword());
        signUpRequestDto.setEmail("root@localhost");

        final User user = userService.createUser(signUpRequestDto);
        userService.setUserRoles(user, roleService.findAll());
        userService.setUserConfirmed(user);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
