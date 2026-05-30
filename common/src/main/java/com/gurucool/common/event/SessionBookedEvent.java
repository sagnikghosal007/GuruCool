package com.gurucool.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record SessionBookedEvent(
        UUID bookingId,
        UUID sessionId,
        String sessionTitle,
        UUID studentId,
        String studentEmail,
        String studentName,
        UUID mentorId,
        String mentorEmail,
        String mentorName,
        boolean isPaid,
        BigDecimal priceAmount,
        String scheduledAt
) {}
