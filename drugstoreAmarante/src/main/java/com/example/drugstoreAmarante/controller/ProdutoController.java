package com.example.drugstoreAmarante.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.service.ProdutoService;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoResponse> listar(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) BigDecimal minPreco,
        @RequestParam(required = false) BigDecimal maxPreco,
        @RequestParam(required = false) Integer estoqueMinimo
    ) {
        return produtoService.listar(q, minPreco, maxPreco, estoqueMinimo);
    }

    @GetMapping("/{id}")
    public ProdutoResponse detalhar(@PathVariable Long id) {
        return produtoService.detalhar(id);
    }
}
