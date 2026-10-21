package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.domain.PerfilUsuario;
import com.example.drugstoreAmarante.dto.LoginForm;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.AuthSessionService;
import com.example.drugstoreAmarante.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminAuthController {

    private final UserService userService;
    private final AuthSessionService authSessionService;

    public AdminAuthController(UserService userService, AuthSessionService authSessionService) {
        this.userService = userService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/admin/login")
    public String login(Model model, @RequestParam(required = false) String next) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("next", sanitizeAdminNext(next));
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String loginSubmit(
        @ModelAttribute LoginForm loginForm,
        @RequestParam(required = false) String next,
        HttpServletRequest request,
        HttpSession session
    ) {
        String login = normalizeLogin(loginForm.getEmail());
        User user = userService.authenticate(login, PerfilUsuario.ADMIN, loginForm.getPassword());

        if (user != null) {
            request.changeSessionId();
            authSessionService.login(session, user);
            return "redirect:" + sanitizeAdminNext(next);
        }

        return "redirect:/admin/login?erro=1";
    }

    private String sanitizeAdminNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/admin/")) {
            return "/admin/dashboard";
        }
        return next;
    }

    private String normalizeLogin(String login) {
        return login == null ? null : login.trim();
    }
}
