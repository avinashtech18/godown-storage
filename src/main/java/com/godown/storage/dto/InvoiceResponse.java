package com.godown.storage.dto;

import com.godown.storage.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InvoiceResponse(
        Long id,
        Long bookingId,
        LocalDate billingMonth,
        BigDecimal amount,
        BigDecimal lateFee,
        LocalDate dueDate,
        InvoiceStatus status,
        Instant paidAt
) {
}