package com.gurucool.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record MentorMatchRequest(
    @NotNull UUID studentId,
    @NotBlank String careerGoal,
    List<String> skills,
    @NotBlank String preferredDomain,
    String learningStyle
) {}
