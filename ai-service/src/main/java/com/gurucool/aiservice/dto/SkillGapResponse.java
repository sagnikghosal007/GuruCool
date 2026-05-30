package com.gurucool.aiservice.dto;

import java.util.List;

public record SkillGapResponse(
    List<String> missingSkills,
    List<String> prioritySkills,
    int estimatedLearningWeeks
) {}
