package dev.jacobandersen.cams.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class CamsAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        final String errorMessage = switch (exception) {
            case LockedException ignored ->
                    "Your account is not yet confirmed. Please check your email or request a new confirmation code if needed.";
            case DisabledException ignored ->
                    "Your account has been banned. If you think this is in error, please contact the staff team.";
            case BadCredentialsException ignored -> "Your email or password is incorrect. Please try again.";
            default ->
                    "An unknown error has occurred while logging in. Please try again or contact the staff team for assistance.";
        };

        request.getSession().setAttribute("AUTH_FAILURE_ERROR", errorMessage);
        response.sendRedirect("/login");
    }
}
