package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.LoginForm;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final String SESSION_USER = "usuarioLogado";
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) {
            return "redirect:/login?next=/perfil";
        }

        model.addAttribute("user", user);
        return "perfil";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("user", new User());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@ModelAttribute User user) {
        userService.salvar(user);
        return "redirect:/login?cadastro=ok";
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String next) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("next", sanitizeNext(next));
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(
        @ModelAttribute LoginForm loginForm,
        @RequestParam(required = false) String next,
        HttpSession session
    ) {
        String login = normalizeLogin(loginForm.getEmail());
        User user = userService.findByLogin(login);

        if (user != null && user.getPassword() != null && user.getPassword().equals(loginForm.getPassword())) {
            session.setAttribute(SESSION_USER, user);
            return "redirect:" + sanitizeNext(next);
        }

        return "redirect:/login?erro=1";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private User currentUser(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER);
        return value instanceof User user ? user : null;
    }

    private String sanitizeNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/")) {
            return "/";
        }
        return next;
    }

    private String normalizeLogin(String login) {
        return login == null ? null : login.trim();
    }
}
