package com.gurucool.mentorservice.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record UpdateMentorProfileRequest(
    @Size(max = 2000)
    String bio,
    @Size(max = 200)
    String currentCompany,
    @Size(max = 200)
    String currentRole,
    @Min(0) @Max(50)
    Integer experienceYears,
    String linkedinUrl,
    UUID collegeId,
    List<String> expertiseTags,
    List<String> expertiseCategories
) {}
