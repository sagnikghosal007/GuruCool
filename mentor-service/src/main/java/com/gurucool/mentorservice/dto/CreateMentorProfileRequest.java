package com.gurucool.mentorservice.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record CreateMentorProfileRequest(
    @NotBlank(message = "Bio is required")
    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    String bio,

    @NotBlank(message = "Current company is required")
    @Size(max = 200)
    String currentCompany,

    @NotBlank(message = "Current role is required")
    @Size(max = 200)
    String currentRole,

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years cannot exceed 50")
    Integer experienceYears,

    @Pattern(regexp = "^(https?://)?(www\\.)?linkedin\\.com/.*$", message = "Invalid LinkedIn URL")
    String linkedinUrl,

    UUID collegeId,
    List<String> expertiseTags,
    List<String> expertiseCategories
) {}
