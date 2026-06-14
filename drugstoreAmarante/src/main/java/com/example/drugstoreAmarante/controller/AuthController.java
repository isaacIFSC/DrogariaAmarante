package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {

        User user = (User) session.getAttribute("usuarioLogado");

        if (user == null) {
            return "redirect:/login";
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

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginForm loginForm, HttpSession session) {

        User user = userService.findByEmail(loginForm.getEmail());

        if(user != null && user.getPassword().equals(loginForm.getPassword())) {
            session.setAttribute("usuarioLogado", user);
            log.info("usuario logado");
            System.out.println("LOGIN OK");
            return "redirect:/";
        }

        System.out.println("LOGIN FALHOU");
        return "redirect:/login?erro";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    public static class LoginForm {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}