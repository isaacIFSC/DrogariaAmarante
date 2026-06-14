package com.example.drugstoreAmarante.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @GetMapping("/carrinho")
    public String carrinho(Model model) {
        List<Map<String, Object>> carrinho = List.of(
            Map.of(
                "nome", "Vitamina C 500mg",
                "precoUnitario", "R$ 29,90",
                "quantidade", 1,
                "total", "R$ 29,90",
                "imagemUrl", "/images/produto-placeholder.png"
            ),
            Map.of(
                "nome", "Protetor Solar FPS 50",
                "precoUnitario", "R$ 49,90",
                "quantidade", 2,
                "total", "R$ 99,80",
                "imagemUrl", "/images/produto-placeholder.png"
            )
        );

        Map<String, String> resumo = Map.of(
            "subtotal", "R$ 129,70",
            "descontos", "R$ 10,00",
            "taxaEntrega", "R$ 8,00",
            "total", "R$ 127,70"
        );

        model.addAttribute("carrinho", carrinho);
        model.addAttribute("resumo", resumo);
        return "carrinho";
    }
}
