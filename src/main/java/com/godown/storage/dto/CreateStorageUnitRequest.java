package com.godown.storage.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateStorageUnitRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 30) String sizeLabel,
        @NotNull @Positive @Digits(integer = 8, fraction = 2)
        BigDecimal monthlyBaseCapacityKg
) {}