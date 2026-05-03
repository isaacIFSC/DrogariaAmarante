package com.example.drugstoreAmarante.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.drugstoreAmarante.domain.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("""
        select p
        from Produto p
        where (:q is null or :q = '' or lower(p.nome) like lower(concat('%', :q, '%')))
          and (:minPreco is null or p.preco >= :minPreco)
          and (:maxPreco is null or p.preco <= :maxPreco)
          and (:estoqueMinimo is null or p.estoque >= :estoqueMinimo)
        order by p.nome
        """)
    List<Produto> search(
        @Param("q") String q,
        @Param("minPreco") BigDecimal minPreco,
        @Param("maxPreco") BigDecimal maxPreco,
        @Param("estoqueMinimo") Integer estoqueMinimo
    );
}
