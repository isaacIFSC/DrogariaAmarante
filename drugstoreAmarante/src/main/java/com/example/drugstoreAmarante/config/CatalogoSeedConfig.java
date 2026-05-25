package com.example.drugstoreAmarante.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.drugstoreAmarante.domain.Categoria;
import com.example.drugstoreAmarante.domain.Marca;
import com.example.drugstoreAmarante.domain.Produto;
import com.example.drugstoreAmarante.repository.CategoriaRepository;
import com.example.drugstoreAmarante.repository.MarcaRepository;
import com.example.drugstoreAmarante.repository.ProdutoRepository;

@Configuration
public class CatalogoSeedConfig {

    @Bean
    CommandLineRunner seedCatalogo(
        CategoriaRepository categoriaRepository,
        MarcaRepository marcaRepository,
        ProdutoRepository produtoRepository
    ) {
        return args -> {
            if (produtoRepository.count() > 0) {
                return;
            }

            Categoria higiene = obterOuCriarCategoria(categoriaRepository, "Higiene");
            Categoria beleza = obterOuCriarCategoria(categoriaRepository, "Beleza");
            Categoria saude = obterOuCriarCategoria(categoriaRepository, "Saude");

            Marca colgate = obterOuCriarMarca(marcaRepository, "Colgate");
            Marca nivea = obterOuCriarMarca(marcaRepository, "Nivea");
            Marca omron = obterOuCriarMarca(marcaRepository, "Omron");

            List<Produto> produtos = List.of(
                new Produto(
                    "Creme Dental Colgate Total 12",
                    "Protecao completa para a higiene bucal diaria.",
                    new BigDecimal("12.90"),
                    "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=900&q=80",
                    48,
                    higiene,
                    colgate
                ),
                new Produto(
                    "Desodorante Nivea Men Original",
                    "Desodorante aerosol com frescor prolongado.",
                    new BigDecimal("18.50"),
                    "https://images.unsplash.com/photo-1583947215259-38e31be8751f?auto=format&fit=crop&w=900&q=80",
                    32,
                    beleza,
                    nivea
                ),
                new Produto(
                    "Monitor de Pressao Omron HEM-7122",
                    "Aparelho digital para monitoramento domestico da pressao arterial.",
                    new BigDecimal("229.00"),
                    "https://images.unsplash.com/photo-1516542076529-1ea3854896b8?auto=format&fit=crop&w=900&q=80",
                    14,
                    saude,
                    omron
                ),
                new Produto(
                    "Shampoo Anti-Residuos Nivea",
                    "Limpeza profunda com uso frequente.",
                    new BigDecimal("27.30"),
                    "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=900&q=80",
                    21,
                    beleza,
                    nivea
                ),
                new Produto(
                    "Escova Dental Colgate Triple Action",
                    "Cerdas para limpeza eficiente dos dentes e gengivas.",
                    new BigDecimal("9.80"),
                    "https://images.unsplash.com/photo-1607619056574-7b8d3ee536b2?auto=format&fit=crop&w=900&q=80",
                    60,
                    higiene,
                    colgate
                )
            );

            produtoRepository.saveAll(produtos);
        };
    }

    private Categoria obterOuCriarCategoria(CategoriaRepository categoriaRepository, String nome) {
        return categoriaRepository.findByNomeIgnoreCase(nome)
            .orElseGet(() -> categoriaRepository.save(new Categoria(nome)));
    }

    private Marca obterOuCriarMarca(MarcaRepository marcaRepository, String nome) {
        return marcaRepository.findByNomeIgnoreCase(nome)
            .orElseGet(() -> marcaRepository.save(new Marca(nome)));
    }
}
