package com.brick.billing.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brick.billing.model.Report;
import com.brick.billing.repository.ReportRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final ReportRepository reportRepo;

    // -------------------------
    // 1. LIST COMPLETED REPORTS ONLY
    // -------------------------
    @GetMapping
    public List<Map<String, Object>> getRevenueReports(
            @RequestParam(defaultValue = "lifetime") String filter) {

        List<Report> reports = reportRepo.findCompletedReports();

        return reports.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();

            m.put("reportId", r.getId());
            m.put("customerName", r.getBooking().getCustomer().getName());
            m.put("mobile", r.getBooking().getCustomer().getMobile());
            m.put("address", r.getBooking().getCustomer().getAddress());
            m.put("quantity", r.getBooking().getQuantity());
            m.put("finalTotal", r.getFinalTotal());
            m.put("paymentDone", Boolean.TRUE.equals(r.getPaymentDone()));

            return m;
        }).toList();
    }

    // -------------------------
    // 2. SUMMARY (FOR RINGS)
    // -------------------------
    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestParam(defaultValue = "lifetime") String filter) {

        List<Object[]> result = reportRepo.getRevenueSummary();

        Object[] row = result.isEmpty() ? new Object[]{0,0,0,0} : result.get(0);

        long totalReports = ((Number) row[0]).longValue();
        double totalAmount = ((Number) row[1]).doubleValue();
        double paidAmount = ((Number) row[2]).doubleValue();
        long paidCount = ((Number) row[3]).longValue();

        double paidPercentage = totalReports == 0
                ? 0
                : (paidCount * 100.0 / totalReports);

        Map<String, Object> map = new HashMap<>();
        map.put("totalCompletedAmount", totalAmount);
        map.put("totalPaidAmount", paidAmount);
        map.put("paidPercentage", paidPercentage);

        return map;
    }


    // -------------------------
    // 3. MARK PAYMENT DONE
    // -------------------------
    @PutMapping("/{id}/pay")
    @Transactional
    public void markPaid(@PathVariable Long id) {

        Report r = reportRepo.findById(id).orElseThrow();
        r.setPaymentDone(true);
    }
}
