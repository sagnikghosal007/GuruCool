package com.gurucool.aiservice.dto;

import java.util.List;

public record CareerPathResponse(List<Phase> phases) {
    public record Phase(int phase, String duration, List<String> skills, List<String> resources, String milestoneTitle) {}
}
