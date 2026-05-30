package com.gurucool.userservice.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String email,
    String fullName,
    String role,
    String phoneNumber,
    String profilePictureUrl,
    UUID collegeId,
    Boolean isEmailVerified,
    Boolean isActive,
    Instant createdAt
) {}
