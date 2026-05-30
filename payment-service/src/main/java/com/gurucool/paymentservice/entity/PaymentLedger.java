package com.gurucool.paymentservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_ledger", indexes = {
    @Index(name = "idx_ledger_payment_order_id", columnList = "payment_order_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentLedger extends BaseEntity {

    @Column(name = "payment_order_id", nullable = false)
    private UUID paymentOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private LedgerEventType eventType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
