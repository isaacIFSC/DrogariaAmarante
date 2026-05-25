package com.example.drugstoreAmarante.dto;

import java.util.List;

public record ProdutoImportacaoResposta(
    int linhasLidas,
    int produtosCriados,
    List<String> erros
) {
}
