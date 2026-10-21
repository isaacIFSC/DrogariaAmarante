package com.example.drugstoreAmarante.dto;

import java.util.List;

public record CatalogoPaginaResponse(
    List<ProdutoResponse> produtos,
    int pagina,
    int tamanhoPagina,
    long totalProdutos,
    int totalPaginas,
    boolean temPaginaAnterior,
    boolean temProximaPagina
) {
}
