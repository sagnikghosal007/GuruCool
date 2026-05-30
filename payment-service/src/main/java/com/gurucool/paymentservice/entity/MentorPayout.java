package com.gurucool.paymentservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mentor_payouts", indexes = {
    @Index(name = "idx_payout_mentor_id", columnList = "mentor_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorPayout extends BaseEntity {

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "mock_payout_id", length = 100)
    private String mockPayoutId;

    @Column(name = "period_start")
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;

    @Column(name = "processed_at")
    private Instant processedAt;
}
