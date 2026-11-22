package com.godown.storage.service;

import com.godown.storage.dto.BookingResponse;
import com.godown.storage.entity.Booking;
import com.godown.storage.entity.StorageUnit;
import com.godown.storage.entity.StorageUnitStatus;
import com.godown.storage.entity.User;
import com.godown.storage.repository.BookingRepository;
import com.godown.storage.repository.StorageUnitRepository;
import com.godown.storage.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.godown.storage.entity.BookingStatus;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class BookingService {

    private static final ZoneId WAREHOUSE_ZONE = ZoneId.of("Asia/Kolkata");

    private final BookingRepository bookings;
    private final StorageUnitRepository units;
    private final UserRepository users;

    public BookingService(
            BookingRepository bookings,
            StorageUnitRepository units,
            UserRepository users) {
        this.bookings = bookings;
        this.units = units;
        this.users = users;
    }

    @Transactional
    public BookingResponse create(Long customerId, Long unitId) {
        User customer = users.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Customer account no longer exists"));

        // Hold the row lock through commit so another request sees the updated status.
        StorageUnit unit = units.findByIdForUpdate(unitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Storage unit not found"));

        if (unit.getStatus() != StorageUnitStatus.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Storage unit is already occupied");
        }

        // JPA saves the unit status change in the same transaction as the booking.
        unit.markOccupied();
        Booking booking = bookings.save(
                new Booking(customer, unit, LocalDate.now(WAREHOUSE_ZONE)));

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listOwnBookings(Long customerId) {
        return bookings.findAllByCustomer_IdOrderByIdDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUnit().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus());
    }
    @Transactional
    public BookingResponse closeOwnBooking(Long customerId, Long bookingId) {
        // Lock the booking so concurrent close requests cannot release the unit twice.
        Booking booking = bookings.findOwnBookingForUpdate(bookingId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() == BookingStatus.CLOSED) {
            return toResponse(booking);
        }

        StorageUnit unit = units.findByIdForUpdate(booking.getUnit().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Storage unit not found"));

        booking.close(LocalDate.now(WAREHOUSE_ZONE));
        unit.markAvailable();

        return toResponse(booking);
    }
}