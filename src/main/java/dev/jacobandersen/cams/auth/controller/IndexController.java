package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.model.domain.Role;
import dev.jacobandersen.cams.auth.model.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController extends BaseController {
    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        final User user = (User) authentication.getPrincipal();
        model.addAttribute("nickname", user.nickname());
        model.addAttribute("isAdmin", user.roles().stream().map(Role::name).toList().contains("admin"));
        return "index";
    }
}
