package com.example.drugstoreAmarante.dto;

import java.math.BigDecimal;

public record CarrinhoItemResponse(
    Long id,
    Long produtoId,
    String nome,
    String descricao,
    BigDecimal precoUnitario,
    String precoUnitarioFormatado,
    Integer quantidade,
    BigDecimal total,
    String totalFormatado,
    String imagemUrl,
    Integer estoqueDisponivel
) {}
