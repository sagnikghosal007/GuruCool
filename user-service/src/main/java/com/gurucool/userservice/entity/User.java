package com.gurucool.userservice.entity;

import com.gurucool.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_college_id", columnList = "college_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String profilePictureUrl;

    @Column(name = "college_id")
    private UUID collegeId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(length = 500)
    private String emailVerificationToken;

    private Instant emailVerificationTokenExpiry;

    private Instant lastLoginAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    private Instant lockedUntil;
}
