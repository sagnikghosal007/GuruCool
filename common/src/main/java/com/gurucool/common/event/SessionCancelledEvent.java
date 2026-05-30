package com.gurucool.common.event;

import java.util.List;
import java.util.UUID;

public record SessionCancelledEvent(UUID sessionId, String sessionTitle, List<UUID> studentIds, UUID mentorId, String mentorEmail, String cancellationReason) {}
