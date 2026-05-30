package com.gurucool.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CareerPathRequest(
    @NotEmpty List<String> currentSkills,
    @NotBlank String targetRole,
    int yearsExperience
) {}
