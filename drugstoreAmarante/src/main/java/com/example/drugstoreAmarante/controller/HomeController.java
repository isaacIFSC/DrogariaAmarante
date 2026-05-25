package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.service.ProdutoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    private final ProdutoService produtoService;

    public HomeController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/")
    public String index(Model model) {

        List<ProdutoResponse> produtos = List.of(
                new ProdutoResponse(
                        1L,
                        "Vitamina C 500mg",
                        new BigDecimal("29.90"),
                        "/img/img_produto.png",
                        10
                )
        );

        model.addAttribute("produtos", produtos);

        return "index";
    }
}