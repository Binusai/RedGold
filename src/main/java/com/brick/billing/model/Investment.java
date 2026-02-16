package com.brick.billing.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate createdDate;

    private String remarks;

    private Double grandTotal;

    @OneToMany(mappedBy = "investment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvestmentItem> items = new ArrayList<>();

    public Long getId() { return id; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }

    public List<InvestmentItem> getItems() { return items; }
}
