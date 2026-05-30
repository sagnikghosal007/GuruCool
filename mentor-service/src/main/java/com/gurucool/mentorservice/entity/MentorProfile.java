package com.gurucool.mentorservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mentor_profiles", indexes = {
    @Index(name = "idx_mentor_user_id", columnList = "user_id"),
    @Index(name = "idx_mentor_college_id", columnList = "college_id"),
    @Index(name = "idx_mentor_verification_status", columnList = "verification_status"),
    @Index(name = "idx_mentor_average_rating", columnList = "average_rating")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 200)
    private String currentCompany;

    @Column(length = 200)
    private String currentRole;

    private Integer experienceYears;

    @Column(length = 500)
    private String linkedinUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "college_id")
    private UUID collegeId;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(length = 500)
    private String profilePictureUrl;

    @Column(length = 100)
    private String fullName;

    @Column(length = 255)
    private String email;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExpertiseTag> expertiseTags = new ArrayList<>();

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MentorAvailability> availabilitySlots = new ArrayList<>();
}
