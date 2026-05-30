package com.gurucool.sessionservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "waitlist", indexes = {
    @Index(name = "idx_waitlist_session_id", columnList = "session_id"),
    @Index(name = "idx_waitlist_student_id", columnList = "student_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Waitlist extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "position", nullable = false)
    private Integer position;
}
