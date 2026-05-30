package com.gurucool.sessionservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSessionStatusRequest(
    @NotBlank String status
) {}
