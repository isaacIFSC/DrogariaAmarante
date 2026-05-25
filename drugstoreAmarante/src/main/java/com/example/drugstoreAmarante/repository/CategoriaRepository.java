package com.example.drugstoreAmarante.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.drugstoreAmarante.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
