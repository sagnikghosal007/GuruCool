package com.gurucool.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRefundedEvent(UUID paymentOrderId, UUID bookingId, UUID studentId, String studentEmail, BigDecimal amount) {}
