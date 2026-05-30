package com.gurucool.sessionservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
    UUID id,
    UUID mentorId,
    String title,
    String description,
    String sessionType,
    Instant scheduledAt,
    Integer durationMinutes,
    Integer maxParticipants,
    Integer currentParticipants,
    String status,
    String meetingLink,
    Boolean isPaid,
    BigDecimal priceAmount,
    String priceCurrency,
    Instant createdAt
) {}
