package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.service.AuthSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProdutoPageController {

    private final AuthSessionService authSessionService;

    public AdminProdutoPageController(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @GetMapping("/admin/produtos")
    public String paginaCadastroProdutos(HttpSession session, Model model) {
        if (!authSessionService.isAdmin(session)) {
            return "redirect:/admin/login?next=/admin/produtos";
        }

        model.addAttribute("adminPage", true);
        return "admin/produtos";
    }
}
