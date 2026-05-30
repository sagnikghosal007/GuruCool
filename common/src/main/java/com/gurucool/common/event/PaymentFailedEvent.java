package com.gurucool.common.event;

import java.util.UUID;

public record PaymentFailedEvent(UUID paymentOrderId, UUID bookingId, UUID studentId, String studentEmail, String reason) {}
