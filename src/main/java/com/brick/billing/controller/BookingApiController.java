package com.brick.billing.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brick.billing.model.Booking;
import com.brick.billing.repository.BookingRepository;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private final BookingRepository bookingRepository;

    public BookingApiController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllWithCustomer();

    }

    

    @GetMapping("/pending-report")
    public List<Booking> getBookingsWithoutReport() {
        return bookingRepository.findBookingsWithoutReport();
    }
}

