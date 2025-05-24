package dev.jacobandersen.cams.auth.interceptor;

import dev.jacobandersen.cams.auth.util.AuthenticationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class IfNotAuthenticatedRedirectHandlerInterceptor extends RedirectHandlerInterceptor {
    public IfNotAuthenticatedRedirectHandlerInterceptor(String redirectLocation) {
        super(redirectLocation);
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        if (!AuthenticationUtil.isLoggedIn(SecurityContextHolder.getContext().getAuthentication())) {
            response.sendRedirect(getRedirectLocation());
            return false;
        }

        return true;
    }
}
