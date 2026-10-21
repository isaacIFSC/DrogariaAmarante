package com.example.drugstoreAmarante.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.drugstoreAmarante.domain.CarrinhoItem;

@Repository
public interface CarrinhoItemRepository extends JpaRepository<CarrinhoItem, Long> {

    @EntityGraph(attributePaths = {"produto", "produto.categoria", "produto.marca"})
    List<CarrinhoItem> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"produto", "produto.categoria", "produto.marca"})
    List<CarrinhoItem> findBySessionKey(String sessionKey);

    List<CarrinhoItem> findByUserIdAndProdutoId(Long userId, Long produtoId);

    java.util.Optional<CarrinhoItem> findByIdAndUserId(Long id, Long userId);

    java.util.Optional<CarrinhoItem> findByIdAndSessionKey(Long id, String sessionKey);

    List<CarrinhoItem> findBySessionKeyAndProdutoId(String sessionKey, Long produtoId);

    void deleteByUserId(Long userId);

    void deleteBySessionKey(String sessionKey);
}
