package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.AdicionarCarrinhoRequest;
import com.example.drugstoreAmarante.dto.AtualizarQuantidadeRequest;
import com.example.drugstoreAmarante.dto.CarrinhoResumoResponse;
import com.example.drugstoreAmarante.service.CarrinhoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoApiController {

    private final CarrinhoService carrinhoService;

    public CarrinhoApiController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @GetMapping
    public ResponseEntity<CarrinhoResumoResponse> obterCarrinho(HttpSession session) {
        return ResponseEntity.ok(carrinhoService.obterResumo(session));
    }

    @PostMapping("/adicionar")
    public ResponseEntity<CarrinhoResumoResponse> adicionar(
        @RequestBody AdicionarCarrinhoRequest request,
        HttpSession session
    ) {
        CarrinhoResumoResponse resumo = carrinhoService.adicionarItem(
            request.getProdutoId(),
            request.getQuantidade(),
            session
        );
        return ResponseEntity.ok(resumo);
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<CarrinhoResumoResponse> atualizarQuantidade(
        @PathVariable Long itemId,
        @RequestBody AtualizarQuantidadeRequest request,
        HttpSession session
    ) {
        CarrinhoResumoResponse resumo = carrinhoService.atualizarQuantidade(
            itemId,
            request.getQuantidade(),
            session
        );
        return ResponseEntity.ok(resumo);
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<CarrinhoResumoResponse> remover(
        @PathVariable Long itemId,
        HttpSession session
    ) {
        CarrinhoResumoResponse resumo = carrinhoService.removerItem(itemId, session);
        return ResponseEntity.ok(resumo);
    }

    @DeleteMapping("/limpar")
    public ResponseEntity<Void> limpar(HttpSession session) {
        carrinhoService.limparCarrinho(session);
        return ResponseEntity.noContent().build();
    }
}
