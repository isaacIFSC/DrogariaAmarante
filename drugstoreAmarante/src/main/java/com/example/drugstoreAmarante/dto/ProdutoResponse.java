package com.example.drugstoreAmarante.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    String imagemUrl,
    Integer estoque,
    Boolean disponivel,
    String categoria,
    String marca
) {
}
