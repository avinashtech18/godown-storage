package com.godown.storage.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ScheduleBookingRequest(
        @NotNull LocalDateTime dropOffAt,
        @NotNull LocalDateTime pickupAt
) {
}