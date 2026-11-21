package com.godown.storage.service;

import com.godown.storage.dto.CreateStorageUnitRequest;
import com.godown.storage.dto.StorageUnitResponse;
import com.godown.storage.entity.StorageUnit;
import com.godown.storage.entity.StorageUnitStatus;
import com.godown.storage.repository.StorageUnitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Service
public class StorageUnitService {

    private final StorageUnitRepository units;

    public StorageUnitService(StorageUnitRepository units) {
        this.units = units;
    }

    public StorageUnitResponse create(CreateStorageUnitRequest request) {
        StorageUnit unit = new StorageUnit(
                request.code().strip().toUpperCase(Locale.ROOT),
                request.sizeLabel().strip(),
                request.monthlyBaseCapacityKg());

        try {
            // MySQL enforces unique codes even when two requests arrive together.
            StorageUnit saved = units.saveAndFlush(unit);
            return toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            if (exception.getMostSpecificCause() instanceof SQLException sql
                    && sql.getErrorCode() == 1062) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Storage unit code already exists");
            }
            throw exception;
        }
    }

    public List<StorageUnitResponse> listAvailable() {
        return units.findAllByStatusOrderByCodeAsc(StorageUnitStatus.AVAILABLE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StorageUnitResponse toResponse(StorageUnit unit) {
        return new StorageUnitResponse(
                unit.getId(),
                unit.getCode(),
                unit.getSizeLabel(),
                unit.getMonthlyBaseCapacityKg(),
                unit.getStatus());
    }
}