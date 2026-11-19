package com.dw.checkout.infrastructure.adapter.out.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dw.checkout.domain.model.CartStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, UUID> {
    @Modifying
    @Query(value = "UPDATE carts SET status = 'PROCESSING', payment_attempt_method = :method, " +
            "payment_attempt_card_last_digits = :lastDigits, payment_processing_at = CURRENT_TIMESTAMP, " +
            "payment_attempt_id = :attemptId " +
            "WHERE cart_id = :cartId AND status IN ('CREATED', 'DECLINED')", nativeQuery = true)
    int claimForPayment(@Param("cartId") UUID cartId,
                        @Param("method") String method,
                        @Param("lastDigits") String lastDigits,
                        @Param("attemptId") UUID attemptId);

    @Modifying
    @Query(value = "UPDATE carts SET payment_processing_at = CURRENT_TIMESTAMP " +
            "WHERE cart_id = :cartId AND status = 'PROCESSING' " +
            "AND payment_processing_at <= :cutoff", nativeQuery = true)
    int claimStuckRecovery(@Param("cartId") UUID cartId, @Param("cutoff") Instant cutoff);

    List<CartJpaEntity> findTop100ByStatusAndPaymentProcessingAtBeforeOrderByPaymentProcessingAtAsc(
            CartStatus status, Instant cutoff);
}
