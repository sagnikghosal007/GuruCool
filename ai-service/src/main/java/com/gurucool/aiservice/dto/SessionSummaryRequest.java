package com.gurucool.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SessionSummaryRequest(
    @NotNull UUID sessionId,
    @NotBlank String sessionNotes
) {}
