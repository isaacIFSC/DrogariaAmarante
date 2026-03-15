package com.example.drugstoreAmarante.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Mock simples para visualizar o template
        List<Map<String, String>> produtos = List.of(
            Map.of(
                "nome", "Vitamina C 500mg",
                "preco", "R$ 29,90",
                "imagemUrl", "/images/produto-placeholder.png"
            ),
            Map.of(
                "nome", "Shampoo Anticaspa",
                "preco", "R$ 19,90",
                "imagemUrl", "/images/produto-placeholder.png"
            ),
            Map.of(
                "nome", "Protetor Solar FPS 50",
                "preco", "R$ 49,90",
                "imagemUrl", "/images/produto-placeholder.png"
            ),
            Map.of(
                "nome", "Colageno Hidrolisado",
                "preco", "R$ 39,90",
                "imagemUrl", "/images/produto-placeholder.png"
            )
        );

        model.addAttribute("produtos", produtos);
        return "index";
    }
}
