package com.gurucool.paymentservice.repository;

import com.gurucool.paymentservice.entity.PaymentOrder;
import com.gurucool.paymentservice.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {
    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentOrder> findByMockOrderId(String mockOrderId);
    Page<PaymentOrder> findByStudentId(UUID studentId, Pageable pageable);
    Page<PaymentOrder> findByMentorId(UUID mentorId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentOrder p WHERE p.id = :id")
    Optional<PaymentOrder> findByIdForUpdate(UUID id);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentOrder p WHERE p.mentorId = :mentorId AND p.status = 'CAPTURED'")
    BigDecimal sumCapturedAmountByMentorId(UUID mentorId);

    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.status = :status")
    long countByStatus(PaymentStatus status);
}
