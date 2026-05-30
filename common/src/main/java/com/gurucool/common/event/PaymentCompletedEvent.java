package com.gurucool.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(UUID paymentOrderId, UUID bookingId, UUID studentId, String studentEmail, UUID mentorId, BigDecimal amount, String currency) {}
