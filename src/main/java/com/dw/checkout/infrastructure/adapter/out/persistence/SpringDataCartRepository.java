package com.dw.checkout.infrastructure.adapter.out.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, UUID> {
    @Modifying
    @Query(value = "UPDATE carts SET status = 'PROCESSING' " +
            "WHERE cart_id = :cartId AND status IN ('CREATED', 'DECLINED')", nativeQuery = true)
    int claimForPayment(@Param("cartId") UUID cartId);
}
