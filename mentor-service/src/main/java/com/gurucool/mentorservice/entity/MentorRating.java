package com.gurucool.mentorservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "mentor_ratings", indexes = {
    @Index(name = "idx_rating_mentor_id", columnList = "mentor_id"),
    @Index(name = "idx_rating_student_id", columnList = "student_id"),
    @Index(name = "idx_rating_session_id", columnList = "session_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorRating extends BaseEntity {

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAnonymous = false;
}
