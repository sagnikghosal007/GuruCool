package com.gurucool.paymentservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_orders", indexes = {
    @Index(name = "idx_payment_booking_id", columnList = "booking_id"),
    @Index(name = "idx_payment_student_id", columnList = "student_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_mock_order_id", columnList = "mock_order_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentOrder extends BaseEntity {

    @Column(name = "mock_order_id", unique = true, length = 100)
    private String mockOrderId;

    @Column(name = "mock_payment_id", length = 100)
    private String mockPaymentId;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "mentor_id")
    private UUID mentorId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "mock_signature", length = 200)
    private String mockSignature;
}
