package com.brick.billing.controller.dto;

public record ReportItemDto(
        String item,
        String description,
        Double rate,
        Double qty,
        Double total
) {}
