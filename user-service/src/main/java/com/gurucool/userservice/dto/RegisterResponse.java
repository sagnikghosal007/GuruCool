package com.gurucool.userservice.dto;

import java.util.UUID;

public record RegisterResponse(
    UUID userId,
    String email,
    String role,
    String message
) {}
