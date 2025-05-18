package dev.jacobandersen.cams.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model, @ModelAttribute("message") String message) {
        model.addAttribute("error", request.getSession().getAttribute("AUTH_FAILURE_ERROR"));
        model.addAttribute("message", message);
        return "login";
    }
}
