package com.example.drugstoreAmarante.service;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.criteria.JoinType;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.drugstoreAmarante.dto.ProdutoResponse;
import com.example.drugstoreAmarante.domain.Categoria;
import com.example.drugstoreAmarante.domain.Marca;
import com.example.drugstoreAmarante.domain.Produto;
import com.example.drugstoreAmarante.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<ProdutoResponse> listar(
        String q,
        String categoria,
        String marca,
        BigDecimal minPreco,
        BigDecimal maxPreco,
        Boolean disponivel,
        String ordenacao
    ) {
        Specification<Produto> specification = Specification.where(carregarRelacionamentos())
            .and(nomeOuDescricaoContem(q))
            .and(categoriaIgual(categoria))
            .and(marcaIgual(marca))
            .and(precoMaiorOuIgual(minPreco))
            .and(precoMenorOuIgual(maxPreco))
            .and(disponivelIgual(disponivel));

        return produtoRepository.findAll(specification, sortFrom(ordenacao)).stream()
            .map(this::toResponse)
            .toList();
    }

    public ProdutoResponse detalhar(Long id) {
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));
        return toResponse(produto);
    }

    private ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getImagemUrl(),
            produto.getEstoque(),
            produto.isAtivo() && produto.getEstoque() != null && produto.getEstoque() > 0,
            produto.getCategoria().getNome(),
            produto.getMarca().getNome()
        );
    }

    private Specification<Produto> nomeOuDescricaoContem(String termo) {
        return (root, query, cb) -> {
            if (termo == null || termo.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + termo.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("nome")), like),
                cb.like(cb.lower(root.get("descricao")), like),
                cb.like(cb.lower(root.get("categoria").get("nome")), like),
                cb.like(cb.lower(root.get("marca").get("nome")), like)
            );
        };
    }

    private Specification<Produto> carregarRelacionamentos() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("categoria", JoinType.LEFT);
                root.fetch("marca", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    private Specification<Produto> categoriaIgual(String categoria) {
        return (root, query, cb) -> {
            if (categoria == null || categoria.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("categoria").get("nome")), categoria.toLowerCase());
        };
    }

    private Specification<Produto> marcaIgual(String marca) {
        return (root, query, cb) -> {
            if (marca == null || marca.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("marca").get("nome")), marca.toLowerCase());
        };
    }

    private Specification<Produto> precoMaiorOuIgual(BigDecimal minPreco) {
        return (root, query, cb) -> minPreco == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("preco"), minPreco);
    }

    private Specification<Produto> precoMenorOuIgual(BigDecimal maxPreco) {
        return (root, query, cb) -> maxPreco == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("preco"), maxPreco);
    }

    private Specification<Produto> disponivelIgual(Boolean disponivel) {
        return (root, query, cb) -> {
            if (disponivel == null) {
                return cb.conjunction();
            }
            return disponivel
                ? cb.and(cb.equal(root.get("ativo"), true), cb.greaterThan(root.get("estoque"), 0))
                : cb.or(cb.equal(root.get("ativo"), false), cb.lessThanOrEqualTo(root.get("estoque"), 0));
        };
    }

    private Sort sortFrom(String ordenacao) {
        if (ordenacao == null || ordenacao.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "nome");
        }

        return switch (ordenacao.toLowerCase()) {
            case "preco_desc" -> Sort.by(Sort.Direction.DESC, "preco");
            case "preco_asc" -> Sort.by(Sort.Direction.ASC, "preco");
            case "estoque_desc" -> Sort.by(Sort.Direction.DESC, "estoque");
            case "nome_desc" -> Sort.by(Sort.Direction.DESC, "nome");
            default -> Sort.by(Sort.Direction.ASC, "nome");
        };
    }
}
