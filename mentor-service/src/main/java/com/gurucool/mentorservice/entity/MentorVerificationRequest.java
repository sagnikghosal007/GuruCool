package com.gurucool.mentorservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mentor_verification_requests", indexes = {
    @Index(name = "idx_verification_mentor_id", columnList = "mentor_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorVerificationRequest extends BaseEntity {

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "college_id")
    private UUID collegeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;
}
