package com.example.drugstoreAmarante.repository;

import com.example.drugstoreAmarante.domain.Pedido;
import com.example.drugstoreAmarante.domain.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUserIdOrderByDataCriacaoDesc(Long userId);

    Optional<Pedido> findByCodigoRastreio(String codigoRastreio);

    Page<Pedido> findAllByOrderByDataCriacaoDesc(Pageable pageable);

    List<Pedido> findTop10ByOrderByDataCriacaoDesc();

    long countByStatus(StatusPedido status);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.status = 'PAGO'")
    BigDecimal somarFaturamentoTotal();
}
