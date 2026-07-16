package com.brick.billing.controller.dto;

import java.util.List;

public record ReportRequest(

        Long bookingId,
        String status,
        Double netTotal,
        Double discount,
        Double finalTotal,
        String remarks,

        List<ReportItemDto> items,

        // Editable booking fields
        String customerName,
        String mobile,
        String email,
        String address,
        String location

        // ⚠️ quantity removed — booking quantity is fixed at creation time
        // and is no longer editable from the Report screen.

) {}
