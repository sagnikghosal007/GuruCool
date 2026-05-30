package com.gurucool.sessionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookSessionRequest(
    @NotNull UUID sessionId,
    @NotBlank String idempotencyKey
) {}
