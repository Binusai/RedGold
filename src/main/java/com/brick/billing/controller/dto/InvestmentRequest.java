package com.brick.billing.controller.dto;

import java.util.List;

public record InvestmentRequest(
        Long id,
        String createdDate,
        String remarks,
        List<InvestmentItemDto> items
) {}
