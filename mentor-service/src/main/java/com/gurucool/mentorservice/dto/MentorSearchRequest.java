package com.gurucool.mentorservice.dto;

import java.math.BigDecimal;

public record MentorSearchRequest(
    String domain,
    String company,
    BigDecimal minRating,
    Integer maxExperience,
    String verificationStatus,
    String tag
) {}
