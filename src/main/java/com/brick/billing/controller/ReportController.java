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
import com.brick.billing.model.Customer;
import com.brick.billing.model.Report;
import com.brick.billing.model.ReportItem;
import com.brick.billing.repository.BookingRepository;
import com.brick.billing.repository.CustomerRepository;
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
    private final CustomerRepository customerRepo;

    public ReportController(BookingRepository bookingRepo, ReportRepository reportRepo, CustomerRepository customerRepo) {
        this.bookingRepo = bookingRepo;
        this.reportRepo = reportRepo;
        this.customerRepo = customerRepo;
    }

    // ---------------- SAVE DRAFT ----------------
    @PostMapping("/save-draft")
    @Transactional
    public Long saveDraft(@RequestBody ReportRequest req) {
        return save(req);
    }

    // ---------------- FINALIZE REPORT ----------------
    @PostMapping("/finalize")
    @Transactional
    public Long finalizeReport(@RequestBody ReportRequest req) {
        return save(req);
    }

    // ---------------- LOAD REPORT FOR EDIT/VIEW ----------------
    @GetMapping("/by-booking/{bookingId}")
    @Transactional(readOnly = true)
    public ReportViewDto getReportByBooking(@PathVariable Long bookingId) {

        Report report = reportRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        List<ReportItemDto> items = report.getItems().stream()
                .map(i -> new ReportItemDto(
                        i.getDescription(),
                        i.getUom(),
                        i.getRate(),
                        i.getQty(),
                        i.getDiscount(),
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

    // ---------------- DOWNLOAD PDF (legacy / unused by current frontend) ----------------
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws Exception {

        Report report = reportRepo.findById(id).orElseThrow();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("CITIZEN PAPER PRODUCTS"));
        document.add(new Paragraph("Customer: " + report.getBooking().getCustomer().getName()));
        document.add(new Paragraph("Mobile: " + report.getBooking().getCustomer().getMobile()));
        document.add(new Paragraph("Final Total: Rs. " + report.getFinalTotal()));

        document.close();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    // ---------------- COMMON SAVE METHOD ----------------
    // NOTE: no @Transactional here on purpose - private/self-invoked methods are not
    // intercepted by Spring's transaction proxy. The real transaction boundary is on
    // saveDraft()/finalizeReport() above, which are the actual public entry points.
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

        // FIX: explicitly persist the customer changes.
        // Previously these setters were never actually saved, so address/location edits
        // were silently discarded at the end of the request.
        customerRepo.save(customer);
        // ---- find or create report (WITH items fetched) ----
        Report report = reportRepo.findByBookingIdWithItems(booking.getId()).orElse(null);
        
        if (report == null) {
            report = new Report();
            report.setBooking(booking);
        }
        report.setStatus(req.status());
        report.setRemarks(req.remarks());
        // ---- replace items ----
        if (report.getItems() == null) {
            report.setItems(new ArrayList<>());
        } else {
            report.getItems().clear();
        }
        
        double netTotal = 0.0;
        double totalDiscount = 0.0;
        
        for (ReportItemDto dto : req.items()) {
        
            if (dto.description() == null || dto.description().isBlank()) continue;
        
            double rate = dto.rate() == null ? 0.0 : dto.rate();
            double qty = dto.qty() == null ? 0.0 : dto.qty();
            double rowDiscount = dto.discount() == null ? 0.0 : dto.discount();
            double rowGross = rate * qty;
            double rowTotal = rowGross - rowDiscount;
        
            ReportItem item = new ReportItem();
            item.setReport(report);
            item.setDescription(dto.description());
            item.setUom(dto.uom() == null ? null : dto.uom().toUpperCase());
            item.setRate(rate);
            item.setQty(qty);
            item.setDiscount(rowDiscount);
            item.setTotal(rowTotal);
        
            report.getItems().add(item);
        
            netTotal += rowGross;
            totalDiscount += rowDiscount;
        }

        double finalTotal = netTotal - totalDiscount;

        report.setNetTotal(netTotal);
        report.setDiscount(totalDiscount);
        report.setFinalTotal(finalTotal);

        // ---- save ----
        reportRepo.saveAndFlush(report);

        // ---- workflow ----
        booking.setStatus("COMPLETED".equals(req.status()) ? "COMPLETED" : "DRAFT");
        bookingRepo.saveAndFlush(booking);

        return report.getId();
    }

}
