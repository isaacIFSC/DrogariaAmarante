package com.example.drugstoreAmarante.dto;

public class AtualizarQuantidadeRequest {
    private Integer quantidade;

    public AtualizarQuantidadeRequest() {
    }

    public AtualizarQuantidadeRequest(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
