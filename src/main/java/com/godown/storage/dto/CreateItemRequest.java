package com.godown.storage.dto;

import com.godown.storage.entity.ItemSize;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateItemRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull ItemSize size,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal weightKg
) {
}