package com.gurucool.sessionservice.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
    UUID bookingId,
    UUID sessionId,
    UUID studentId,
    String status,
    boolean paymentRequired,
    Integer waitlistPosition,
    Instant bookedAt
) {}
