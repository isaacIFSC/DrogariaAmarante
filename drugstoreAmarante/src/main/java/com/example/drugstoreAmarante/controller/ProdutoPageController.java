package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.ProdutoResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@Controller
public class ProdutoPageController {

    @GetMapping("/produto/{id}")
    public String produto(@PathVariable Long id, Model model) {

        ProdutoResponse produto = new ProdutoResponse(
                1L,
                "Vitamina C 500mg",
                new BigDecimal("29.90"),
                "/img/img_produto.png",
                10
        );

        model.addAttribute("produto", produto);

        return "produto";
    }
}
