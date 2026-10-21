package com.example.drugstoreAmarante.domain;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO("Aguardando Pagamento"),
    PAGO("Pagamento Confirmado"),
    EM_SEPARACAO("Em Separação"),
    ENVIADO("Enviado"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
