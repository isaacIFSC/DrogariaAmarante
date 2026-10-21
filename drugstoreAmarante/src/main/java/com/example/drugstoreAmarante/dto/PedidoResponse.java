package com.example.drugstoreAmarante.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    String codigoRastreio,
    String nomeCliente,
    String emailCliente,
    String cpfCliente,
    String telefoneCliente,
    String enderecoCompleto,
    LocalDateTime dataCriacao,
    String dataCriacaoFormatada,
    String status,
    String statusDescricao,
    String formaPagamento,
    BigDecimal subtotal,
    String subtotalFormatado,
    BigDecimal frete,
    String freteFormatado,
    BigDecimal desconto,
    String descontoFormatado,
    BigDecimal total,
    String totalFormatado,
    List<ItemPedidoResponse> itens
) {
    public record ItemPedidoResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        String imagemUrl,
        BigDecimal precoUnitario,
        String precoUnitarioFormatado,
        Integer quantidade,
        BigDecimal subtotal,
        String subtotalFormatado
    ) {}
}
