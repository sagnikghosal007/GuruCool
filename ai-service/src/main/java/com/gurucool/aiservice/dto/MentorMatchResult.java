package com.gurucool.aiservice.dto;

import java.util.List;

public record MentorMatchResult(List<MentorMatch> matches) {
    public record MentorMatch(String mentorId, int matchScore, String matchReason) {}
}
