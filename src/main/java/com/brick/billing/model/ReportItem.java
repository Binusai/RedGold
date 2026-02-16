package com.brick.billing.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;


@Entity
public class ReportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    private Report report;


    private String description;
    private Double rate;
    private Double qty;
    private Double total;

    // getters & setters
    public void setReport(Report report) { this.report = report; }


    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }

    public Double getQty() { return qty; }
    public void setQty(Double qty) { this.qty = qty; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
