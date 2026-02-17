package com.brick.billing.controller;

import com.brick.billing.model.*;
import com.brick.billing.repository.*;
import com.brick.billing.controller.dto.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/investments")
@Transactional
public class InvestmentController {

    private final InvestmentRepository repo;
    private final ReportRepository reportRepository;

    public InvestmentController(InvestmentRepository repo,
                                ReportRepository reportRepository) {
        this.repo = repo;
        this.reportRepository = reportRepository;
    }

    // ✅ CREATE NEW BATCH OR APPEND TO EXISTING
    @PostMapping("/save")
    public Long save(@RequestBody InvestmentRequest req) {

        Investment inv;

        if (req.id() != null) {
            // LOAD existing batch (append mode)
            inv = repo.findByIdWithItems(req.id()).orElseThrow();
        } else {
            // CREATE new batch
            inv = new Investment();
            inv.setCreatedDate(LocalDate.parse(req.createdDate()));
            inv.setGrandTotal(0.0);
            inv.setItems(new ArrayList<>());
        }

        // Append only new rows
        for (InvestmentItemDto dto : req.items()) {

            if (dto.description() == null || dto.description().isBlank()) continue;

            InvestmentItem item = new InvestmentItem();
            item.setInvestment(inv);
            item.setDescription(dto.description());
            item.setPackageSize(dto.packageSize());
            item.setQty(dto.qty());
            item.setRate(dto.rate());
            item.setDiscount(dto.discount());
            item.setTotal(dto.total());
            item.setRemarks(dto.remarks());

            // 🔴 Per-row entry date (core change)
            item.setEntryDate(LocalDate.parse(dto.entryDate()));

            inv.getItems().add(item);
        }

        // Recalculate grand total from ALL rows (safe)
        double grandTotal = inv.getItems()
                .stream()
                .mapToDouble(i -> i.getTotal() == null ? 0 : i.getTotal())
                .sum();

        inv.setGrandTotal(grandTotal);

        repo.save(inv);
        return inv.getId();
    }

    // ✅ LIST BATCHES
    @GetMapping("/all")
    @Transactional(readOnly = true)
    public List<InvestmentViewDto> list() {

        return repo.findAllWithItems().stream().map(i ->
                new InvestmentViewDto(
                        i.getId(),
                        i.getCreatedDate().toString(),
                        i.getGrandTotal(),
                        i.getRemarks(),
                        i.getItems().stream().map(it ->
                                new InvestmentItemDto(
                                        it.getDescription(),
                                        it.getPackageSize(),
                                        it.getQty(),
                                        it.getRate(),
                                        it.getDiscount(),
                                        it.getTotal(),
                                        it.getRemarks(),
                                        it.getEntryDate() == null ? null : it.getEntryDate().toString()
                                )
                        ).toList()
                )
        ).toList();
    }

    // ✅ LOAD SINGLE BATCH
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public InvestmentViewDto get(@PathVariable Long id) {

        Investment i = repo.findByIdWithItems(id).orElseThrow();

        return new InvestmentViewDto(
                i.getId(),
                i.getCreatedDate().toString(),
                i.getGrandTotal(),
                i.getRemarks(),
                i.getItems().stream().map(it ->
                        new InvestmentItemDto(
                                it.getDescription(),
                                it.getPackageSize(),
                                it.getQty(),
                                it.getRate(),
                                it.getDiscount(),
                                it.getTotal(),
                                it.getRemarks(),
                                it.getEntryDate() == null ? null : it.getEntryDate().toString()
                        )
                ).toList()
        );
    }

    // ✅ PROFIT SUMMARY (unchanged)
    @GetMapping("/summary")
    @Transactional(readOnly = true)
    public Map<String, Double> summary() {

        Double invested = repo.findAll().stream()
                .mapToDouble(i -> i.getGrandTotal() == null ? 0 : i.getGrandTotal())
                .sum();

        Double revenue = reportRepository.sumAllFinalTotals();
        if (revenue == null) revenue = 0.0;

        Double profit = revenue - invested;

        return Map.of(
                "invested", invested,
                "revenue", revenue,
                "profit", profit
        );
    }
}
