package com.gurucool.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VerifyPaymentRequest(
    @NotBlank String mockOrderId,
    @NotBlank String mockPaymentId,
    @NotBlank String mockSignature,
    @NotNull UUID bookingId
) {}
