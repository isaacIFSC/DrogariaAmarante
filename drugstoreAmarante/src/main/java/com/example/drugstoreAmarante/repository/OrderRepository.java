package com.example.drugstoreAmarante.repository;

import com.example.drugstoreAmarante.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}