package com.brick.billing.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import com.brick.billing.model.BookingSequence;

public interface BookingSequenceRepository extends JpaRepository<BookingSequence, LocalDate> {

    // Pessimistic write lock: if two bookings are created at the exact same moment,
    // the second request waits for the first to finish updating the counter,
    // so we never hand out the same booking code twice.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BookingSequence s WHERE s.seqDate = :date")
    Optional<BookingSequence> findForUpdate(LocalDate date);
}
