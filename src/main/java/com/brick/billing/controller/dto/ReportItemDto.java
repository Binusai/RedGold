package com.brick.billing.controller.dto;

public record ReportItemDto(
        String description,
        Double rate,
        Double qty,
        Double total
) {}
