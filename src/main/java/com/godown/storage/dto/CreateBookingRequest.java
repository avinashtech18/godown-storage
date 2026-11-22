package com.godown.storage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBookingRequest(@NotNull @Positive Long unitId) {
}