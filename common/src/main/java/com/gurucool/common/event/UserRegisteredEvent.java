package com.gurucool.common.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email, String role, UUID collegeId, String fullName) {}
