package com.example.drugstoreAmarante.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarrinhoResumoResponse(
    List<CarrinhoItemResponse> itens,
    int quantidadeTotalItens,
    BigDecimal subtotal,
    String subtotalFormatado,
    BigDecimal frete,
    String freteFormatado,
    BigDecimal desconto,
    String descontoFormatado,
    BigDecimal total,
    String totalFormatado
) {}
