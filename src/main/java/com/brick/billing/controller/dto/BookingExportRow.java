package com.brick.billing.controller.dto;

import java.time.LocalDateTime;

public record BookingExportRow(
        Long id,
        String customerName,
        String mobile,
        Integer quantity,
        String status,
        LocalDateTime createdAt
) {}
