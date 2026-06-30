package com.example.drugstoreAmarante.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProdutoPageController {

    @GetMapping({"/produto/{id}", "/produtos/{id}"})
    public String detalheProduto(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", id);
        return "produto";
    }
}
