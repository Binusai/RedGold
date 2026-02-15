package com.brick.billing.controller;

public record CustomerBookingRequest(
        String name,
        String email,
        String mobile,
        String whatsapp,
        String address,
        String location,
        Integer quantity
) {}
