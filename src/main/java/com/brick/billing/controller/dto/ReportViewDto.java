package com.brick.billing.controller.dto;

import java.util.List;

public record ReportViewDto(
        Long id,
        String status,
        Double netTotal,
        Double discount,
        Double finalTotal,
        String remarks,
        List<ReportItemDto> items
) {}
