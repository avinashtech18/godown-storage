package com.godown.storage.service;

import com.godown.storage.dto.InvoiceResponse;
import com.godown.storage.entity.Booking;
import com.godown.storage.entity.Invoice;
import com.godown.storage.repository.BookingRepository;
import com.godown.storage.repository.InvoiceRepository;
import com.godown.storage.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import com.godown.storage.entity.InvoiceStatus;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;

@Service
public class InvoiceService {

    private static final ZoneId WAREHOUSE_ZONE = ZoneId.of("Asia/Kolkata");

    private static final BigDecimal LATE_FEE = new BigDecimal("50.00");
    private final BookingRepository bookings;
    private final InvoiceRepository invoices;
    private final ItemRepository items;
    private final InvoicePriceCalculator calculator;

    public InvoiceService(
            BookingRepository bookings,
            InvoiceRepository invoices,
            ItemRepository items,
            InvoicePriceCalculator calculator) {
        this.bookings = bookings;
        this.invoices = invoices;
        this.items = items;
        this.calculator = calculator;
    }



    @Transactional
    public InvoiceResponse generate(Long bookingId) {
        // Share the booking lock with item registration and other invoice requests.
        Booking booking = bookings.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        LocalDate today = LocalDate.now(WAREHOUSE_ZONE);
        YearMonth billingMonth = YearMonth.from(today);
        LocalDate monthStart = billingMonth.atDay(1);

        if (booking.getStartDate().isAfter(today)
                || (booking.getEndDate() != null && booking.getEndDate().isBefore(monthStart))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking has no billable days this month");
        }

        Invoice invoice = invoices.findByBooking_IdAndBillingMonth(bookingId, monthStart)
                .orElseGet(() -> {
                    BigDecimal amount = calculator.calculate(
                            items.findAllByBooking_IdOrderByIdAsc(bookingId));
                    return invoices.save(
                            new Invoice(booking, billingMonth, amount, today.plusDays(7)));
                });

        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listOwnInvoices(Long customerId, Long bookingId) {
        // Check ownership even when the booking has no invoices.
        if (!bookings.existsByIdAndCustomer_Id(bookingId, customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }

        return invoices.findAllByBooking_IdOrderByBillingMonthDesc(bookingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getOwnInvoice(Long customerId, Long bookingId, Long invoiceId) {
        return invoices.findByIdAndBooking_IdAndBooking_Customer_Id(
                        invoiceId, bookingId, customerId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invoice not found"));
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void updateOverdueInvoices() {
        invoices.markOverdue(
                InvoiceStatus.PENDING,
                InvoiceStatus.OVERDUE,
                LATE_FEE,
                LocalDate.now(WAREHOUSE_ZONE));
    }

    @Transactional
    public InvoiceResponse recordPayment(Long bookingId, Long invoiceId) {
        // Lock the invoice so concurrent requests preserve the first payment timestamp.
        Invoice invoice = invoices.findByIdAndBooking_Id(invoiceId, bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invoice not found"));

        Instant now = Instant.now();
        invoice.markPaid(now, now.atZone(WAREHOUSE_ZONE).toLocalDate(), LATE_FEE);
        return toResponse(invoice);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getBooking().getId(),
                invoice.getBillingMonth(),
                invoice.getAmount(),
                invoice.getLateFee(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getPaidAt());
    }
}