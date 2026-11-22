package com.godown.storage.controller;

import com.godown.storage.dto.BookingResponse;
import com.godown.storage.dto.CreateBookingRequest;
import com.godown.storage.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookings;

    public BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBookingRequest request) {
        return bookings.create(Long.valueOf(jwt.getSubject()), request.unitId());
    }

    @GetMapping
    public List<BookingResponse> listOwnBookings(@AuthenticationPrincipal Jwt jwt) {
        return bookings.listOwnBookings(Long.valueOf(jwt.getSubject()));
    }

    @PatchMapping("/{id}/close")
    public BookingResponse close(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long bookingId) {
        return bookings.closeOwnBooking(Long.valueOf(jwt.getSubject()), bookingId);
    }
}