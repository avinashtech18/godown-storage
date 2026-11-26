package com.godown.storage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A customer and a unit can each have multiple bookings over time.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private StorageUnit unit;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    public Booking(User customer, StorageUnit unit, LocalDate startDate) {
        this.customer = customer;
        this.unit = unit;
        this.startDate = startDate;
        this.status = BookingStatus.ACTIVE;
    }

    public void close(LocalDate endDate) {
        this.endDate = endDate;
        this.status = BookingStatus.CLOSED;
    }

    @Column(name = "scheduled_drop_off_at")
    private LocalDateTime scheduledDropOffAt;

    @Column(name = "scheduled_pickup_at")
    private LocalDateTime scheduledPickupAt;

    public void scheduleHandover(LocalDateTime dropOffAt, LocalDateTime pickupAt) {
        this.scheduledDropOffAt = dropOffAt;
        this.scheduledPickupAt = pickupAt;
    }
}