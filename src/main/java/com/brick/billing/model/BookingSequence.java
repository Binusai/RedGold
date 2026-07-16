package com.brick.billing.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Tracks the last booking counter used for a given date.
 * One row per calendar date, e.g.:
 *   seqDate = 2026-07-16, lastNumber = 3   -> next booking code will be CM16072026004
 *
 * A new row is created automatically the first time a booking happens on a new date,
 * so the counter naturally resets to 001 each day.
 */
@Entity
public class BookingSequence {

    @Id
    @Column(name = "seq_date")
    private LocalDate seqDate;

    @Column(name = "last_number", nullable = false)
    private Integer lastNumber = 0;

    public BookingSequence() {}

    public BookingSequence(LocalDate seqDate, Integer lastNumber) {
        this.seqDate = seqDate;
        this.lastNumber = lastNumber;
    }

    public LocalDate getSeqDate() { return seqDate; }
    public void setSeqDate(LocalDate seqDate) { this.seqDate = seqDate; }

    public Integer getLastNumber() { return lastNumber; }
    public void setLastNumber(Integer lastNumber) { this.lastNumber = lastNumber; }
}
