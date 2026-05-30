package com.gurucool.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundRequestedEvent(UUID bookingId, UUID studentId, BigDecimal amount, String reason) {}
