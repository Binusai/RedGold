package com.brick.billing.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brick.billing.model.BookingSequence;
import com.brick.billing.repository.BookingSequenceRepository;

@Service
public class BookingCodeService {

    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final BookingSequenceRepository sequenceRepo;

    public BookingCodeService(BookingSequenceRepository sequenceRepo) {
        this.sequenceRepo = sequenceRepo;
    }

    /**
     * Generates the next booking code for today, e.g. CM16072026001, CM16072026002, ...
     * Resets to 001 automatically on a new calendar date.
     * Runs in its own transaction with a row lock so concurrent bookings never collide.
     */
    @Transactional
    public String generateNextCode() {

        LocalDate today = LocalDate.now();

        BookingSequence seq = sequenceRepo.findForUpdate(today)
                .orElseGet(() -> new BookingSequence(today, 0));

        int nextNumber = seq.getLastNumber() + 1;
        seq.setLastNumber(nextNumber);
        sequenceRepo.save(seq);

        String datePart = today.format(DATE_PART);
        String numberPart = String.format("%03d", nextNumber);

        return "CM" + datePart + numberPart;
    }
}
