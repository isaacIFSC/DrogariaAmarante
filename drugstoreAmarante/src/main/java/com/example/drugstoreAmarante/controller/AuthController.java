package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.domain.PerfilUsuario;
import com.example.drugstoreAmarante.dto.LoginForm;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.AuthSessionService;
import com.example.drugstoreAmarante.service.CarrinhoService;
import com.example.drugstoreAmarante.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthSessionService authSessionService;
    private final CarrinhoService carrinhoService;

    public AuthController(
        UserService userService,
        AuthSessionService authSessionService,
        CarrinhoService carrinhoService
    ) {
        this.userService = userService;
        this.authSessionService = authSessionService;
        this.carrinhoService = carrinhoService;
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        User user = authSessionService.currentUser(session);
        if (user == null) {
            return "redirect:/login?next=/perfil";
        }

        if (user.getPerfil() == PerfilUsuario.ADMIN) {
            return "redirect:/admin/produtos";
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
    public String cadastrar(@ModelAttribute User user, Model model) {
        String cpf = normalizeCpf(user.getCpf());
        String email = normalizeEmail(user.getEmail());

        if (cpf.isBlank()) {
            model.addAttribute("erroCadastro", "Informe um CPF válido.");
            model.addAttribute("user", user);
            return "cadastro";
        }

        if (userService.existsByCpf(cpf)) {
            model.addAttribute("erroCadastro", "Esse CPF já está cadastrado.");
            user.setCpf(cpf);
            model.addAttribute("user", user);
            return "cadastro";
        }

        if (userService.existsByEmail(email)) {
            model.addAttribute("erroCadastro", "Esse email já está cadastrado.");
            user.setCpf(cpf);
            user.setEmail(email);
            model.addAttribute("user", user);
            return "cadastro";
        }

        try {
            user.setCpf(cpf);
            user.setEmail(email);
            userService.salvarCliente(user);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("erroCadastro", "Não foi possível concluir o cadastro porque já existe um dado repetido.");
            user.setCpf(cpf);
            user.setEmail(email);
            model.addAttribute("user", user);
            return "cadastro";
        }

        return "redirect:/login?cadastro=cliente";
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
        HttpServletRequest request,
        HttpSession session
    ) {
        String login = normalizeLogin(loginForm.getEmail());
        User user = userService.authenticate(login, PerfilUsuario.CLIENTE, loginForm.getPassword());

        if (user != null) {
            request.changeSessionId();
            authSessionService.login(session, user);
            carrinhoService.fundirCarrinhoVisitanteSeHouver(session, user);
            return "redirect:" + sanitizeClientNext(next);
        }

        return "redirect:/login?erro=1";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        authSessionService.logout(session);
        return "redirect:/";
    }

    private String sanitizeNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/") || next.startsWith("//") || next.contains("\\")) {
            return "/";
        }
        return next;
    }

    private String sanitizeClientNext(String next) {
        if (next == null || next.isBlank() || !next.startsWith("/")) {
            return "/perfil";
        }

        String sanitized = sanitizeNext(next);
        if (sanitized.startsWith("/admin/")) {
            return "/perfil";
        }
        return sanitized;
    }

    private String normalizeLogin(String login) {
        return login == null ? null : login.trim();
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
