package com.dw.checkout.infrastructure.adapter.out.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, UUID> {
}