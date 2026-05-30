package com.gurucool.sessionservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_mentor_id", columnList = "mentor_id"),
    @Index(name = "idx_session_status", columnList = "status"),
    @Index(name = "idx_session_scheduled_at", columnList = "scheduled_at"),
    @Index(name = "idx_session_type", columnList = "session_type")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Session extends BaseEntity {

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    private SessionType sessionType;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "max_participants", nullable = false)
    @Builder.Default
    private Integer maxParticipants = 1;

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.UPCOMING;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "price_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal priceAmount = BigDecimal.ZERO;

    @Column(name = "price_currency", length = 10)
    @Builder.Default
    private String priceCurrency = "INR";

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}
