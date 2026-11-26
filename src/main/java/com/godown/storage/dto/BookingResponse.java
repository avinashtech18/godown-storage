package com.godown.storage.dto;

import com.godown.storage.entity.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long unitId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        LocalDateTime scheduledDropOffAt,
        LocalDateTime scheduledPickupAt
) {
}