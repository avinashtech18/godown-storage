package com.godown.storage.dto;

import com.godown.storage.entity.ItemSize;

import java.math.BigDecimal;

public record ItemResponse(
        Long id,
        Long bookingId,
        String name,
        String description,
        ItemSize size,
        BigDecimal weightKg
) {
}