package com.gurucool.common.event;

import java.util.UUID;

public record WaitlistPromotedEvent(UUID sessionId, String sessionTitle, UUID studentId, String studentEmail, UUID bookingId) {}
