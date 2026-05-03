package com.example.drugstoreAmarante.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
    Long id,
    String nome,
    BigDecimal preco,
    String imagemUrl,
    Integer estoque
) {
}
