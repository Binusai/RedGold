package com.brick.billing.controller.dto;

import java.util.List;

public record ReportRequest(
        Long bookingId,
        String status,
        Double netTotal,
        Double discount,
        Double finalTotal,
        String remarks,
        List<ReportItemDto> items
        
        String customerName,
        String mobile,
        String email,
        String address,
        Integer quantity,
        String location,

) {}
