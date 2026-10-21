package com.example.drugstoreAmarante.dto;

public class AdicionarCarrinhoRequest {
    private Long produtoId;
    private Integer quantidade = 1;

    public AdicionarCarrinhoRequest() {
    }

    public AdicionarCarrinhoRequest(Long produtoId, Integer quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
