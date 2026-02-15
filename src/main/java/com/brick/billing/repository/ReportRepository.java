package com.brick.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.brick.billing.model.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByBookingId(Long bookingId);

    @Query("""
    SELECT r FROM Report r
    JOIN FETCH r.booking b
    JOIN FETCH b.customer
    WHERE r.status = 'COMPLETED'
    ORDER BY r.createdAt DESC
    """)
    List<Report> findCompletedReports();
    @Query("""
    SELECT 
        COUNT(r),
        COALESCE(SUM(r.finalTotal), 0),
        COALESCE(SUM(CASE WHEN r.paymentDone = true THEN r.finalTotal ELSE 0 END), 0),
        COUNT(CASE WHEN r.paymentDone = true THEN 1 END)
    FROM Report r
    WHERE r.status = 'COMPLETED'
    """)
    List<Object[]> getRevenueSummary();



}
