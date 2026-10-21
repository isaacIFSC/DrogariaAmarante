package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.CarrinhoResumoResponse;
import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.service.AuthSessionService;
import com.example.drugstoreAmarante.service.CarrinhoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    private final CarrinhoService carrinhoService;
    private final AuthSessionService authSessionService;

    public CartController(CarrinhoService carrinhoService, AuthSessionService authSessionService) {
        this.carrinhoService = carrinhoService;
        this.authSessionService = authSessionService;
    }

    @GetMapping("/carrinho")
    public String carrinho(HttpSession session, Model model) {
        preencherDadosCarrinho(session, model);
        return "carrinho";
    }

    @GetMapping("/pagamento")
    public String pagamento(HttpSession session, Model model) {
        User user = authSessionService.currentUser(session);
        if (user == null || !authSessionService.isCliente(session)) {
            return "redirect:/login?next=/pagamento";
        }
        preencherDadosCarrinho(session, model);
        model.addAttribute("usuarioLogado", user);
        return "pagamento";
    }

    private void preencherDadosCarrinho(HttpSession session, Model model) {
        CarrinhoResumoResponse resumo = carrinhoService.obterResumo(session);
        model.addAttribute("resumo", resumo);
        model.addAttribute("carrinho", resumo.itens());
    }
}
