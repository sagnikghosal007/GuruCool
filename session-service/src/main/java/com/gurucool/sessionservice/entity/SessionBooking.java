package com.gurucool.sessionservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_bookings", indexes = {
    @Index(name = "idx_booking_session_id", columnList = "session_id"),
    @Index(name = "idx_booking_student_id", columnList = "student_id"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_idempotency_key", columnList = "idempotency_key")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionBooking extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "booked_at")
    private Instant bookedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;
}
