package com.gurucool.mentorservice.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record RateMentorRequest(
    @NotNull UUID sessionId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @Size(max = 500, message = "Feedback cannot exceed 500 characters")
    String feedback,
    boolean isAnonymous
) {}
