package com.gurucool.mentorservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MentorProfileResponse(
    UUID id,
    UUID userId,
    String fullName,
    String email,
    String bio,
    String currentCompany,
    String currentRole,
    Integer experienceYears,
    String linkedinUrl,
    String verificationStatus,
    UUID collegeId,
    BigDecimal averageRating,
    Integer totalSessions,
    Integer totalRatings,
    String profilePictureUrl,
    List<String> expertiseTags,
    Instant createdAt
) {}
