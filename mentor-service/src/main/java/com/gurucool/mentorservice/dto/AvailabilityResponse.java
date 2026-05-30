package com.gurucool.mentorservice.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
    UUID mentorId,
    List<SlotResponse> slots
) {
    public record SlotResponse(UUID id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, boolean isActive) {}
}
