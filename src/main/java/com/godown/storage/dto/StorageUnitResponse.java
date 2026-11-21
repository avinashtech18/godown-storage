package com.godown.storage.dto;

import com.godown.storage.entity.StorageUnitStatus;

import java.math.BigDecimal;

public record StorageUnitResponse(
        Long id,
        String code,
        String sizeLabel,
        BigDecimal monthlyBaseCapacityKg,
        StorageUnitStatus status
) {}