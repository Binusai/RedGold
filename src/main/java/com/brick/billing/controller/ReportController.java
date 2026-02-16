package com.brick.billing.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.brick.billing.controller.dto.ReportItemDto;
import com.brick.billing.controller.dto.ReportRequest;
import com.brick.billing.controller.dto.ReportViewDto;
import com.brick.billing.model.Booking;
import com.brick.billing.model.Customer;   // ✅ IMPORTANT (was missing)
import com.brick.billing.model.Report;
import com.brick.billing.model.ReportItem;
import com.brick.billing.repository.BookingRepository;
import com.brick.billing.repository.ReportRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final BookingRepository bookingRepo;
    private final ReportRepository reportRepo;

    public ReportController(BookingRepository bookingRepo, ReportRepository reportRepo) {
        this.bookingRepo = bookingRepo;
        this.reportRepo = reportRepo;
    }

    // ---------------- SAVE DRAFT ----------------
    @PostMapping("/save-draft")
    public Long saveDraft(@RequestBody ReportRequest req) {
        return save(req);
    }

    // ---------------- FINALIZE REPORT ----------------
    @PostMapping("/finalize")
    public Long finalizeReport(@RequestBody ReportRequest req) {
        return save(req);
    }

    // ---------------- LOAD REPORT FOR EDIT/VIEW ----------------
    @GetMapping("/by-booking/{bookingId}")
    @Transactional(readOnly = true)
    public ReportViewDto getReportByBooking(@PathVariable Long bookingId) {

        Report report = reportRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // ✅ Explicit type (fix Docker compile issue)
        List<ReportItemDto> items = report.getItems().stream()
                .map(i -> new ReportItemDto(
                        i.getDescription(),
                        i.getRate(),
                        i.getQty(),
                        i.getTotal()
                ))
                .toList();

        return new ReportViewDto(
                report.getId(),
                report.getStatus(),
                report.getNetTotal(),
                report.getDiscount(),
                report.getFinalTotal(),
                report.getRemarks(),
                items
        );
    }

    // ---------------- DOWNLOAD PDF ----------------
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws Exception {

        Report report = reportRepo.findById(id).orElseThrow();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("RED GOLD REPORT"));
        document.add(new Paragraph("Customer: " + report.getBooking().getCustomer().getName()));
        document.add(new Paragraph("Mobile: " + report.getBooking().getCustomer().getMobile()));
        document.add(new Paragraph("Quantity: " + report.getBooking().getQuantity()));
        document.add(new Paragraph("Final Total: ₹ " + report.getFinalTotal()));

        document.close();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    // ---------------- COMMON SAVE METHOD ----------------
    @Transactional
    private Long save(ReportRequest req) {
    
        Booking booking = bookingRepo.findByIdWithCustomer(req.bookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));
    
        // ---- update customer ----
        Customer customer = booking.getCustomer();
        customer.setName(req.customerName());
        customer.setMobile(req.mobile());
        customer.setEmail(req.email());
        customer.setAddress(req.address());
        customer.setLocation(req.location());
    
        // ---- update booking ----
        booking.setQuantity(req.quantity());
    
        // ---- find or create report (WITH items fetched) ----
        Report report = reportRepo.findByBookingIdWithItems(booking.getId()).orElse(null);
    
        if (report == null) {
            report = new Report();
            report.setBooking(booking);
        }
    
        report.setStatus(req.status());
        report.setNetTotal(req.netTotal());
        report.setDiscount(req.discount());
        report.setFinalTotal(req.finalTotal());
        report.setRemarks(req.remarks());
    
        // ---- replace items safely (NO clear(), NO add()) ----
        List<ReportItem> newItems = new ArrayList<>();
    
        for (ReportItemDto dto : req.items()) {
    
            if (dto.description() == null || dto.description().isBlank()) continue;
    
            ReportItem item = new ReportItem();
            item.setReport(report);
            item.setDescription(dto.description());
            item.setRate(dto.rate());
            item.setQty(dto.qty());
            item.setTotal(dto.total());
    
            newItems.add(item);
        }
    
        report.setItems(newItems);
    
        // ---- save ----
        reportRepo.saveAndFlush(report);
    
        // ---- workflow ----
        booking.setStatus("COMPLETED".equals(req.status()) ? "COMPLETED" : "DRAFT");
        bookingRepo.saveAndFlush(booking);
    
        return report.getId();
    }

}
