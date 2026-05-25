package com.example.drugstoreAmarante.dto;

import java.math.BigDecimal;

public record ProdutoCadastroRequest(
    String nome,
    String descricao,
    BigDecimal preco,
    String imagemUrl,
    Integer estoque,
    String categoria,
    String marca
) {
}
