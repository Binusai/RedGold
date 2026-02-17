package com.brick.billing.controller.dto;

public record InvestmentItemDto(
        String description,
        String packageSize,
        private String entryDate;
        Double qty,
        Double rate,
        Double discount,
        Double total,
        String remarks
) {}
