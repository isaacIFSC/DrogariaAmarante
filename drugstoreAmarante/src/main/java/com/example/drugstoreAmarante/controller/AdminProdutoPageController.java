package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProdutoPageController {

    @GetMapping("/admin/produtos")
    public String paginaCadastroProdutos(HttpSession session, Model model) {
        if (!estaLogado(session)) {
            return "redirect:/login?next=/admin/produtos";
        }

        model.addAttribute("adminPage", true);
        return "admin/produtos";
    }

    private boolean estaLogado(HttpSession session) {
        return session.getAttribute("usuarioLogado") instanceof User;
    }
}
