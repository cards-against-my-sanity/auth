package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.model.template.Alert;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BaseController {
    protected static final String SIGNUP_PAGE = "signup";
    protected static final String REDIRECT_TO_LOGIN_PAGE = "redirect:/login";
    private static final String ALERT_KEY = "alert";

    public final void addAlert(final Model model, final Alert alert) {
        model.addAttribute(ALERT_KEY, alert);
    }

    public final void addFlashAlert(final RedirectAttributes redirectAttributes, final Alert alert) {
        redirectAttributes.addFlashAttribute(ALERT_KEY, alert);
    }

    public final boolean isLoggedIn(final Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @ModelAttribute("alert")
    public Alert alert() {
        return null;
    }
}
