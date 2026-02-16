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

    // SAVE OR UPDATE
    @PostMapping("/save")
    public Long save(@RequestBody InvestmentRequest req) {

        Investment inv = (req.id() != null)
                ? repo.findByIdWithItems(req.id()).orElse(new Investment())
                : new Investment();

        if (inv.getCreatedDate() == null) {
            inv.setCreatedDate(LocalDate.parse(req.createdDate()));
        }

        inv.setRemarks(req.remarks());

        List<InvestmentItem> newItems = new ArrayList<>();
        double grandTotal = 0;

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

            grandTotal += dto.total();
            newItems.add(item);
        }

        inv.setItems(newItems);
        inv.setGrandTotal(grandTotal);

        repo.saveAndFlush(inv);
        return inv.getId();
    }

    // LIST
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
                        it.getRemarks()
                    )
                ).toList()
            )
        ).toList();
    }

    // LOAD SINGLE
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
                                it.getRemarks()
                        )
                ).toList()
        );
    }

    // PROFIT SUMMARY
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
