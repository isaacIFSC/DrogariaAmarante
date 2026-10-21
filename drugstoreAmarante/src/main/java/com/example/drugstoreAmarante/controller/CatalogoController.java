package com.example.drugstoreAmarante.controller;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.drugstoreAmarante.dto.CategoriaResponse;
import com.example.drugstoreAmarante.dto.MarcaResponse;
import com.example.drugstoreAmarante.dto.CatalogoPaginaResponse;
import com.example.drugstoreAmarante.repository.CategoriaRepository;
import com.example.drugstoreAmarante.repository.MarcaRepository;
import com.example.drugstoreAmarante.service.ProdutoService;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProdutoService produtoService;

    public CatalogoController(
        CategoriaRepository categoriaRepository,
        MarcaRepository marcaRepository,
        ProdutoService produtoService
    ) {
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.produtoService = produtoService;
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
            .map(categoria -> new CategoriaResponse(categoria.getId(), categoria.getNome()))
            .toList();
    }

    @GetMapping("/marcas")
    public List<MarcaResponse> listarMarcas() {
        return marcaRepository.findAll().stream()
            .map(marca -> new MarcaResponse(marca.getId(), marca.getNome()))
            .toList();
    }

    @GetMapping("/produtos")
    public CatalogoPaginaResponse listarProdutos(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) String marca,
        @RequestParam(required = false) BigDecimal minPreco,
        @RequestParam(required = false) BigDecimal maxPreco,
        @RequestParam(required = false) Boolean disponivel,
        @RequestParam(required = false) String ordenacao,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "12") int tamanho
    ) {
        return produtoService.listarPaginado(
            q, categoria, marca, minPreco, maxPreco, disponivel, ordenacao, pagina, tamanho
        );
    }
}
