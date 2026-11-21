package com.godown.storage.repository;

import com.godown.storage.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import com.godown.storage.entity.StorageUnitStatus;
import java.util.List;
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Long> {
    // Spring Data filters by status in MySQL and orders the results by code.
    List<StorageUnit> findAllByStatusOrderByCodeAsc(StorageUnitStatus status);
}