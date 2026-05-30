package com.gurucool.aiservice.dto;

import java.util.List;

public record SessionSummaryResponse(
    String summary,
    List<String> keyTakeaways,
    List<String> suggestedNextSteps
) {}
