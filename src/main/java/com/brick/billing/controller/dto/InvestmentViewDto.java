package com.brick.billing.controller.dto;

import java.util.List;

public record InvestmentViewDto(
        Long id,
        String createdDate,
        Double grandTotal,
        String remarks,
        List<InvestmentItemDto> items
) {}
