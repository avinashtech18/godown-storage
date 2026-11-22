package com.godown.storage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "storage_units")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "size_label", nullable = false, length = 30)
    private String sizeLabel;

    // Store capacity to two decimal places so fractional kilograms stay exact.
    @Column(name = "monthly_base_capacity_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyBaseCapacityKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StorageUnitStatus status;

    public StorageUnit(String code, String sizeLabel, BigDecimal monthlyBaseCapacityKg) {
        this.code = code;
        this.sizeLabel = sizeLabel;
        this.monthlyBaseCapacityKg = monthlyBaseCapacityKg;
        this.status = StorageUnitStatus.AVAILABLE;
    }
    public void markOccupied() {
        this.status = StorageUnitStatus.OCCUPIED;
    }

    public void markAvailable() {
        this.status = StorageUnitStatus.AVAILABLE;
    }
}