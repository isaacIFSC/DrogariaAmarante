package com.example.drugstoreAmarante.controller;

import com.example.drugstoreAmarante.dto.FinalizarPedidoRequest;
import com.example.drugstoreAmarante.dto.PedidoResponse;
import com.example.drugstoreAmarante.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutApiController {

    private final PedidoService pedidoService;

    public CheckoutApiController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/finalizar")
    public ResponseEntity<PedidoResponse> finalizar(
        @RequestBody FinalizarPedidoRequest request,
        HttpSession session
    ) {
        PedidoResponse pedido = pedidoService.finalizarPedido(request, session);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/meus-pedidos")
    public ResponseEntity<List<PedidoResponse>> meusPedidos(HttpSession session) {
        return ResponseEntity.ok(pedidoService.listarMeusPedidos(session));
    }

    @GetMapping("/pedido/{codigo}")
    public ResponseEntity<PedidoResponse> buscarPedido(@PathVariable String codigo, HttpSession session) {
        return ResponseEntity.ok(pedidoService.buscarPorCodigoRastreio(codigo, session));
    }
}
