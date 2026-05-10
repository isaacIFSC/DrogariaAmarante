package com.example.drugstoreAmarante.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.drugstoreAmarante.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<ProdutoResponse> listar(String q, BigDecimal minPreco, BigDecimal maxPreco, Integer estoqueMinimo) {
        return produtoRepository.search(q, minPreco, maxPreco, estoqueMinimo).stream()
            .map(this::toResponse)
            .toList();
    }

    public ProdutoResponse detalhar(Long id) {
        Product produto = produtoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));
        return toResponse(produto);
    }

    private ProdutoResponse toResponse(Product produto) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getName(),
            produto.getPrice(),
            produto.getImageUrl(),
            produto.getStock()
        );
    }
}
