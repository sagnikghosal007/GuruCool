package com.gurucool.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentOrderResponse(
    UUID paymentOrderId,
    String mockOrderId,
    UUID bookingId,
    BigDecimal amount,
    String currency,
    String status,
    String mockSignature,
    String note
) {}
