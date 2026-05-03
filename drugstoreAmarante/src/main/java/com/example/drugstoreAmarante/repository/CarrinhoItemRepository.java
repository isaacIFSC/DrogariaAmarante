package com.example.drugstoreAmarante.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.drugstoreAmarante.domain.CarrinhoItem;

public interface CarrinhoItemRepository extends JpaRepository<CarrinhoItem, Long> {

    @EntityGraph(attributePaths = {"produto"})
    List<CarrinhoItem> findByUsuarioId(Long usuarioId);
}
