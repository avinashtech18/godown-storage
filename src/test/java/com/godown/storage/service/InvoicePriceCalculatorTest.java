package com.godown.storage.service;

import com.godown.storage.entity.Booking;
import com.godown.storage.entity.Item;
import com.godown.storage.entity.ItemSize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class InvoicePriceCalculatorTest {

    private final InvoicePriceCalculator calculator = new InvoicePriceCalculator();

    @Test
    void calculatesPriceForTheSuitcase() {
        Booking booking = mock(Booking.class);
        Item suitcase = new Item(
                booking, "Travel suitcase", null, ItemSize.MEDIUM, new BigDecimal("12.50"));

        assertEquals(new BigDecimal("312.50"), calculator.calculate(List.of(suitcase)));
    }

    @Test
    void sumsPricesAcrossAllThreeSizes() {
        Booking booking = mock(Booking.class);
        Item small = new Item(booking, "Small box", null, ItemSize.SMALL, new BigDecimal("2.00"));
        Item medium = new Item(booking, "Suitcase", null, ItemSize.MEDIUM, new BigDecimal("12.50"));
        Item large = new Item(booking, "Large box", null, ItemSize.LARGE, new BigDecimal("20.00"));

        assertEquals(new BigDecimal("1022.50"), calculator.calculate(List.of(small, medium, large)));
    }

    @Test
    void returnsZeroForAnEmptyBooking() {
        assertEquals(new BigDecimal("0.00"), calculator.calculate(List.of()));
    }
}