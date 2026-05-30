package com.gurucool.common.event;

import java.util.UUID;

public record MentorVerificationRequestedEvent(UUID mentorId, UUID collegeId, String documentUrl, String mentorName) {}
