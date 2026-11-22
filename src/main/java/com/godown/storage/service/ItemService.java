package com.godown.storage.service;

import com.godown.storage.dto.CreateItemRequest;
import com.godown.storage.dto.ItemResponse;
import com.godown.storage.entity.Booking;
import com.godown.storage.entity.BookingStatus;
import com.godown.storage.entity.Item;
import com.godown.storage.repository.BookingRepository;
import com.godown.storage.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemService {

    private final BookingRepository bookings;
    private final ItemRepository items;

    public ItemService(BookingRepository bookings, ItemRepository items) {
        this.bookings = bookings;
        this.items = items;
    }

    @Transactional
    public ItemResponse create(Long customerId, Long bookingId, CreateItemRequest request) {
        // Use the same booking lock as closure so both operations run in order.
        Booking booking = bookings.findOwnBookingForUpdate(bookingId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is closed");
        }

        // SUM returns null when the booking has no items.
        BigDecimal existingWeight = items.sumWeightByBookingId(bookingId);
        if (existingWeight == null) {
            existingWeight = BigDecimal.ZERO;
        }

        BigDecimal totalWeight = existingWeight.add(request.weightKg());
        if (totalWeight.compareTo(booking.getUnit().getMonthlyBaseCapacityKg()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Item would exceed the unit's weight capacity");
        }

        Item saved = items.save(new Item(
                booking,
                request.name().strip(),
                request.description(),
                request.size(),
                request.weightKg()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listOwnItems(Long customerId, Long bookingId) {
        // Check ownership even when the booking has no items.
        if (!bookings.existsByIdAndCustomer_Id(bookingId, customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        return items.findAllByBooking_IdOrderByIdAsc(bookingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getBooking().getId(),
                item.getName(),
                item.getDescription(),
                item.getSize(),
                item.getWeightKg());
    }
}