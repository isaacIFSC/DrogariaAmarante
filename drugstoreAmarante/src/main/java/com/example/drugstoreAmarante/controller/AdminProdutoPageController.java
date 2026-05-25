package com.example.drugstoreAmarante.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProdutoPageController {

    @GetMapping("/admin/produtos")
    public String paginaCadastroProdutos() {
        return "admin/produtos";
    }
}
