package com.example.drugstoreAmarante.repository;

import java.math.BigDecimal;
import java.util.List;

import com.example.drugstoreAmarante.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Product, Long> {

    @Query("""
        select p
        from Product p
        where (:q is null or :q = '' or lower(p.name) like lower(concat('%', :q, '%')))
          and (:minPreco is null or p.price >= :minPreco)
          and (:maxPreco is null or p.price <= :maxPreco)
          and (:estoqueMinimo is null or p.stock >= :estoqueMinimo)
        order by p.name
        """)
    List<Product> search(
            @Param("q") String q,
            @Param("minPreco") BigDecimal minPreco,
            @Param("maxPreco") BigDecimal maxPreco,
            @Param("estoqueMinimo") Integer estoqueMinimo
    );
}