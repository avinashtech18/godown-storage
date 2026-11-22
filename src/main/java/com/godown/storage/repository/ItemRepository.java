package com.godown.storage.repository;

import com.godown.storage.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.math.BigDecimal;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select sum(i.weightKg) from Item i where i.booking.id = :bookingId")
    BigDecimal sumWeightByBookingId(@Param("bookingId") Long bookingId);
    List<Item> findAllByBooking_IdOrderByIdAsc(Long bookingId);
}