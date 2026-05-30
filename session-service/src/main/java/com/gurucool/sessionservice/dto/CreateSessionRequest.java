package com.gurucool.sessionservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateSessionRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 2000) String description,
    @NotBlank String sessionType,
    @NotNull @Future Instant scheduledAt,
    @NotNull @Min(15) @Max(480) Integer durationMinutes,
    @NotNull @Min(1) @Max(1000) Integer maxParticipants,
    String meetingLink,
    boolean isPaid,
    @DecimalMin(value = "0.0") BigDecimal priceAmount,
    String priceCurrency
) {}
