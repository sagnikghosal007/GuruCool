package com.gurucool.mentorservice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityRequest(
    @NotNull List<AvailabilitySlot> slots
) {
    public record AvailabilitySlot(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
    ) {}
}
