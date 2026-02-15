package com.brick.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brick.billing.model.ReportItem;

public interface ReportItemRepository extends JpaRepository<ReportItem, Long> {
}
