package com.godown.storage.repository;

import com.godown.storage.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByCustomer_IdOrderByIdDesc(Long customerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select b from Booking b
        where b.id = :bookingId and b.customer.id = :customerId
        """)
    Optional<Booking> findOwnBookingForUpdate(
            @Param("bookingId") Long bookingId,
            @Param("customerId") Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :bookingId")
    Optional<Booking> findByIdForUpdate(@Param("bookingId") Long bookingId);

    boolean existsByIdAndCustomer_Id(Long id, Long customerId);
}