package com.example.drugstoreAmarante.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nome;

    @Column(nullable = false, length = 1200)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Lob
    @Column(name = "imagem_url", nullable = false)
    private String imagemUrl;

    @Column(nullable = false)
    private Integer estoque;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    protected Produto() {
    }

    public Produto(
        String nome,
        String descricao,
        BigDecimal preco,
        String imagemUrl,
        Integer estoque,
        Categoria categoria,
        Marca marca
    ) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagemUrl = imagemUrl;
        this.estoque = estoque;
        this.categoria = categoria;
        this.marca = marca;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Marca getMarca() {
        return marca;
    }
}
