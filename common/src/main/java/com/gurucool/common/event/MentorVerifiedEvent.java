package com.gurucool.common.event;

import java.util.UUID;

public record MentorVerifiedEvent(UUID mentorId, UUID userId, UUID collegeId, String status, String mentorName, String mentorEmail) {}
