package com.gurucool.common.event;

import java.util.List;
import java.util.UUID;

public record SessionCompletedEvent(UUID sessionId, String sessionTitle, UUID mentorId, List<UUID> attendedStudentIds) {}
