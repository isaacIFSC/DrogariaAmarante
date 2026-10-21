package com.example.drugstoreAmarante.repository;

import com.example.drugstoreAmarante.model.User;
import com.example.drugstoreAmarante.domain.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCpf(String cpf);

    long countByPerfil(PerfilUsuario perfil);

}
