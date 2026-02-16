package com.brick.billing.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brick.billing.controller.dto.ReportItemDto;
import com.brick.billing.controller.dto.ReportRequest;
import com.brick.billing.controller.dto.ReportViewDto;
import com.brick.billing.model.Booking;
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

    // SAVE DRAFT
    @PostMapping("/save-draft")
    public Long saveDraft(@RequestBody ReportRequest req) {
        return save(req);
    }

    // FINALIZE REPORT
    @PostMapping("/finalize")
    public Long finalizeReport(@RequestBody ReportRequest req) {
        return save(req);
    }

    @GetMapping("/by-booking/{bookingId}")
    @Transactional(readOnly = true)
    public ReportViewDto getReportByBooking(@PathVariable Long bookingId) {

        Report report = reportRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        var items = report.getItems().stream()
                .map(i -> new ReportItemDto(
                        i.getItem(),
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



    // DOWNLOAD PDF
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

    // COMMON SAVE METHOD
    @Transactional
    private Long save(ReportRequest req) {

        Booking booking = bookingRepo.findById(req.bookingId()).orElseThrow();

        // Check if report already exists for this booking
        Report report = reportRepo.findByBookingId(booking.getId()).orElse(new Report());

        report.setBooking(booking);
        report.setStatus(req.status());
        report.setNetTotal(req.netTotal());
        report.setDiscount(req.discount());
        report.setFinalTotal(req.finalTotal());
        report.setRemarks(req.remarks());

        var items = new ArrayList<ReportItem>();

        for (ReportItemDto dto : req.items()) {
            ReportItem item = new ReportItem();
            item.setReport(report);
            item.setDescription(dto.description());
            item.setRate(dto.rate());
            item.setQty(dto.qty());
            item.setTotal(dto.total());
            items.add(item);
        }

        report.setItems(items);

        reportRepo.save(report);

        // ---- Update booking workflow status ----
        String newStatus = "COMPLETED".equals(req.status()) ? "COMPLETED" : "DRAFT";

        booking.setStatus(newStatus);

        // force update immediately (avoid Hibernate cache issue)
        bookingRepo.saveAndFlush(booking);

        return report.getId();

    }


}
