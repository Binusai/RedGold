package com.brick.billing.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brick.billing.model.Booking;
import com.brick.billing.model.Customer;
import com.brick.billing.repository.BookingRepository;
import com.brick.billing.repository.CustomerRepository;
import com.brick.billing.service.BookingCodeService;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final CustomerRepository customerRepo;
    private final BookingRepository bookingRepo;
    private final BookingCodeService bookingCodeService;

    public BookingController(CustomerRepository c, BookingRepository b, BookingCodeService bookingCodeService) {
        this.customerRepo = c;
        this.bookingRepo = b;
        this.bookingCodeService = bookingCodeService;
    }

    @PostMapping("/add-booking")
    public Long addBooking(@RequestBody CustomerBookingRequest req) {

        Customer c = new Customer();
        c.setName(req.name());
        c.setEmail(req.email());
        c.setMobile(req.mobile());
        c.setWhatsapp(req.whatsapp());
        c.setAddress(req.address());
        c.setLocation(req.location());

        customerRepo.save(c);

        Booking booking = new Booking();
        booking.setCustomer(c);
        booking.setQuantity(req.quantity());
        booking.setStatus("PENDING");   // important for your workflow
        booking.setBookingCode(bookingCodeService.generateNextCode());   // ✅ CM16072026001 style code

        bookingRepo.save(booking);

        return booking.getId();   // ✅ RETURN ID TO FRONTEND
    }


}
