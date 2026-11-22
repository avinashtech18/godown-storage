package com.godown.storage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoices_booking_billing_month",
                columnNames = {"booking_id", "billing_month"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "late_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "paid_at")
    private Instant paidAt;

    public Invoice(Booking booking, YearMonth billingMonth,
                   BigDecimal amount, LocalDate dueDate) {
        this.booking = booking;
        // Store each month as its first day so the unique constraint sees one key per month.
        this.billingMonth = billingMonth.atDay(1);
        this.amount = amount;
        this.lateFee = BigDecimal.ZERO;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.PENDING;
    }
    public void markPaid(Instant paidAt, LocalDate paymentDate, BigDecimal overdueFee) {
        if (status == InvoiceStatus.PAID) {
            return;
        }

        // Apply the fee even if payment arrives before the scheduled overdue check runs.
        if (status == InvoiceStatus.PENDING && paymentDate.isAfter(dueDate)) {
            this.lateFee = overdueFee;
        }

        this.paidAt = paidAt;
        this.status = InvoiceStatus.PAID;
    }
}