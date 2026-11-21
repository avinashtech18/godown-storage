package com.godown.storage.controller;

import com.godown.storage.dto.CreateStorageUnitRequest;
import com.godown.storage.dto.StorageUnitResponse;
import com.godown.storage.service.StorageUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/storage-units")
public class StorageUnitController {

    private final StorageUnitService units;

    public StorageUnitController(StorageUnitService units) {
        this.units = units;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageUnitResponse create(@Valid @RequestBody CreateStorageUnitRequest request) {
        return units.create(request);
    }
    @GetMapping("/available")
    public List<StorageUnitResponse> listAvailable() {
        return units.listAvailable();
    }
}