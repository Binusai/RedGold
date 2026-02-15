package com.brick.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.brick.billing.controller.dto.BookingExportRow;
import com.brick.billing.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
    SELECT b FROM Booking b
    JOIN FETCH b.customer
    ORDER BY b.createdAt DESC
    """)
    List<Booking> findAllWithCustomer();


    // bookings WITHOUT report (for dropdown)
    @Query("""
    SELECT b FROM Booking b
    WHERE b.id NOT IN (
        SELECT r.booking.id FROM Report r
    )
    ORDER BY b.createdAt DESC
    """)
    List<Booking> findBookingsWithoutReport();

    @Query("""
    SELECT new com.brick.billing.controller.dto.BookingExportRow(
        b.id,
        c.name,
        c.mobile,
        b.quantity,
        b.status,
        b.createdAt
    )
    FROM Booking b
    JOIN b.customer c
    ORDER BY b.createdAt DESC
    """)
    List<BookingExportRow> fetchAllForExport();

}
