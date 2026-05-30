package com.gurucool.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerificationReviewRequest(
    @NotNull Boolean approved,
    @NotBlank String reviewNote
) {}
