package dev.jacobandersen.cams.auth.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

public abstract class RedirectHandlerInterceptor implements HandlerInterceptor {
    private final String redirectLocation;

    public RedirectHandlerInterceptor(String redirectLocation) {
        this.redirectLocation = redirectLocation;
    }

    public String getRedirectLocation() {
        return redirectLocation;
    }
}
