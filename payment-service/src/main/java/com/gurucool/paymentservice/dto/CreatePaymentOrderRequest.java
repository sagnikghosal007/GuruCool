package com.gurucool.paymentservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentOrderRequest(
    @NotNull UUID bookingId,
    @NotNull @DecimalMin("1.00") BigDecimal amount,
    @NotBlank String currency,
    UUID mentorId
) {}
