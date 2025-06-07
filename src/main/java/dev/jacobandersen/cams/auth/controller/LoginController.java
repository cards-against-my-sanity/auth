package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.model.template.Alert;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class LoginController extends BaseController {
    @GetMapping("/login")
    public String login(Authentication authentication, HttpServletRequest request, Model model, @ModelAttribute("alert") Alert alert) {
        if (isLoggedIn(authentication)) {
            return "redirect:/";
        }

        if (null == alert) {
            var loginError = request.getSession().getAttribute("AUTH_FAILURE_ERROR");
            if (loginError != null) {
                request.getSession().removeAttribute("AUTH_FAILURE_ERROR");
                addAlert(model, Alert.error(loginError.toString()));
            }
        }

        return "login";
    }
}
