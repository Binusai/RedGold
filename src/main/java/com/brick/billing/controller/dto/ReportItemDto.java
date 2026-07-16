package com.brick.billing.controller.dto;

public record ReportItemDto(
        String description,
        String uom,
        Double rate,
        Double qty,
        Double discount,
        Double total
) {}
