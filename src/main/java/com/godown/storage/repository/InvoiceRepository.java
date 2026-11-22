package com.godown.storage.repository;

import com.godown.storage.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import com.godown.storage.entity.InvoiceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByBooking_IdAndBillingMonth(
            Long bookingId, LocalDate billingMonth);
    List<Invoice> findAllByBooking_IdOrderByBillingMonthDesc(Long bookingId);

    Optional<Invoice> findByIdAndBooking_IdAndBooking_Customer_Id(
            Long invoiceId, Long bookingId, Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Invoice> findByIdAndBooking_Id(Long invoiceId, Long bookingId);

    @Modifying
    @Query("""
        update Invoice i
        set i.status = :overdue, i.lateFee = :fee
        where i.status = :pending and i.dueDate < :today
        """)
    int markOverdue(
            @Param("pending") InvoiceStatus pending,
            @Param("overdue") InvoiceStatus overdue,
            @Param("fee") BigDecimal fee,
            @Param("today") LocalDate today);
}