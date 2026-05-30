package com.gurucool.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SimulatePaymentRequest(
    @NotNull UUID bookingId,
    @NotNull BigDecimal amount,
    String currency,
    UUID mentorId,
    boolean forceFailure
) {}
