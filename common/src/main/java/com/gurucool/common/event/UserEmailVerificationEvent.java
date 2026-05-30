package com.gurucool.common.event;

import java.util.UUID;

public record UserEmailVerificationEvent(UUID userId, String email, String verificationToken, String fullName) {}
