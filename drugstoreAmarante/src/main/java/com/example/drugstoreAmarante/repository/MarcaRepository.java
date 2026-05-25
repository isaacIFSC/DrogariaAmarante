package com.example.drugstoreAmarante.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.drugstoreAmarante.domain.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Long> {

    Optional<Marca> findByNomeIgnoreCase(String nome);
}
