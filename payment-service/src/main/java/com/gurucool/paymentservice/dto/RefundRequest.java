package com.gurucool.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RefundRequest(
    @NotNull UUID paymentOrderId,
    BigDecimal amount,
    @NotBlank String reason
) {}
