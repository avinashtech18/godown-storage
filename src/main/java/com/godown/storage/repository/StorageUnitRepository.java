package com.godown.storage.repository;

import com.godown.storage.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import com.godown.storage.entity.StorageUnitStatus;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Long> {
    // Spring Data filters by status in MySQL and orders the results by code.
    List<StorageUnit> findAllByStatusOrderByCodeAsc(StorageUnitStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select unit from StorageUnit unit where unit.id = :id")
    Optional<StorageUnit> findByIdForUpdate(@Param("id") Long id);
}